package edu.ohiou.mfgresearch.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;
import edu.ohiou.mfgresearch.simplanner.ProcessPlanningKnowledge;
import jess.Rete;
import jess.Value;

public class FeatureProcessMatching {
	
	static Logger log = LoggerFactory.getLogger(FeatureProcessMatching.class);
	
	Model localKB;
	String localPath;
	PropertyReader prop = PropertyReader.getProperty();
	String featureSpec = "", processInd = "", function ="";
	
	public Model getLocalKB() {
		return localKB;
	}

	public void reloadKB(String fileName){
		Uni.of(localPath+fileName)
		   .map(File::new)
		   .map(FileOutputStream::new)
		   .set(s->localKB.write(s, "RDF/XML"))
		   .set(s->s.flush())
		   .set(s->s.close());
//		localKB = ModelFactory.createDefaultModel().read(localPath+fileName);	
	}
	
	public void saveKB(String fileName, Model m){
		Uni.of(localPath+fileName)
		   .map(File::new)
		   .map(FileOutputStream::new)
		   .set(s->m.write(s, "RDF/XML"))
		   .set(s->s.flush())
		   .set(s->s.close());
//		localKB = ModelFactory.createDefaultModel().read(localPath+fileName);	
	}
	
	public FeatureProcessMatching(String[] localIRI) {
		if(localKB == null) {
			localKB = ModelFactory.createDefaultModel();
			localKB.setNsPrefix("", IMPM.plan_ins);
		}
		if(localIRI.length>0) localIRI = new String[]{localPath};
		Omni.of(localIRI)
			.map(path->localKB.add(ModelFactory.createDefaultModel().read(path)));
		IMPM.clearSessionPath();
		localPath = IMPM.createSessionFolder(prop.getProperty("PLAN_ARCHIVE_FOLDER"));
	}
	
	/**
	 * event to update local belief
	 */
	public void loadSpecifications(Node featureIRI){
		log.info("\n ##load specifications for feature "+ featureIRI.getLocalName());

		//load specifications for the given feature
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
		   .set(q->q.addABox(GlobalKnowledge.getCurrentPart()))
		   //.set(q->saveKB("FPM-local-transfer-feature-specification-before.rdf", q.getBelief().getaBox()))
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-feature-specifications.rq"))
		   .set(q->q.getPlan(0).addVarBinding("f1", ResourceFactory.createResource(featureIRI.getURI()))) //bind the last intermediate feature (featureIRi) to filter out every dimension which are already matched
		   .set(q->q.setLocal=true)
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   if(tab.isEmpty()){
				  log.info("No specification is avalable for the feature " + featureSpec); 
			   }
			   else{
				   log.info("Trying to satisfy feature specification " + tab.rows().next().get(Var.alloc("fs")).getLocalName() +
						   "(" + tab.rows().next().get(Var.alloc("fName")).getLiteral().getValue() + ") with the following specifications." );
				   tab.rows().forEachRemaining(r->{
					   log.info(r.get(Var.alloc("d")).getLocalName() + " of type " + r.get(Var.alloc("dimType")).getLocalName() + " having value " + r.get(Var.alloc("dim")).getLiteral().getValue());
				   });
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .onSuccess(m->localKB.add(m))
		   ;
		reloadKB("FPM-local-transfer-feature-specification.rdf");
	}
	
	/**
	 * event to update capability
	 */
	public void loadCapability(Node processIRI, Node function){

		log.info("\n ##loading capability for process "+ processIRI.getLocalName() + " realizing " + function.getLocalName());
		//load capability with both max and min as equation
		log.info("\n ##running rule transfer-capability-measure.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getCapabilityABox())) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-measure.rq"))
		   .set(q->q.getPlan(0).addVarBinding("p", ResourceFactory.createResource(processIRI.getURI())))
		   .set(q->q.getPlan(0).addVarBinding("func", ResourceFactory.createResource(function.getURI())))
		   .set(q->q.setLocal=true)		   
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .onSuccess(m->localKB.add(m));
		
		//load capability with max as measurement and min as equation
		log.info("\n ##running rule transfer-capability-measure-equation.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getCapabilityABox())) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-measure-equation.rq"))
		   .set(q->q.getPlan(0).addVarBinding("p", ResourceFactory.createResource(processIRI.getURI())))
		   .set(q->q.getPlan(0).addVarBinding("func", ResourceFactory.createResource(function.getURI())))
		   .set(q->q.setLocal=true)
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .onSuccess(m->localKB.add(m));
		
