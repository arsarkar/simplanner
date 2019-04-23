package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.jena.rdf.model.Model;
import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;

public class ProcessPlanningKnowledge {
	
	String ppTBoxURI = "http://www.ohio.edu/ontologies/manufacturing-plan";
	String capABoxURI = "http://www.ohio.edu/ontologies/capability-implanner";
	String capTBoxURI = "http://www.ohio.edu/ontologies/manufacturing-capability";
	String capABoxPath = "C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-mm1.owl";
	String capTBoxPath = "C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl";
	String ppTBoxPath = "C:/Users/sarkara1/git/SIMPOM/impm-u/mfg-plan.owl";

	public ProcessPlanningKnowledge() {
		
	}
	
	public Model processPlanningKnowledge1(){
		//assert drilling precedences		
		Model m = 
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(capTBoxPath))
		   .set(q->q.addTBox(ppTBoxPath))
		   .set(q->q.addABox(capABoxPath))
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/process-precedence-drilling.q"))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
		
		return m;
	}	
	
	public static void main(String[] args) {
		ProcessPlanningKnowledge ppk = new ProcessPlanningKnowledge();
		Model pp = ppk.processPlanningKnowledge1();
		try {
			pp.write(new FileOutputStream(new File("C:/Users/sarkara1/git/simplanner/resources/META-INF/kb/pp1.owl")), "RDF/XML");
			pp.write(System.out, "NTRIPLE");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
