package edu.ohiou.mfgresearch.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.graph.Node;
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
	PropertyReader prop = new PropertyReader();
	
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
		log.info("load specifications for feature "+ featureIRI.getURI());

		//load specifications for the given feature
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
		   .set(q->q.addABox(GlobalKnowledge.getPart()))
		   //.set(q->saveKB("FPM-local-transfer-feature-specification-before.rdf", q.getBelief().getaBox()))
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-feature-specifications.rq"))
		   .set(q->q.getPlan(0).addVarBinding("f1", ResourceFactory.createResource(featureIRI.getURI()))) //bind the last intermediate feature (featureIRi) to filter out every dimension which are already matched
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m))
		   ;
		reloadKB("FPM-local-transfer-feature-specification.rdf");
	}
	
	/**
	 * event to update capability
	 */
	public void loadCapability(Node processIRI){

		log.info("load capability for process "+ processIRI.getURI());
		//load capability with both max and min as equation
		log.info("running rule transfer-capability-measure.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(prop.getIRIPath(IMPM.capability_IMPM))) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-measure.rq"))
		   .set(q->q.getPlan(0).addVarBinding("p", ResourceFactory.createResource(processIRI.getURI())))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		//load capability with max as measurement and min as equation
		log.info("running rule transfer-capability-measure-equation.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(prop.getIRIPath(IMPM.capability_IMPM))) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-measure-equation.rq"))
		   .set(q->q.getPlan(0).addVarBinding("p", ResourceFactory.createResource(processIRI.getURI())))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		//load capability with max as equation and min as measurement
		log.info("running rule transfer-capability-equation-measure.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(prop.getIRIPath(IMPM.capability_IMPM))) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-equation-measure.rq"))
		   .set(q->q.getPlan(0).addVarBinding("p", ResourceFactory.createResource(processIRI.getURI())))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		reloadKB("FPM-local-transfer-capability.rdf");
	}	
	
	/**
	 * Service to post feature specification
	 * 
	 * @param featureIRI the intermediate feature IRI
	 * @param processType type of process to be matched
	 * @return process IRIs generated for each function (capability profile) matched 
	 */
	public static Node ask_to_match(Node featureIRI, Node processIRI){
		
		log.info("Testing whether feature " + featureIRI.getURI() + " matches process " + processIRI.getURI() + "|\n");
		
		FeatureProcessMatching matching = new FeatureProcessMatching(new String[]{});
		
		//load localKB 
		matching.loadSpecifications(featureIRI);
		matching.loadCapability(processIRI);
		
		//perform core rules
		matching.execute();
		
		//perform updates to global KB
		matching.postProcessing();
		
		//select result of execution
		return matching.selectResult(featureIRI); 
	} 

	private Node selectResult(Node featureIRI) {
		log.info("select intermediate feature to return.");
		
		log.info("running rule mark-unsatisfied-feature.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/mark-unsatisfied-feature.rq"))
		   .set(q->q.getPlan(0).addVarBinding("f1", ResourceFactory.createResource(featureIRI.getURI())))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .set(m->localKB.add(m))
		   .set(m->GlobalKnowledge.appendPartKB(m));
		
		List<Node> intermFeatures = new LinkedList<Node>();
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.mfg_plan)))
		   .set(q->q.addABox(localKB))
		   .set(q->q.addPlan("resources/META-INF/rules/core/select_interim_feature.rq"))
		   .set(q->q.setSelectPostProcess(t->{
			   t.rows().forEachRemaining(b->{
				   intermFeatures.add(b.get(Var.alloc("f1")));
			   });
			   return t;
		   }))
		   .map(q->q.execute());
		
		return intermFeatures.size()>0?intermFeatures.get(0):null;
	}

	public void execute(){
		
		//first rule: assign ibe of specifications to corresponding argument ICE by matching argument type with specification type
		//localKB.write(System.out, "NTRIPLE");
		log.info("running rule transform-capability-equation1... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation1.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		//second rule: assign concatenated argument sepcifications to equations ICE with is_tokenized_by
		//localKB.write(System.out, "NTRIPLE");
		log.info("running rule transform-capability-equation2... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation2.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		//second rule: assign concatenated argument sepcifications to equations ICE with is_tokenized_by
		//localKB.write(System.out, "NTRIPLE");
		log.info("running rule transform-capability-equation3... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation3.rq", this))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		reloadKB("before-match1.rdf");
		
		//third rule: specification-capability matching for max and min both measurement type
		log.info("running rule specification-capability-matching-limit.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/specification-capability-matching-limit.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .set(m->localKB.add(m));		
		
		log.info("running rule specification-capability-not-matching-limit.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/specification-capability-not-matching-limit.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .set(m->localKB.add(m));	

	}	
	
	private void postProcessing() {
		//create an intermediate feature (only when at least one dimension spec is concretized)
		log.info("running rule create-interm-feature.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create-interm-feature.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .set(m->localKB.add(m))
		   .set(m->GlobalKnowledge.appendPartKB(m));
		
		log.info("running rule create-final-feature.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create-final-feature.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .set(m->localKB.add(m))
		   .set(m->GlobalKnowledge.appendPartKB(m));
		
		//assert dimensions to intermediate feature 
		log.info("running rule create-interm-feature-dimensions.rq... ");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create-interm-feature-dimensions.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .set(m->localKB.add(m))
		   .set(m->GlobalKnowledge.appendPartKB(m));
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