		//load capability with max as equation and min as measurement
		log.info("\n ##running rule transfer-capability-equation-measure.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getCapabilityABox())) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-equation-measure.rq"))
		   .set(q->q.getPlan(0).addVarBinding("p", ResourceFactory.createResource(processIRI.getURI())))
		   .set(q->q.getPlan(0).addVarBinding("func", ResourceFactory.createResource(function.getURI())))
		   .set(q->q.setLocal=true)
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .onSuccess(m->localKB.add(m));
		
		reloadKB("FPM-local-transfer-capability.rdf");
	}	
	
	/**
	 * Service to post feature specification
	 * 
	 * @param featureIRI the intermediate feature IRI
	 * @param function type of process to be matched
	 * @return process IRIs generated for each function (capability profile) matched 
	 */
	public static Node ask_to_match(Node featureIRI, Node processIRI, Node functionURI){
		log.info("\n---------------------------------------------------------------------------------------------------------------------");
		//log.info("##Testing whether feature " + featureIRI.getLocalName() + " matches process " + processIRI.getLocalName() + "|\n");		
		
		FeatureProcessMatching matching = new FeatureProcessMatching(new String[]{});

		matching.featureSpec = featureIRI.getLocalName();
		matching.processInd = processIRI.getLocalName();
		matching.function = functionURI.getLocalName();
		
		//load localKB 
		matching.loadSpecifications(featureIRI);
		matching.loadCapability(processIRI, functionURI);
		
		//perform core rules
		matching.execute();
		
		//perform updates to global KB
		matching.postProcessing();
		
		//select result of execution
		return matching.selectResult(featureIRI); 
	} 

	private Node selectResult(Node featureIRI) {
		
		log.info("\n ##select intermediate feature to return.");
		
		List<Node> intermFeatures = new LinkedList<Node>();
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
		   .set(q->q.addABox(localKB))
		   .set(q->q.addPlan("resources/META-INF/rules/core/select_interim_feature.rq"))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
			   }
			   if(tab.isEmpty()){
				   log.info("Nothing to return as either all or no specification is satisfied by " + processInd);
			   }
			   else{
				   log.info("Intermediate feature " +  tab.rows().next().get(Var.alloc("f1")).getLocalName() + " is returned as output of process " + processInd);
			   }
			   tab.rows().forEachRemaining(b->{
				   intermFeatures.add(b.get(Var.alloc("f1")));
			   });
			   return tab;
		   }))
		   .map(q->q.execute());
		
		return intermFeatures.size()>0?intermFeatures.get(0):null;
	}

	public void execute(){
		
		//first rule: assign ibe of specifications to corresponding argument ICE by matching argument type with specification type
		//localKB.write(System.out, "NTRIPLE");
		log.info("\n ##running rule transform-capability-equation1... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation1.rq"))
		   .set(q->q.setLocal=true)
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   return tab;
		   }))
		   .set(q->q.setServicePostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .onSuccess(m->localKB.add(m));
		
		//second rule: assign concatenated argument sepcifications to equations ICE with is_tokenized_by
		//localKB.write(System.out, "NTRIPLE");
		log.info("\n ##running rule transform-capability-equation2... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation2.rq"))
		   .set(q->q.setLocal=true)
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   return tab;
		   }))
		   .set(q->q.setServicePostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .onSuccess(m->localKB.add(m));
		
		//second rule: assign concatenated argument sepcifications to equations ICE with is_tokenized_by
		//localKB.write(System.out, "NTRIPLE");
		log.info("\n ##running rule transform-capability-equation3... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation3.rq", this))
		   .set(q->q.setLocal=true)
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   return tab;
		   }))
		   .set(q->q.setServicePostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .onSuccess(m->localKB.add(m));
		
		reloadKB("before-match1.rdf");
		
		//this query is just to report all abailable cpability
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/select-combined-capability.q"))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(tab.isEmpty()){
				   log.info("No capability is found for process "+ processInd);
				   return tab;
			   }
			   tab.rows().forEachRemaining(r->{
				   log.info("Capability " + r.get(Var.alloc("capa")).getLocalName() + " (" + r.get(Var.alloc("capaType")).getLocalName() + ")" + "\t" +
						   			" max = " + r.get(Var.alloc("max")).getLiteral().getValue() + "\t" + " min = " + r.get(Var.alloc("min")).getLiteral().getValue() + "\t" +
						   			 " matches " + r.get(Var.alloc("refType")).getLocalName());
			   });
			   return tab;
		   }))
		   .map(q->q.execute())
		   .onFailure(e->log.error(e.getMessage()));	
		
		
		//third rule: specification-capability matching for max and min both measurement type
		log.info("\n ##running rule specification-capability-matching-limit.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/specification-capability-matching-limit.rq"))
		   .set(q->q.setLocal=true)
		   .set(q->q.setSelectPostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   return tab;
		   }))
		   .set(q->q.setServicePostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
			   }
			   if(tab.isEmpty()){
				   log.info("No dimension is satisfied by any capability of " + processInd);
			   }
			   else{
				   log.info("The following specifications of " + featureSpec + " are satisfied by " + processInd);
				   tab.rows().forEachRemaining(r->{
					   log.info("Dimension " +  r.get(Var.alloc("d")).getLocalName() + "(" +  r.get(Var.alloc("dimType")).getLocalName() + ")" + "\t" +
							   				" is satisfied by " +  r.get(Var.alloc("capa")).getLocalName() + "(" +  r.get(Var.alloc("capaType")).getLocalName() + ")");
				   });
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .set(m->localKB.add(m));		
		
		log.info("\n ##running rule specification-capability-not-matching-limit.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/specification-capability-not-matching-limit.rq"))
		   .set(q->q.setLocal=true)
		   .set(q->q.setServicePostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
			   }
			   if(tab.isEmpty()){
				   log.info("every dimension is satisfied by some capability of " + processInd);
			   }
			   else{
				   log.info("The following specifications of " + featureSpec + " are not satisfied by " + processInd);
				   tab.rows().forEachRemaining(r->{
					   log.info("Dimension " +  r.get(Var.alloc("d")).getLocalName() + "(" +  r.get(Var.alloc("dimType")).getLocalName() + ")" + "\t" +
							   				" is not satisfied by " +  r.get(Var.alloc("capa")).getLocalName() + "(" +  r.get(Var.alloc("capaType")).getLocalName() + ")");
				   });
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .set(m->localKB.add(m));	
		
		log.info("\n ##running rule specification-capability-not-concretized.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/specification-capability-not-concretized.rq"))
		   .set(q->q.setLocal=true)
		   .set(q->q.setServicePostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
			   }
			   if(tab.isEmpty()){
				   log.info("every dimension is compared by some capability of " + processInd);
			   }
			   else{
				   log.info("The following specifications of " + featureSpec + " have no corresponding capability for " + processInd);
				   tab.rows().forEachRemaining(r->{
					   log.info("Dimension " +  r.get(Var.alloc("d")).getLocalName() + "(" +  r.get(Var.alloc("dimType")).getLocalName() + ")");
				   });
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .set(m->localKB.add(m));	

	}	
	
	private void postProcessing() {
		//create an intermediate feature (only when at least one dimension spec is concretized)
		log.info("\n ##running rule create-interm-feature.rq... ");
		Uni.of(FunQL::new)
			.set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
			.set(q->q.addABox(localKB)) 
			.set(q->q.addPlan("resources/META-INF/rules/core/create-interm-feature.rq"))
			.set(q->q.setLocal=true)
			.set(q->q.setServicePostProcess(tab->{
				if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
					if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
				}
				if(tab.isEmpty()){
					log.info("No intermediate feature is created because all specifications are satisfied by " + processInd);
				}
				else{
					log.info("Intermediate feature " +  tab.rows().next().get(Var.alloc("f1")).getLocalName() + " is created as some specifications are satisfied by " + processInd);
				}
				return tab;
			}))
			.map(q->q.execute())
			.map(q->q.getBelief())
			.map(b->b.getLocalABox())
			.onFailure(e->log.error(e.getMessage()))
			.set(m->localKB.add(m))
			.set(m->GlobalKnowledge.getCurrentPart().add(m));

		//save the intermediate RDF for bug fixing
