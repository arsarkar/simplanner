package edu.ohiou.mfgresearch.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.labimp.graphmodel.gui.PlanarLevelLayouter;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;

/**
 * This class is a place holder for distributed knowledg base. 
 * For now a static global knowledge is maintained for the entire process selection
 * @author sarkara1
 * 
 */
public class GlobalKnowledge {

	static Logger log = LoggerFactory.getLogger(GlobalKnowledge.class);
	
	private static GlobalKnowledge KB;
	private Model specificationKB;
	private Model capabilityKB;
	private Model partKB;
	private Model currentPartKB;
	private Model currentPlanKB;
	private Model planKB;
	private Map<Node, Model> planArchive = new HashMap<Node, Model>();
	private Map<Node, Model> partArchive = new HashMap<Node, Model>();
	private static OntModel designTBox = null;
	private static OntModel resourceTBox = null;
	private static OntModel planTBox = null;
	private static PropertyReader prop = PropertyReader.getProperty();
	
	private GlobalKnowledge() {
	}

	public static OntModel getDesignTBox(){
		if(designTBox == null){
			designTBox = ModelFactory.createOntologyModel();
			designTBox.read(prop.getIRIPath(IMPM.design));
		}
		return designTBox;
	}
	
	public static OntModel getResourceTBox(){
		if(resourceTBox == null){
			resourceTBox = ModelFactory.createOntologyModel();
			resourceTBox.read(prop.getIRIPath(IMPM.capability));
		}
		return resourceTBox;
	}
	
	public static Model getCapabilityABox(){
		load();
		if(KB.capabilityKB==null){
			KB.capabilityKB = ModelFactory.createDefaultModel().read(prop.getIRIPath(IMPM.capability_IMPM));
		}
		return KB.capabilityKB;
	}	
	
	public static OntModel getPlanTBox(){
		if(planTBox == null){
			planTBox = ModelFactory.createOntologyModel();
			planTBox.read(prop.getIRIPath(IMPM.mfg_plan));
		}
		return planTBox;
	}
	
	public static Model getSpecification(){
		load();
		return KB.specificationKB;
	}
	
	public static Model getPart(){
		load();
		return KB.partKB;
	}
	
	public static Model getCurrentPart(){
		load();
		if(KB.currentPartKB==null){
			KB.currentPartKB = ModelFactory.createDefaultModel();
		}
		return KB.currentPartKB;
	}
	
	public static void refreshCurrentPart(){
		load();
		if(KB.partKB==null){
			setPart();
		}
		KB.partKB.add(KB.currentPartKB);
		KB.currentPartKB = null;
	}
	
	public static void memoizeCurrentPart(Node featureSpec){
		load();
		KB.partArchive.put(featureSpec, KB.currentPartKB);
	}
	
	public static Model retrieveCurrentPart(Node featureSpec){
		load();
		if(KB.partArchive.containsKey(featureSpec)) return KB.partArchive.get(featureSpec);
		else return null;
	}
	
	public static Model getCurrentPlan(){
		load();
		if(KB.currentPlanKB==null){
			KB.currentPlanKB = ModelFactory.createDefaultModel();
		}
		return KB.currentPlanKB;
	}
	
	public static void refreshCurrentPlan(){
		load();
		if(KB.planKB==null){
			setPlan();
		}
		KB.planKB.add(KB.currentPlanKB);
		KB.currentPlanKB = null;
	}
	
	public static void memoizeCurrentPlan(Node featureSpec){
		load();
		KB.planArchive.put(featureSpec, KB.currentPlanKB);
	}
	
	public static Model retrieveCurrentPlan(Node featureSpec){
		load();
		if(KB.planArchive.containsKey(featureSpec)) return KB.planArchive.get(featureSpec);
		else return null;
	}
	
	public static void setPart(){
		load();
		KB.partKB = ModelFactory.createDefaultModel();
	}
	
