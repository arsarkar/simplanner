package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.plan.PlanUtil;

public class TestPlanCreator {

	Model planKB;
	Map<String, String> pNodes = new HashMap<String, String>();
	Map<String, String> fNodes = new HashMap<String, String>();
	
	public TestPlanCreator(String rootFeatureURI) {
		System.out.println("Feature Precedence is loaded");
		planKB = ModelFactory.createDefaultModel();
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(createInitialPlanPattern());
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(planKB);
		String iProessString =  IMPM.plan_ins + "IProcess" + IMPM.newHash(6);
		pNodes.put("IProcess", iProessString);
		Binding b = BindingFactory.binding(Var.alloc("p"), NodeFactory.createURI(iProessString));
		expander.andThen(updater).apply(Uni.of(TableFactory.create()).set(t->t.addBinding(b)).get());	
		System.out.println("Initial plan is loaded");
		
		Function<Table, BasicPattern> expander1 = IPlanner.createPatternExpander(createFeatureAssertionPattern());
		Function<BasicPattern, BasicPattern> updater1 = IPlanner.createUpdateExecutor(planKB);
		Binding b3 = BindingFactory.binding(Var.alloc("p"), NodeFactory.createURI(iProessString));
		Binding b4 = BindingFactory.binding(b3, Var.alloc("r"), NodeFactory.createURI(rootFeatureURI));
		String fString =  IMPM.design_ins + "feature" + "_" + IMPM.newHash(6);
		fNodes.put(rootFeatureURI, fString);
		Binding b5 = BindingFactory.binding(b4, Var.alloc("f"), NodeFactory.createURI(fString));
		expander1.andThen(updater1)
				.apply(Uni.of(TableFactory.create())
				.set(t->t.addBinding(b5))
				.get());
	}

	private static BasicPattern createInitialPlanPattern(){
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
	
	private static BasicPattern createProcessNodePattern(){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("plan", IMPM.mfg_plan))
			   .set(b->b.addConstruct("plan:precedes", "rdf:type", "owl:ObjectProperty"))	
			   .set(b->b.addConstruct("?p2", "rdf:type", "plan:PlannedProcess"))
			   .set(b->b.addConstruct("?p1", "plan:precedes", "?p2"))		   
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}
	
	private static BasicPattern createFeatureAssertionPattern(){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("plan", IMPM.mfg_plan))
			   .set(b->b.addPrefix("design", IMPM.design))
			   .set(b->b.addConstruct("cco:has_output", "rdf:type", "owl:ObjectProperty"))	
			   .set(b->b.addConstruct("cco:specified_by", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("?f", "rdf:type", "design:FormFeature"))
			   .set(b->b.addConstruct("?r", "rdf:type", "design:FeatureRepresentation"))
			   .set(b->b.addConstruct("?p", "cco:has_output", "?f"))
			   .set(b->b.addConstruct("?r", "cco:specified_by", "?f"))		   
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}
	
	public void addProcessToPlan(String source, String target){
		Node sNode, tNode;
		
		if(pNodes.containsKey(source)){
			sNode = NodeFactory.createURI(pNodes.get(source));
		}
		else{
			System.out.println("source node doesn't exit!");
			return;
		}
		
		if(pNodes.containsKey(target)){
			tNode = NodeFactory.createURI(pNodes.get(target));
		}
		else{
			String pString =  IMPM.plan_ins + target + "_" + IMPM.newHash(6);
			pNodes.put(target, pString);
			tNode = NodeFactory.createURI(pString);
		}
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(createProcessNodePattern());
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(planKB);
		Binding b1 = BindingFactory.binding(Var.alloc("p1"), sNode);
		Binding b2 = BindingFactory.binding(b1, Var.alloc("p2"), tNode);
		expander.andThen(updater)
				.apply(Uni.of(TableFactory.create())
				.set(t->t.addBinding(b2))
				.get());	
		System.out.println(tNode.toString() + " is added to "+ sNode.toString());	
	}
	
	public void addProcessToPlan(String source, String target, String featureURI){
		Node sNode, tNode;
		
		if(pNodes.containsKey(source)){
			sNode = NodeFactory.createURI(pNodes.get(source));
		}
		else{
			System.out.println("source node doesn't exit!");
			return;
		}
		
		if(pNodes.containsKey(target)){
			tNode = NodeFactory.createURI(pNodes.get(target));
		}
		else{
			String pString =  IMPM.plan_ins + target + "_" + IMPM.newHash(6);
			pNodes.put(target, pString);
			tNode = NodeFactory.createURI(pString);
		}
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(createProcessNodePattern());
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(planKB);
		Binding b1 = BindingFactory.binding(Var.alloc("p1"), sNode);
		Binding b2 = BindingFactory.binding(b1, Var.alloc("p2"), tNode);
		expander.andThen(updater)
				.apply(Uni.of(TableFactory.create())
				.set(t->t.addBinding(b2))
				.get());	
		System.out.println(tNode.toString() + " is added to "+ sNode.toString());
		
		Function<Table, BasicPattern> expander1 = IPlanner.createPatternExpander(createFeatureAssertionPattern());
		Function<BasicPattern, BasicPattern> updater1 = IPlanner.createUpdateExecutor(planKB);
		Binding b3 = BindingFactory.binding(Var.alloc("p"), tNode);
		Binding b4 = BindingFactory.binding(b3, Var.alloc("r"), NodeFactory.createURI(featureURI));
		
		String fString = "";
		if(fNodes.containsKey(featureURI)){
			fString = fNodes.get(featureURI);
		}
		else{
			fString = IMPM.design_ins + "feature" + "_" + IMPM.newHash(4);
			fNodes.put(featureURI, fString);
		}
		Binding b5 = BindingFactory.binding(b4, Var.alloc("f"), NodeFactory.createURI(fString));
		expander1.andThen(updater1)
				.apply(Uni.of(TableFactory.create())
				.set(t->t.addBinding(b5))
				.get());
	}
	
	public void savePlan(String path){
		try {
			planKB.write(new FileOutputStream(new File(path)), "RDF/XML");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void savePlanWithPart(String path, Model partKB){
		try {
			planKB.add(partKB).write(new FileOutputStream(new File(path)), "RDF/XML");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