//		try {
//			GlobalKnowledge.getCurrentPart().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-match-interm.rdf")), "RDF/XML");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		log.info("\n ##running rule create-final-feature.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create-final-feature.rq"))
		   .set(q->q.setLocal=true)
		   .set(q->q.setServicePostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
			   }
			   if(tab.isEmpty()){
				   log.info("No final feature is created as some specifications are not satisfied by " + processInd);
			   }
			   else{
				   log.info("Final feature " +  tab.rows().next().get(Var.alloc("f1")).getLocalName() + " is created as all specifications are satisfied by "+ processInd);
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .set(m->localKB.add(m))
		   .set(m->GlobalKnowledge.getCurrentPart().add(m));
		
		//save the intermediate RDF for bug fixing
//		try {
//			GlobalKnowledge.getCurrentPart().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-match-final.rdf")), "RDF/XML");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		log.info("\n ##running rule create-unsatisfied-feature.rq... ");
		Uni.of(FunQL::new)
			   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
			   .set(q->q.addABox(localKB)) 
			   .set(q->q.addPlan("resources/META-INF/rules/core/create-unsatisfied-feature.rq"))
			   .set(q->q.setLocal=true)
			   .set(q->q.setServicePostProcess(tab->{
				   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
					   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());				   
				   }
				   if(tab.isEmpty()){
					   log.info("No unsatisfied feature is created as either all or some specifications are satisfied by " + processInd);
				   }
				   else{
					   log.info("Unsatisfied feature " +  tab.rows().next().get(Var.alloc("f1")).getLocalName() + " is created as no specification is satisfied by " + processInd);
				   }
				   return tab;
			   }))
			   .map(q->q.execute())
			   .map(q->q.getBelief())
			   .map(b->b.getLocalABox())
			   .onFailure(e->log.error(e.getMessage()))
			   .set(m->localKB.add(m))
			   .set(m->GlobalKnowledge.getCurrentPart().add(m));		

		
		//save the intermediate RDF for bug fixing
