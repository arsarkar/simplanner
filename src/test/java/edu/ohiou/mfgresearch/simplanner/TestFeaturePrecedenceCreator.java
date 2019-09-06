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

public class TestFeaturePrecedenceCreator {

	Model partKB;
	Map<String, String> fNodes = new HashMap<String, String>();
	
	public TestFeaturePrecedenceCreator() {
		partKB = ModelFactory.createDefaultModel();
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(createInitialPlanPattern());
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(partKB);
		String iFeatureString =  IMPM.plan_ins + "IFeature" + IMPM.newHash(4);
		fNodes.put("IFeature", iFeatureString);
		Binding b = BindingFactory.binding(Var.alloc("f"), NodeFactory.createURI(iFeatureString));
		expander.andThen(updater).apply(Uni.of(TableFactory.create()).set(t->t.addBinding(b)).get());	
		System.out.println("Initial feature node is loaded");
	}

	private static BasicPattern createInitialPlanPattern(){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("design", IMPM.design))
			   .set(b->b.addConstruct("?f", "rdf:type", "design:RootFeatureRepresentation"))	   
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
			   .set(b->b.addPrefix("design", IMPM.design))
			   .set(b->b.addConstruct("plan:hasSucceedingFeature", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("?f2", "rdf:type", "design:FeatureRepresentation"))
			   .set(b->b.addConstruct("?f1", "plan:hasSucceedingFeature", "?f2"))		   
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}
	
	public void addFeatureToPrecedence(String source, String target){
		Node sNode, tNode;
		
		if(fNodes.containsKey(source)){
			sNode = NodeFactory.createURI(fNodes.get(source));
		}
		else{
			System.out.println("source node doesn't exit!");
			return;
		}
		
		if(fNodes.containsKey(target)){
			tNode = NodeFactory.createURI(fNodes.get(target));
		}
		else{
			String fString =  IMPM.plan_ins + target + "_" + IMPM.newHash(4);
			fNodes.put(target, fString);
			tNode = NodeFactory.createURI(fString);
		}
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(createProcessNodePattern());
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(partKB);
		Binding b1 = BindingFactory.binding(Var.alloc("f1"), sNode);
		Binding b2 = BindingFactory.binding(b1, Var.alloc("f2"), tNode);
		expander.andThen(updater)
				.apply(Uni.of(TableFactory.create())
				.set(t->t.addBinding(b2))
				.get());	
		System.out.println(tNode.toString() + " is added to "+ sNode.toString());
	}
	
	public String getFeature(String featureName){
		if(!fNodes.containsKey(featureName)){
			System.out.println("Feature not available");
			return "";
		}
		return fNodes.get(featureName);
	}
	
	public void savePart(String path){
		try {
			partKB.write(new FileOutputStream(new File(path)), "RDF/XML");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
