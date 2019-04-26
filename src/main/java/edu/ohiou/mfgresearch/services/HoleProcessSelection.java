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
import edu.ohiou.mfgresearch.simplanner.IMPM;
import edu.ohiou.mfgresearch.simplanner.ProcessPlanningKnowledge;

public class HoleProcessSelection {
	
	Model localKB;
	PropertyReader prop = new PropertyReader();
	
	public Model getLocalKB() {
		return localKB;
	}

	public HoleProcessSelection(String[] localIRI) {
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
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-feature-specifications.rq"))
		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createStringLiteral(featureName)))
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
		//load capability with both max and min as equation
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(prop.getIRIPath(IMPM.capability_IMPM))) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-measure.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pType", ResourceFactory.createResource(processType)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		//load capability with max as measurement and min as equation
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(prop.getIRIPath(IMPM.capability_IMPM))) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-measure-equation.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pType", ResourceFactory.createResource(processType)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		//load capability with max as equation and min as measurement
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(prop.getIRIPath(IMPM.capability_IMPM))) //capability repository 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-capability-equation-measure.rq"))
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
		
		//first rule: assign ibe of specifications to corresponding argument ICE by matching argument type with specification type
		//localKB.write(System.out, "NTRIPLE");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation1.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		//second rule: assign concatenated argument sepcifications to equations ICE with is_tokenized_by
		//localKB.write(System.out, "NTRIPLE");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation2.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		//third rule: specification-capability matching for max and min both measurement type
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/specification-capability-matching-limit.rq", this))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));		
		
		//fourth rule: specification-capability matching for max equation and min measurement type
		
		
		//fifth rule:  specification-capability matching for max measurement and min equation type
		
		
	}
	
	public Double matchSpecCapMeasure(Double dim, Double max, Double min) throws Exception{
		if( min <= dim && dim <= max ){
			return dim;
		}
		throw new Exception("Not matched");
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