	public static Model getPlan(){
		load();
		return KB.planKB;
	}
	
	public static void setPlan(){
		load();
		KB.planKB = ModelFactory.createDefaultModel();
	}
	
	public static void appendPartKB(Model m){
		KB.partKB.add(m);
	}
	
	public static void appendPlanKB(Model m){
		KB.planKB.add(m);
	}
	
	public static void loadSpecification(String url){
		load();
		KB.specificationKB = ModelFactory.createDefaultModel().read(url);
		System.out.println("Specification is loaded onto global knowledge base from "+url.toString());
	}
	
	public static void loadSpecification(Model m){
		load();
		KB.specificationKB = m;
		System.out.println("Specification is loaded onto global knowledge base.");
	}
	
	public static void appendSpecificationKB(Model m){
		KB.specificationKB.add(m);
	}	
	
	private static BasicPattern createPatternInitialPlan(){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("plan", IMPM.mfg_plan))
			   .set(b->b.addConstruct("?p", "rdf:type", "plan:RootProcess"))	
			   .set(b->b.addConstruct("?p", "rdf:type", "plan:PlannedProcess"))		   
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}
	
	public static Node loadInitialPlan(){
		load();
		if(KB.planKB == null){
			KB.planKB = ModelFactory.createDefaultModel();
		}
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(createPatternInitialPlan());
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(KB.planKB);
		String iProessString =  IMPM.plan_ins + "IProcess" + IMPM.newHash(4);
		Node iProcess = NodeFactory.createURI(iProessString);
		Binding b = BindingFactory.binding(Var.alloc("p"), iProcess);
		expander.andThen(updater).apply(Uni.of(TableFactory.create()).set(t->t.addBinding(b)).get());
		log.info("Initial plan is loaded onto global knowledge base with root occurrence " + iProessString);
		return iProcess;
	}

	public static Node loadRootFeature(){
		load();
		if(KB.partKB == null){
			KB.partKB = ModelFactory.createDefaultModel();
		}
		List<Node> rootFeature = new LinkedList<Node>();
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addABox(KB.specificationKB)) 
		   .set(q->q.addABox(KB.planKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create-root-feature.rq"))
		   .set(q->q.setLocal=true)
		   .set(q->q.setServicePostProcess(tab->{
			   if(!tab.isEmpty()){
				   log.info("Root feature " + tab.rows().next().get(Var.alloc("f0")).getLocalName() + " specifying representation " + 
						   		tab.rows().next().get(Var.alloc("r0")).getLocalName() + " is created as output of root process " + 
						   		tab.rows().next().get(Var.alloc("p0")).getLocalName());
				   rootFeature.add(tab.rows().next().get(Var.alloc("f0")));
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->KB.partKB.add(m));
		return rootFeature.get(0);
	}
	
	/**
	 * Load the stock feature, only creates a FormFeature and ICE1 which points to the type and label bearing entity 
	 * of the specification 
	 * @param featureName
	 */
	public static void loadStockFeature(String featureName){
		load();
		if(KB.partKB == null){
			setPart();
		}
		//two queries are required to insert the assertions in different KB
		//create the stock feature and save to the specification KB
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addABox(KB.specificationKB)) 
//		   .set(q->q.addABox(KB.planKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create_stock_feature2.rq"))
		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createPlainLiteral(featureName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->KB.partKB.add(m));
		
		if(KB.planKB == null){
			KB.planKB = ModelFactory.createDefaultModel();
		}
		//assert the stock feature is output of the root planned process
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addABox(KB.specificationKB)) 
//		   .set(q->q.addABox(KB.planKB))  
		   .set(q->q.addABox(KB.partKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create_stock_feature1.rq"))
		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createPlainLiteral(featureName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->KB.planKB.add(m));
	}
	
	private static void load() {
		if(KB==null){
			KB = new GlobalKnowledge();
		}
	}
	
	
}
