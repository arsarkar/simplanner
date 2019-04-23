package edu.ohiou.mfgresearch.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.ProcessPlanningKnowledge;

public class HoleProcessSelection {
	
	Model localKB;
	
	
	public Model getLocalKB() {
		return localKB;
	}

	public HoleProcessSelection(String[] localIRI) {
		PropertyReader pr = new PropertyReader("");
		if(localKB == null) localKB = ModelFactory.createDefaultModel();
		Omni.of(localIRI)
			.map(path->localKB.add(ModelFactory.createDefaultModel().read(path)));
	}
	
	/**
	 * event to update local belief
	 */
	public void loadSpecifications(String featureName){
		//load specifications for the given feature
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
		   .onSuccess(m->localKB.add(m))
		   ;
	}
	
	/**
	 * event to update capability
	 */
	public void loadCapability(String processType){
		//load specifications for the given feature
		Uni.of(FunQL::new)
		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
		   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl")) //capability repository 
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/core/transfer-capability-measure.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pType", ResourceFactory.createResource(processType)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
		   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl")) //capability repository 
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/core/transfer-capability-measure-equation.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pType", ResourceFactory.createResource(processType)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
		   .set(q->q.addABox("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl")) //capability repository 
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/core/transfer-capability-equation-measure.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pType", ResourceFactory.createResource(processType)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
	}	
	
	/**
	 * Service to post feature specification
	 * @param featureIRI
	 */
	public void ask_to_match(String featureName, String processType){
				
	} 
	
	
	
	public void execute(String featureName, String processType){
		localKB.write(System.out, "NTRIPLE");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox("C:/Users/sarkara1/git/SIMPOM/resource/mfg-resource.owl"))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("C:/Users/sarkara1/git/simplanner/resources/META-INF/rules/core/transform-capability-equation.rq"))
		   .map(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;
		
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
