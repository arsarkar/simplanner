package edu.ohiou.mfgresearch.services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.graph.Node;
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

public class PartProcessSelection {

	static Logger log = LoggerFactory.getLogger(PartProcessSelection.class);
	Model localKB;
	String localPath;
	static PropertyReader prop = PropertyReader.getProperty();
	
	public PartProcessSelection() {
		
	}

	public Model getLocalKB() {
		return localKB;
	}
		
	public PartProcessSelection(String[] localIRI) {
		if(localKB == null) {
			localKB = ModelFactory.createDefaultModel();
			localKB.setNsPrefix("", IMPM.plan_ins);
		}
		if(localIRI.length>0) localIRI = new String[]{localPath};
		Omni.of(localIRI)
			.map(path->localKB.add(ModelFactory.createDefaultModel().read(path)));
		IMPM.clearSessionPath();
		localPath = IMPM.createSessionFolder("");
	}
	
	public static Node createStockFeature(String featureName){
		
		if(GlobalKnowledge.getPart() == null){
			GlobalKnowledge.setPart();
		}
		List<Node> ff = new LinkedList<Node>();
		Function<Table, Table> storeFormFeatureURI = tab->{
			ff.add(tab.rows().next().get(Var.alloc("f")));
			return tab;
		};
		//two queries are required to insert the assertions in different KB
		//create the stock feature and save to the specification KB		
		log.info("Create stock for feature " + featureName + " ----------------------------------------------------->");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create_stock_feature2.rq"))
		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createPlainLiteral(featureName)))
		   .set(q->q.setLocal=true)
		   .set(q->q.setServicePostProcess(storeFormFeatureURI))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->GlobalKnowledge.appendPartKB(m));
		
		if(GlobalKnowledge.getPlan() == null){
			GlobalKnowledge.setPlan();
		}
		
		//assert the stock feature is output of the root planned process (required because two unknown is not supported in FunQL yet)
		log.info("Assert the stock as output of root process ----------------------------------------------------->");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
//		   .set(q->q.addABox(KB.planKB))  
		   .set(q->q.addABox(GlobalKnowledge.getPart())) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create_stock_feature1.rq"))
		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createPlainLiteral(featureName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->GlobalKnowledge.appendPlanKB(m));		
		return ff.get(0);
	}
	
	
	public Node ask_to_plan(String partName){
		
		//read all the features of the part and load stock feature and root process for every feature
		//log.info("Create stock and root process tree for each feature----------------------------------------------------->");
//		Uni.of(FunQL::new)
//		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
//		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
//		   .set(q->q.addPlan("resources/META-INF/rules/core/create-stock-for-features.rq"))
//		   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
//		   .set(q->q.setLocal=true)
//		   .map(q->q.execute())
//		   .map(q->q.getBelief())
//		   .map(b->b.getLocalABox())
//		   .onFailure(e->e.printStackTrace(System.out))
//		   .onSuccess(m->localKB.add(m));

		//Initial plan
		log.info("load initial plan");
		GlobalKnowledge.loadInitialPlan();
		
		//Root Feature
		log.info("load root feature and representation");
		GlobalKnowledge.loadRootFeature();
		
		//transform feature precedence (add root representation)
		log.info("transform feature precedence");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
		   .set(q->q.addABox(GlobalKnowledge.getPlan())) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform_feature_precedence.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->GlobalKnowledge.appendSpecificationKB(m));
		
		//download to test
		Uni.of(GlobalKnowledge.getSpecification())
		   .set(m->m.add(GlobalKnowledge.getPlan()))
		   .set(m->m.write(new FileOutputStream(new File("C:/Users/sarkara1/git/SIMPOM/plan/initial_plan.rdf")), "RDF/XML"));
		
		//plan every feature
		log.info("Create occurrence tree for each feature----------------------------------------------------->");
		boolean stopIteration = false;
		while(!stopIteration){
			boolean	isSuccessful =
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
				   .set(q->q.addABox(GlobalKnowledge.getPlan())) 
				   .set(q->q.addPlan("resources/META-INF/rules/core/feature-precedence-1.rq"))
//				   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
				   .set(q->q.setLocal=true)
				   .map(q->q.execute())
				   .set(q->GlobalKnowledge.appendPlanKB(q.getBelief().getLocalABox()))
				   .map(q->q.isQuerySuccess())
				   .get();	
			stopIteration = !isSuccessful;
		}
		execute();
		return null;
	}
	
	public void createFeaturePrecedenceNetwork(String partName){
		//feature precedence network (add a start node)
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/feature-precedence-0.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));		
	}

	public void execute(){
		
		
		
	}
	
}
