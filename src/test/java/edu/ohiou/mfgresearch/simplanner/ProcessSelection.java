package edu.ohiou.mfgresearch.simplanner;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.junit.Test;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.services.GlobalKnowledge;
import edu.ohiou.mfgresearch.services.FeatureProcessMatching;

public class ProcessSelection {
	
	@Test
	public void holeTypeFeatureSelection() {
		
		//get the feature specification which are not matched till now
				Uni.of(FunQL::new)
				   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/product-model/design_bfo.owl"))
				   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/product-model/aboxes/simple1.rdf"))
				   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/specification/infer-feature-type-hole.q"))
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
				   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/product-model/design_bfo.owl"))
				   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/product-model/aboxes/simple1.rdf"))
				   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/test/feature-specification1.rq"))
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getaBox())
				   .onFailure(e->e.printStackTrace(System.out))
				   ;
		
	}
	
	@Test
	public void FeatureSelection2() { 
		
		GlobalKnowledge.loadSpecification("C:/Users/sarkara1/git/SIMPOM/product-model/aboxes/simple1.rdf");
		
		//get the feature specification which are not matched till now
		Model m =
				Uni.of(FunQL::new)
				   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/product-model/design_bfo.owl"))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
				   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/core/transfer-feature-specifications.rq"))
				   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createStringLiteral("SIMPLE HOLE(4)")))
				   .set(q->q.setLocal=true)
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getLocalABox())
				   .onFailure(e->e.printStackTrace(System.out))
				   .get()
				   ;
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/product-model/design_bfo.owl"))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/test/feature-specification.rq"))
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
				   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
				   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-mm1.owl"))
				   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/test/capability-measure-selection1.rq"))
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
	public void holedrillselection1(){
		
		GlobalKnowledge.loadSpecification(new PropertyReader().getProperty("DESIGN_PART_XML"));
		
		FeatureProcessMatching hs = new FeatureProcessMatching(new String[]{});
		
		hs.loadSpecifications("SIMPLE HOLE(4)");
		hs.loadCapability("http://www.ohio.edu/ontologies/manufacturing-capability#TwistDrilling");
		
		System.out.println("---------------------------------------------------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------------------------------------------------");
		
		hs.execute("SIMPLE HOLE(4)", "http://www.ohio.edu/ontologies/manufacturing-capability#TwistDrilling");				
		
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
	
	

}
