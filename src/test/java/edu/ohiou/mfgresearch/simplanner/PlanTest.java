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
import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.services.GlobalKnowledge;
import edu.ohiou.mfgresearch.services.FeatureProcessMatching;

public class PlanTest {
	
	PropertyReader prop;
	
	@Before
    public void beforeEachTestMethod() {
        prop = new PropertyReader();
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
}
