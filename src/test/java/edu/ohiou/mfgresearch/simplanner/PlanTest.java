package edu.ohiou.mfgresearch.simplanner;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.labimp.graph.GraphViewer;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.services.GlobalKnowledge;
import edu.ohiou.mfgresearch.services.FeatureProcessMatching;

public class PlanTest {
	
	PropertyReader prop;
	
	@Before
    public void beforeEachTestMethod() {
        prop = PropertyReader.getProperty();
    }
	
	@Test
	public void testTransitiveRelationship(){
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.graph)))
		   .set(q->q.addABox(prop.getNS("git1")+"graph/graph5_test1.owl"))
		   .set(q->q.addPlan("resources/META-INF/rules/test/graph_transitivity_test1.q"))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;
		
	}
	
	@Test
	public void testTransitiveRelationship1(){
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.graph)))
		   .set(q->q.addABox(prop.getNS("git1")+"graph/graph5_test3.owl"))
		   .set(q->q.addPlan("resources/META-INF/rules/test/graph_transitivity_test1.q"))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;	
	}
	
	@Test
	public void testTransitiveRelationshipInverse(){
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.graph)))
		   .set(q->q.addABox(prop.getNS("git1")+"graph/graph5_test1.owl"))
		   .set(q->q.addPlan("resources/META-INF/rules/test/graph_transitivity_test11.q"))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;
		
	}
	
	@Test
	public void testEmptyTree(){
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.graph)))
		   .set(q->q.addABox(prop.getNS("git1")+"graph/graph5_test2.owl"))
		   .set(q->q.addPlan("resources/META-INF/rules/test/graph_transitivity_test2.q"))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;		
	}	
	
	@Test
	public void testSiblings(){		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.graph)))
		   .set(q->q.addABox(prop.getNS("git1")+"graph/graph5_test2.owl"))
		   .set(q->q.addPlan("resources/META-INF/rules/test/graph_transitivity_test3.q"))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;		
	}

	@Test
	public void testSiblingAncestor(){		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.graph)))
		   .set(q->q.addABox(prop.getNS("git1")+"graph/graph5_test2.owl"))
		   .set(q->q.addPlan("resources/META-INF/rules/test/graph_transitivity_test3.q"))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));		
	}
	
	@Test
	public void testLeafNode(){		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.graph)))
		   .set(q->q.addABox(prop.getNS("git1")+"graph/graph5_test2.owl"))
		   .set(q->q.addPlan("resources/META-INF/rules/test/graph_transitivity_test4.q"))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));		
	}
	
	@Test
	public void testCreatorTest(){		
		TestPlanCreator planCreator = new TestPlanCreator("");
		planCreator.addProcessToPlan("IProcess", "p1");
		planCreator.addProcessToPlan("IProcess", "p2");
		planCreator.addProcessToPlan("p1", "p3");
		planCreator.addProcessToPlan("p1", "p4");
		planCreator.addProcessToPlan("p2", "P5");
		planCreator.savePlan("C:/Users/sarkara1/git/SIMPOM/plan/plan1.rdf");
		
		TestFeaturePrecedenceCreator fpCreator = new TestFeaturePrecedenceCreator();
		fpCreator.addFeatureToPrecedence("IFeature", "f1");
		fpCreator.addFeatureToPrecedence("IFeature", "f2");
		fpCreator.addFeatureToPrecedence("f1", "f3");
		fpCreator.addFeatureToPrecedence("f2", "f4");
		fpCreator.addFeatureToPrecedence("f3", "f4");
		fpCreator.savePart("C:/Users/sarkara1/git/SIMPOM/plan/part1.rdf");
	}
	
	@Test
	public void testPlan1(){		
		
		TestFeaturePrecedenceCreator fpCreator = new TestFeaturePrecedenceCreator();
		fpCreator.addFeatureToPrecedence("IFeature", "f1");
		fpCreator.addFeatureToPrecedence("IFeature", "f2");
		fpCreator.addFeatureToPrecedence("f1", "f3");
		fpCreator.addFeatureToPrecedence("f2", "f4");
		fpCreator.addFeatureToPrecedence("f3", "f4");
		fpCreator.savePart("C:/Users/sarkara1/git/SIMPOM/plan/part1.rdf");
		
		TestPlanCreator planCreator = new TestPlanCreator(fpCreator.getFeature("IFeature"));
		planCreator.addProcessToPlan("IProcess", "p1", fpCreator.getFeature("f1"));
		planCreator.addProcessToPlan("IProcess", "p2", fpCreator.getFeature("f2"));
		planCreator.addProcessToPlan("p1", "p3", fpCreator.getFeature("f3"));
		planCreator.addProcessToPlan("p1", "p4", fpCreator.getFeature("f4"));
		planCreator.savePlan("C:/Users/sarkara1/git/SIMPOM/plan/plan1.rdf");
	}
	
	@Test
	public void testPlan2(){		
		
		TestFeaturePrecedenceCreator fpCreator = new TestFeaturePrecedenceCreator();
		fpCreator.addFeatureToPrecedence("IFeature", "f1");
		fpCreator.addFeatureToPrecedence("IFeature", "f2");
		fpCreator.addFeatureToPrecedence("f1", "f3");
		fpCreator.addFeatureToPrecedence("f2", "f4");
		fpCreator.addFeatureToPrecedence("f3", "f4");
		fpCreator.savePart("C:/Users/sarkara1/git/SIMPOM/plan/part1.rdf");
		
		TestPlanCreator planCreator = new TestPlanCreator(fpCreator.getFeature("IFeature"));
		planCreator.addProcessToPlan("IProcess", "p11", fpCreator.getFeature("f1"));
		planCreator.addProcessToPlan("IProcess", "p21", fpCreator.getFeature("f2"));
		planCreator.addProcessToPlan("p11", "p12", fpCreator.getFeature("f1"));
		planCreator.addProcessToPlan("p21", "p22", fpCreator.getFeature("f2"));
		planCreator.addProcessToPlan("p21", "p23", fpCreator.getFeature("f2"));
		planCreator.savePlanWithPart("C:/Users/sarkara1/git/SIMPOM/plan/plan1.rdf", fpCreator.partKB);
	}
	
	@Test
	public void testPlan3(){		
		
		TestFeaturePrecedenceCreator fpCreator = new TestFeaturePrecedenceCreator();
		fpCreator.addFeatureToPrecedence("IFeature", "f1");
		fpCreator.addFeatureToPrecedence("IFeature", "f2");
		fpCreator.addFeatureToPrecedence("f1", "f3");
		fpCreator.addFeatureToPrecedence("f2", "f3");
		fpCreator.savePart("C:/Users/sarkara1/git/SIMPOM/plan/part2.rdf");
		
		TestPlanCreator planCreator = new TestPlanCreator(fpCreator.getFeature("IFeature"));
		planCreator.addProcessToPlan("IProcess", "p11", fpCreator.getFeature("f1"));
		planCreator.addProcessToPlan("IProcess", "p21", fpCreator.getFeature("f2"));
		planCreator.addProcessToPlan("p11", "p12", fpCreator.getFeature("f1"));
		planCreator.addProcessToPlan("p21", "p22", fpCreator.getFeature("f2"));
		planCreator.addProcessToPlan("p21", "p23", fpCreator.getFeature("f2"));
		planCreator.savePlanWithPart("C:/Users/sarkara1/git/SIMPOM/plan/plan2.rdf", fpCreator.partKB);
	}
	
	@Test
	public void testPlan4(){		
		
		TestFeaturePrecedenceCreator fpCreator = new TestFeaturePrecedenceCreator();
		fpCreator.addFeatureToPrecedence("IFeature", "f1");
		fpCreator.addFeatureToPrecedence("IFeature", "f2");
		fpCreator.addFeatureToPrecedence("f1", "f3");
		fpCreator.addFeatureToPrecedence("f2", "f4");
		fpCreator.addFeatureToPrecedence("f3", "f4");
		fpCreator.savePart("C:/Users/sarkara1/git/SIMPOM/plan/part1.rdf");
		
		TestPlanCreator planCreator = new TestPlanCreator(fpCreator.getFeature("IFeature"));
		planCreator.addProcessToPlan("IProcess", "p11", fpCreator.getFeature("f1"));
		planCreator.addProcessToPlan("IProcess", "p21", fpCreator.getFeature("f2"));
		planCreator.savePlanWithPart("C:/Users/sarkara1/git/SIMPOM/plan/plan3.rdf", fpCreator.partKB);
	}
	
	@Test
	public void testPlan5(){		
		
		TestFeaturePrecedenceCreator fpCreator = new TestFeaturePrecedenceCreator();
		fpCreator.addFeatureToPrecedence("IFeature", "f1");
		fpCreator.addFeatureToPrecedence("IFeature", "f2");
		fpCreator.addFeatureToPrecedence("f1", "f3");
		fpCreator.addFeatureToPrecedence("f2", "f4");
		fpCreator.addFeatureToPrecedence("f3", "f4");
		fpCreator.savePart("C:/Users/sarkara1/git/SIMPOM/plan/part1.rdf");		
		TestPlanCreator planCreator = new TestPlanCreator(fpCreator.getFeature("IFeature"));
		planCreator.savePlanWithPart("C:/Users/sarkara1/git/SIMPOM/plan/plan4.rdf", fpCreator.partKB);
	}
	
	@Test
	public void testPlan6(){		
		
		TestFeaturePrecedenceCreator fpCreator = new TestFeaturePrecedenceCreator();
		fpCreator.addFeatureToPrecedence("IFeature", "f1");
		fpCreator.addFeatureToPrecedence("f1", "f2");
		fpCreator.savePart("C:/Users/sarkara1/git/SIMPOM/plan/fpn1.rdf");		
		TestPlanCreator planCreator = new TestPlanCreator(fpCreator.getFeature("IFeature"));
		planCreator.addProcessToPlan("IProcess", "p11", fpCreator.getFeature("f1"));
		planCreator.addProcessToPlan("p11", "p21", fpCreator.getFeature("f2"));
		planCreator.savePlanWithPart("C:/Users/sarkara1/git/SIMPOM/plan/plan5.rdf", fpCreator.partKB);
	}
	
	@Test
	public void testPlan7(){		
		
		TestFeaturePrecedenceCreator fpCreator = new TestFeaturePrecedenceCreator();
		fpCreator.addFeatureToPrecedence("IFeature", "f1");
		fpCreator.addFeatureToPrecedence("f1", "f2");
		fpCreator.addFeatureToPrecedence("f2", "f3");
		fpCreator.savePart("C:/Users/sarkara1/git/SIMPOM/plan/fpn2.rdf");		
		TestPlanCreator planCreator = new TestPlanCreator(fpCreator.getFeature("IFeature"));
		planCreator.addProcessToPlan("IProcess", "p11", fpCreator.getFeature("f1"));
		planCreator.addProcessToPlan("p11", "p21", fpCreator.getFeature("f2"));
		planCreator.savePlanWithPart("C:/Users/sarkara1/git/SIMPOM/plan/plan6.rdf", fpCreator.partKB);
	}
}
