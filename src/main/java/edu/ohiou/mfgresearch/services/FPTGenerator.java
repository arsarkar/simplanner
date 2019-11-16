package edu.ohiou.mfgresearch.services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class FPTGenerator {

static Logger log = LoggerFactory.getLogger(FeatureProcessMatching.class);
	
	Model localKB;
	String localPath;
	PropertyReader prop = PropertyReader.getProperty();
	String partName = "";
	Node unit = null;
	
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
	
	public FPTGenerator(String[] localIRI) {
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
	
	public Node ask_to_create_fpt(String partName){
		
		this.partName = partName;
		//Initial plan
		log.info("load initial root for the part-planning occurrence tree");
		Node iProcess = GlobalKnowledge.loadInitialPlan();
		
		//Root Feature
		log.info("load root feature and representation");
		Node rootFeature = GlobalKnowledge.loadRootFeature();
		
		//transform feature precedence (add root representation)
		log.info("transform feature precedence");
		Uni.of(FunQL::new)
			.set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
			.set(q->q.addABox(GlobalKnowledge.getSpecification())) 
			.set(q->q.addABox(GlobalKnowledge.getPart()))
			.set(q->q.addABox(GlobalKnowledge.getPlan())) 
			.set(q->q.addPlan("resources/META-INF/rules/core/transform_feature_precedence.rq"))
			.set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
			.set(q->q.setLocal=true)
			.set(q->q.setSelectPostProcess(tab->{
				if(!tab.isEmpty()){
					log.info("Root feature is preceding initial features");
				}
				return tab;
			}))
			.set(q->q.setServicePostProcess(tab->{
				if(!tab.isEmpty()){
					
				}
				return tab;
			}))
			.map(q->q.execute())
			.map(q->q.getBelief())
			.map(b->b.getLocalABox())
			.onFailure(e->e.printStackTrace(System.out))
			.onSuccess(m->GlobalKnowledge.appendSpecificationKB(m));
		
		execute();
		return null;
	}
	
	
	public void execute(){
		
		//plan every feature
		log.info("Create feature precedence tree----------------------------------------------------->");
		boolean stopIteration = false;
		int counter = 0;
		while(!stopIteration){
			log.info("Feature precedence tree iteration " + counter);
			
//			//save the intermediate RDF for bug fixing
//			Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/PartProcess_iter_" + counter + "_plan.rdf")
//				.map(File::new)
//				.map(FileOutputStream::new)
//				.set(s->Uni.of(ModelFactory.createDefaultModel())
//							.set(m->m.add(GlobalKnowledge.getPlan()))
//							.set(m->m.write(s, "RDF/XML")))
//				.set(s->s.flush())
//				.set(s->s.close());
//			
//			Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/PartProcess_iter_" + counter + "_part.rdf")
//				.map(File::new)
//				.map(FileOutputStream::new)
//				.set(s->Uni.of(ModelFactory.createDefaultModel())
//							.set(m->m.add(GlobalKnowledge.getPart()))
//							.set(m->m.add(GlobalKnowledge.getSpecification()))
//							.set(m->m.write(s, "RDF/XML")))
//				.set(s->s.flush())
//				.set(s->s.close());
			
			boolean	isSuccessful =
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
				   .set(q->q.addABox(localKB)) 
				   .set(q->q.addPlan("resources/META-INF/rules/core/feature-precedence-0.rq"))
//				   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
				   .set(q->q.setLocal=true)
				   .set(q->q.setSelectPostProcess(tab->{
					   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
						   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
					   }
					   if(!tab.isEmpty()){
						   log.info("-----------------------------------------------------------------------------------------------------------------------");
//						   tab.rows().forEachRemaining(r->{
//							   log.info("After occurrent occurrences for " + r.get(Var.alloc("pCurrent")).getLocalName() + " occurrence sub tree for feature specification "+
//									   r.get(Var.alloc("fs")).getLocalName() + "(" + r.get(Var.alloc("ft")).getLiteralValue() + ") can be planned");
//						   });
					   }
					   return tab;
				   }))
				   .set(q->q.setServicePostProcess(tab->{
					   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
						   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
					   }
					   if(!tab.isEmpty()){
//						   tab.rows().forEachRemaining(r->{
//							   log.info("\n root of sub-occurrence-tree "+ r.get(Var.alloc("pNext")).getLocalName() + " is added as succeeding occurrence of "+ r.get(Var.alloc("pCurrent")).getLocalName());
//						   });
						   log.info("-----------------------------------------------------------------------------------------------------------------------");
					   }
					   return tab;
				   }))
				   .map(q->q.execute())
				   .set(q->GlobalKnowledge.appendPlanKB(q.getBelief().getLocalABox()))
				   .map(q->q.isQuerySuccess())
				   .get();	
			counter++;
			stopIteration = !isSuccessful;
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
