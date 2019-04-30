package edu.ohiou.mfgresearch.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;
import edu.ohiou.mfgresearch.simplanner.ProcessPlanningKnowledge;
import jess.Rete;
import jess.Value;

public class FeatureProcessMatching {
	
	Model localKB;
	String localPath;
	PropertyReader prop = new PropertyReader();
	
	public Model getLocalKB() {
		return localKB;
	}

	public void reloadKB(String fileName){
		Uni.of(localPath+fileName)
		   .map(File::new)
		   .map(FileOutputStream::new)
		   .set(s->localKB.write(s, "RDF/XML"))
		   .set(s->s.flush())
		   .set(s->s.close());
		localKB = ModelFactory.createDefaultModel().read(localPath+fileName);	
	}
	
	public FeatureProcessMatching(String[] localIRI) {
		if(localKB == null) {
			localKB = ModelFactory.createDefaultModel();
			localKB.setNsPrefix("", IMPM.plan_ins);
		}
		if(localIRI.length>0) localIRI = new String[]{localPath};
		Omni.of(localIRI)
			.map(path->localKB.add(ModelFactory.createDefaultModel().read(path)));
		IMPM.clearSessionPath();
		localPath = IMPM.createSessionFolder();
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
		
		//second rule: assign concatenated argument sepcifications to equations ICE with is_tokenized_by
		//localKB.write(System.out, "NTRIPLE");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform-capability-equation3.rq", this))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
		reloadKB("before-match1.rdf");
		
		//third rule: specification-capability matching for max and min both measurement type
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/specification-capability-matching-limit.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .set(m->localKB.add(m))
		   .set(m->GlobalKnowledge.addModel(m));
		
		//create an intermediate feature 
		//third rule: specification-capability matching for max and min both measurement type
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(localKB)) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create-output-feature.rq"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .set(m->localKB.add(m))
		   .set(m->GlobalKnowledge.addModel(m));
	}
	
	public Double matchSpecCapMeasure(Double dim, Double max, Double min) throws Exception{
		if( min <= dim && dim <= max ){
			return dim;
		}
		throw new Exception("Not matched");
	}
	
	public Double calculateEquationCapability(String eq, String args) throws Exception{		
		try {
			List<Double> arguments =
			Omni.of(args.split(" "))
				.map(a->Double.parseDouble(a))
				.toList();
			
			for(int i=0; i<arguments.size(); i++){
				int j = i + 1;
				if(eq.contains("?arg"+j)){
					eq = eq.replace("?arg"+j, String.valueOf(arguments.get(i)));
				}
			}
			Rete r = new Rete();
			Value v = r.eval(eq);
			return v.floatValue(r.getGlobalContext());
		} catch (Exception e) {
			throw e;
		}
	}

	public static void main(String[] args) {
		ProcessPlanningKnowledge ppk = new ProcessPlanningKnowledge();
		Model pp = ppk.processPlanningKnowledge1();
		try {
			pp.write(new FileOutputStream(new File("C:/Users/sarkara1/git/simplanner/resources/META-INF/kb/pp1.owl")), "RDF/XML");
			pp.write(System.out, "NTRIPLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
