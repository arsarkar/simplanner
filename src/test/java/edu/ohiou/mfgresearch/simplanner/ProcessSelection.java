package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.services.GlobalKnowledge;
import edu.ohiou.mfgresearch.services.PartProcessSelection;
import edu.ohiou.mfgresearch.services.FeatureProcessMatching;
import edu.ohiou.mfgresearch.services.FeatureProcessSelection;

public class ProcessSelection {
	
	PropertyReader prop;
	
	@Before
    public void beforeEachTestMethod() {
        prop = PropertyReader.getProperty();
    }
	
	@Test
	public void parseBooleanTest(){
		System.out.println(IMPM.getUnit("mm"));
		System.out.println(IMPM.getUnit("cm"));
		
        prop = PropertyReader.getProperty();
        System.out.println(prop.getProperty("SHOW_PROCESS_GRAPH").toCharArray());
        System.out.println(prop.getProperty("SHOW_PROCESS_GRAPH").toLowerCase().hashCode());
        System.out.println(new String("true").hashCode());
        System.out.println(prop.getProperty("SHOW_PROCESS_GRAPH").toLowerCase().trim().equals("true"));
        System.out.println(Boolean.parseBoolean(prop.getProperty("SHOW_PROCESS_GRAPH").toString()));
		System.out.println(Boolean.parseBoolean("true"));

	}
	