//		try {
//			GlobalKnowledge.getCurrentPart().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-match-unsat.rdf")), "RDF/XML");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		//assert dimensions to intermediate feature 
		log.info("\n ##running rule create-interm-feature-dimensions.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create-interm-feature-dimensions.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->log.error(e.getMessage()))
		   .set(m->localKB.add(m))
		   .set(m->GlobalKnowledge.getCurrentPart().add(m));
		
		//save the intermediate RDF for bug fixing
//		try {
//			GlobalKnowledge.getCurrentPart().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-match-dim.rdf")), "RDF/XML");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public Double matchSpecCapMeasure(Double dim, Double max, Double min) throws Exception{
		if( min <= dim && dim <= max ){
			return dim;
		}
		throw new Exception("Not matched");
	}
	
	public Double calculateEquationCapability(String eq, String args) throws Exception{		
		try {
			List<Double> arguments =
			Omni.of(args.split(" "))
				.map(a->Double.parseDouble(a))
				.toList();
			
			for(int i=0; i<arguments.size(); i++){
				int j = i + 1;
				if(eq.contains("?arg"+j)){
					eq = eq.replace("?arg"+j, String.valueOf(arguments.get(i)));
				}
			}
			Rete r = new Rete();
			Value v = r.eval(eq);
			return v.floatValue(r.getGlobalContext());
		} catch (Exception e) {
			throw e;
		}
	}

	public static void main(String[] args) {
		ProcessPlanningKnowledge ppk = new ProcessPlanningKnowledge();
		Model pp = ppk.processPlanningKnowledge1();
		try {
			pp.write(new FileOutputStream(new File("C:/Users/sarkara1/git/simplanner/resources/META-INF/kb/pp1.owl")), "RDF/XML");
			pp.write(System.out, "NTRIPLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