	@Test
	public void holeTypeFeatureSelection() {
		
		//get the feature specification which are not matched till now
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(prop.getProperty("DESIGN_PART_ABOX")))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-feature-type-hole.q"))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;
		
	}

	@Test
	public void FeatureSelection1() { 
		
		//get the feature specification which are not matched till now
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(prop.getProperty("DESIGN_PART_ABOX")))
		   .set(q->q.addPlan("resources/META-INF/rules/test/feature-specification1.rq"))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;		
	}
	
	@Test
	public void FeatureSelection2() { 
		
		GlobalKnowledge.loadSpecification(prop.getProperty("DESIGN_PART_ABOX"));
		
		//get the feature specification which are not matched till now
		Model m =
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
				   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-feature-specifications.rq"))
				   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createStringLiteral("SIMPLE HOLE(4)")))
				   .set(q->q.setLocal=true)
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getLocalABox())
				   .onFailure(e->e.printStackTrace(System.out))
				   .get()
				   ;
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/test/feature-specification.rq"))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;
//		m.write(System.out, "TURTLE");
		
	}
	
	@Test
	public void capabilitySelection() {		
		
		//get the feature specification which are not matched till now
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
				   .set(q->q.addABox(prop.getProperty("PROCESS_XML")))
				   .set(q->q.addPlan("resources/META-INF/rules/test/capability-measure-selection1.rq"))
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getaBox())
				   .onFailure(e->e.printStackTrace(System.out))
				   ;
		
	}
	
	@Test
	public void capabilityEquationSelection() {		
		Model m =
		Uni.of(FunQL::new)
		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
		   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl")) //capability repository 
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/test/capability-equation-selection1.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pType", ResourceFactory.createResource("http://www.ohio.edu/ontologies/manufacturing-capability#TwistDrilling")))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get()
		   ;		
	}
	
	@Test
	public void capabilityEquationSelection1() {		
		
		//get the feature specification which are not matched till now
		FunQL ql =		
		Uni.of(FunQL::new)
				   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
				   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl")) //capability repository 
				   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/test/transfer-capability-equation.rq"))
				   .onFailure(e->e.printStackTrace(System.out))
				   .get()
				   ;	
		Model abox = ql.getBelief().getaBox();
		//display the result, should come from visualization package
		Function<Table, String> display = tab->{
			System.out.println(tab.toString());
			return "";
		};
		
		Query q = ql.getPlans().get(0).getQuery();
		
		QueryExecution exec = QueryExecutionFactory.create(q, abox);
		Iterator<Triple> m = exec.execConstructTriples();
		m.forEachRemaining(t->System.out.println(t.toString()));
	}
	
	@Test
	public void capabilityEquationTransform1() {		
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
		   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl")) //capability repository 
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/core/transform-capability-equation.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pType", ResourceFactory.createResource("http://www.ohio.edu/ontologies/manufacturing-capability#TwistDrilling")))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
//		   .onSuccess(m->localKB.add(m))
		   ;
	}
	
	@Test
	public void capabilitySelectionUnion() {		
		
		//get the feature specification which are not matched till now
		FunQL ql =		
		Uni.of(FunQL::new)
				   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
				   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl"))
				   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/test/capability-measure-selection1.rq"))
				   .get()
				   ;	
		Model abox = ql.getBelief().getaBox();
		//display the result, should come from visualization package
		Function<Table, String> display = tab->{
			System.out.println(tab.toString());
			return "";
		};
		
		Query q = ql.getPlans().get(0).getQuery();
		QuerySolutionMap binds = new QuerySolutionMap();
		binds.add("pType", abox.getResource("http://www.ohio.edu/ontologies/manufacturing-capability#TwistDrilling"));
		binds.add("pType", abox.getResource("http://www.ohio.edu/ontologies/manufacturing-capability#EndDrilling"));
		Function<Query, Table> queryRes = IPlanner.createQueryExecutorWithBind(abox, binds);
		queryRes.andThen(display).apply(ql.getPlans().get(0).getQuery());
	}
	
	@Test
	public void specificationCapabilitySelection() {		
		
		//get the feature specification which are not matched till now
		Uni.of(FunQL::new)
		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
		   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/product-model/aboxes/simple1.rdf"))
		   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl"))
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/test/specification-capability-selection1.rq"))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;		
	}
	
	@Test
	public void holedrillMatching1(){
		
		GlobalKnowledge.loadSpecification(PropertyReader.getProperty().getProperty("DESIGN_PART_ABOX"));
		
		FeatureProcessMatching hs = new FeatureProcessMatching(new String[]{});
		
		hs.loadSpecifications(ResourceFactory.createResource("http://www.ohio.edu/simplanner/design2019/5/2/724804#FormFeature_I3443").asNode());
		hs.loadCapability(ResourceFactory.createResource("http://www.ohio.edu/ontologies/capability-implanner#twistdrilling0101").asNode());
		
		System.out.println("---------------------------------------------------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------------------------------------------------");
		
		hs.execute();				
		
//		try {
//			hs.getLocalKB().write(new FileOutputStream(new File("C:/Users/sarkara1/git/SIMPOM/impm-ind/plan/plan1.rdf")), "RDF/XML");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		Uni.of(FunQL::new)
//		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
////		   .set(q->q.addABox(ModelFactory.createDefaultModel().read("C:/Users/sarkara1/git/SIMPOM/impm-ind/plan/plan1.rdf")))
//		   .set(q->q.addABox(hs.getLocalKB()))
//		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/test/feature-specification.rq"))
//		   .map(q->q.execute())
//		   .map(q->q.getBelief())
//		   .map(b->b.getaBox())
//		   .onFailure(e->e.printStackTrace(System.out));		
		
	}
	
	@Test
	public void loadStockFeatures(){
		GlobalKnowledge.loadSpecification(PropertyReader.getProperty().getProperty("DESIGN_PART_ABOX"));
//		GlobalKnowledge.loadInitialPlan();
		GlobalKnowledge.loadStockFeature("SIMPLE HOLE(4)");
		GlobalKnowledge.loadStockFeature("RECTANGULAR_SLOT(7)");
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
		   .set(q->q.addABox(GlobalKnowledge.getPart()))
		   .set(q->q.addABox(GlobalKnowledge.getPlan()))
		   .set(q->q.addPlan("resources/META-INF/rules/test/select-stock-feature.rq"))
		   .set(q->q.setSelectPostProcess(tab->{
			   ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   return tab;
		   }))
		   .map(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));
	}
	
	@Test
	public void processSelectionHole(){
		GlobalKnowledge.loadSpecification(PropertyReader.getProperty().getProperty("DESIGN_PART_ABOX"));
		//GlobalKnowledge.loadInitialPlan();
//		GlobalKnowledge.loadStockFeature("SIMPLE HOLE(4)");
		
		FeatureProcessSelection selection = new FeatureProcessSelection(new String[]{});
		
		Node[] roots = FeatureProcessSelection.ask_to_select_holemaking_processes(ResourceFactory.createResource("http://www.ohio.edu/simplanner/design2019/9/9/488500#FeatureSpecification_I7732").asNode());
		
		System.out.println("First process of alternative routes are...");
		for(Node n:roots){
			System.out.println(n.toString());
		}
		
		try {
			GlobalKnowledge.getPlan().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-int-3.rdf")), "RDF/XML");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	@Test
	public void processSelectionSlot(){
		GlobalKnowledge.loadSpecification(PropertyReader.getProperty().getProperty("DESIGN_PART_ABOX"));
		
		//GlobalKnowledge.loadInitialPlan();
//		GlobalKnowledge.loadStockFeature("RECTANGULAR_SLOT(7)");
		
		FeatureProcessSelection selection = new FeatureProcessSelection(new String[]{});
		Node[] roots = FeatureProcessSelection.ask_to_select_open_slotmaking_processes(ResourceFactory.createResource("http://www.ohio.edu/simplanner/design2019/9/9/488500#FeatureSpecification_I5849").asNode());
		
		System.out.println("First process of alternative routes are...");
		for(Node n:roots){
			System.out.println(n.toString());
		}		
		
		try {
			selection.getLocalKB().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-int-rectangular_slot7.rdf")), "RDF/XML");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	@Test
	public void processSelectionPocket(){
		GlobalKnowledge.loadSpecification(PropertyReader.getProperty().getProperty("DESIGN_PART_ABOX"));
		
		//GlobalKnowledge.loadInitialPlan();
//		GlobalKnowledge.loadStockFeature("RECTANGULAR_POCKET(3)");
		
		FeatureProcessSelection selection = new FeatureProcessSelection(new String[]{});
		Node[] roots = FeatureProcessSelection.ask_to_select_open_pocketmaking_processes(ResourceFactory.createResource("http://www.ohio.edu/simplanner/design2019/9/9/488500#FeatureSpecification_I1676").asNode());
		
		System.out.println("First process of alternative routes are...");
		for(Node n:roots){
			System.out.println(n.toString());
		}		
		
		try {
			selection.getLocalKB().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-int-rectangular_slot7.rdf")), "RDF/XML");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	@Test
	public void processSelectionSlab(){
		GlobalKnowledge.loadSpecification(PropertyReader.getProperty().getProperty("DESIGN_PART_ABOX"));
		
		//GlobalKnowledge.loadInitialPlan();
//		GlobalKnowledge.loadStockFeature("RECTANGULAR_POCKET(2)");
		
		FeatureProcessSelection selection = new FeatureProcessSelection(new String[]{});
		Node[] roots = FeatureProcessSelection.ask_to_select_slabmaking_processes(ResourceFactory.createResource("http://www.ohio.edu/simplanner/design2019/9/9/488500#FeatureSpecification_I3940").asNode());
		
		System.out.println("First process of alternative routes are...");
		for(Node n:roots){
			System.out.println(n.toString());
		}	
		
		try {
			selection.getLocalKB().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-int-rectangular_slot7.rdf")), "RDF/XML");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	@Test
	public void processPlanPart(){
		GlobalKnowledge.loadSpecification(PropertyReader.getProperty().getProperty("DESIGN_PART_ABOX"));
		
		PartProcessSelection selection = new PartProcessSelection(new String[]{});
		
		selection.ask_to_plan("SimplePart-v2");
		
		try {
			selection.getLocalKB().write(new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-int-2.rdf")), "RDF/XML");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	@Test
	public void modelCloningTest1(){
//		Model planModel = null, partModel;
//		try {
//			planModel = ModelFactory.createDefaultModel().read(new FileInputStream(new File("C:/Users/sarkara1/Ohio University/Sormaz, Dusan - sarkar-shared/dissertation/experiment/simple-slot/plan_FeatureSpecification_I5692.rdf")), "RDF/XML");
//			partModel = ModelFactory.createDefaultModel().read(new FileInputStream(new File("C:/Users/sarkara1/Ohio University/Sormaz, Dusan - sarkar-shared/dissertation/experiment/simple-slot/part_FeatureSpecification_I5692.rdf")), "RDF/XML");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Model clonedPlan = ModelFactory.createDefaultModel();
		Model clonedPart = ModelFactory.createDefaultModel();
		Map<Node, Resource> nodeMap = new HashMap<Node, Resource>();
		
		Function<Node, String> renameNode = n->{
			String ns = n.getNameSpace();
			String name = n.getLocalName();
			String newName = name.replaceFirst("I[0-9]*(?!.*I[0-9]*)", "I"+IMPM.newHash(4));
			return ns+newName;
		};

	   clonedPlan.add(ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/has_input"), 
		   		  		ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
		   		  		ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));
	   clonedPlan.add(ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/has_output"), 
		  		  		ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
		  		  		ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));
	   clonedPlan.add(ResourceFactory.createProperty("http://www.ohio.edu/ontologies/manufacturing-plan#precedes"), 
		  		  		ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
		  		  		ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
		   .set(q->q.addABox(ModelFactory.createDefaultModel().read(new FileInputStream(new File("C:/Users/sarkara1/Ohio University/Sormaz, Dusan - sarkar-shared/dissertation/experiment/simple-slot/plan_FeatureSpecification_I5692.rdf")), "RDF/XML")))
		   .set(q->q.addPlan("resources/META-INF/rules/core/clone-process-individual.rq"))
		   .set(q->q.setSelectPostProcess(t->{
			   ResultSetFormatter.out(System.out, t.toResultSet(), q.getAllPrefixMapping());
			   t.rows().forEachRemaining(r->{
				   //?p1 rdf:type ?pt
				   Resource p1 = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("p1"))));
				   nodeMap.put(r.get(Var.alloc("p1")), p1);
				   clonedPlan.add(p1, 
						   		  ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
						   		  ResourceFactory.createResource(r.get(Var.alloc("pt")).getURI()));
				   //?p0 cco:has_input ?i1
				   Resource i1 = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("i1"))));
				   nodeMap.put(r.get(Var.alloc("i1")), i1);
				   clonedPlan.add(i1, 
					   		  ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
					   		  ResourceFactory.createResource("http://www.ohio.edu/ontologies/design#FormFeature"));
				   clonedPlan.add(p1, 
					   		  ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/has_input"),
					   		  i1);
				   //?p0 cco:has_output ?o1
				   Resource o1 = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("o1"))));
				   nodeMap.put(r.get(Var.alloc("o1")), o1);
				   clonedPlan.add(o1, 
					   		  ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
					   		  ResourceFactory.createResource("http://www.ohio.edu/ontologies/design#FormFeature"));
				   clonedPlan.add(p1, 
					   		  ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/has_output"),
					   		  o1);
			   });
			   t.rows().forEachRemaining(r->{
				   // ?p1	plan:precedes		?p2
				   if(r.get(Var.alloc("p2"))!=null){
					   clonedPlan.add(nodeMap.get(r.get(Var.alloc("p1"))), 
						   		  ResourceFactory.createProperty("http://www.ohio.edu/ontologies/manufacturing-plan#precedes"),
						   		  nodeMap.get(r.get(Var.alloc("p2"))));
				   }
			   });
			   return t;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out));
		
		try {
			clonedPlan.write(new FileOutputStream(new File("C:/Users/sarkara1/Ohio University/Sormaz, Dusan - sarkar-shared/dissertation/experiment/simple-slot/plan_FeatureSpecification_I5692_cloned.rdf")), "RDF/XML");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}
