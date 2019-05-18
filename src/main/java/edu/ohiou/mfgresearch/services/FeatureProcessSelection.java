package edu.ohiou.mfgresearch.services;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class FeatureProcessSelection {

	Model localKB;
	String localPath;
	PropertyReader prop = new PropertyReader();
	FeatureProcessMatching matchingService;
	
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
	
	public FeatureProcessSelection(String[] localIRI) {
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
	public Model loadSpecifications(String featureName){
		//load specifications for the given feature
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-feature-specifications0.rq"))
		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createStringLiteral(featureName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public void loadProcessPrecedence(String[] functionType){
		//precedence among processes relavant to the feature type
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.mfg_plan)))
		   .set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
		   .set(q->q.addPlan("resources/META-INF/rules/core/process-precedence-drilling.q"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
	}
	
	public void ask_to_select_processes(String featureName){
		//register known matching services and load specification to them
		matchingService = new FeatureProcessMatching(new String[]{});	
		//is it required for this agent to know specification? I don't think so
		matchingService.loadLocalKB(loadSpecifications(featureName)); 
		//load the plan KB 
		localKB.add(GlobalKnowledge.getPlan());
		execute();
	}
	
	public void execute(){
		
		//load process precedence
		loadProcessPrecedence(new String[]{IMPM.capability+"HoleStarting",
											IMPM.capability+"HoleMaking",
											IMPM.capability+"HoleImproving",
											IMPM.capability+"HoleFinishing"});
		
		//check if the feature specification is completely matched
//		Uni.of(FunQL::new)
//		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
//		   .set(q->q.addTBox(prop.getIRIPath(IMPM.mfg_plan)))
//		   .set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
//		   .set(q->q.addPlan("resources/META-INF/rules/process-planning-1.rq"))
//		   .set(q->q.setLocal=true)
//		   .map(q->q.execute())
//		   .map(q->q.getBelief())
//		   .map(b->b.getLocalABox())
//		   .onFailure(e->e.printStackTrace(System.out))
//		   .onSuccess(m->localKB.add(m));
		
		
		
		//get the latest process planned 
//		Uni.of(FunQL::new)
//		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
//		   .set(q->q.addTBox(prop.getIRIPath(IMPM.mfg_plan)))
//		   .set(q->q.addABox(GlobalKnowledge.getPlan()))
//		   .set(q->q.addPlan("resources/META-INF/rules/process-planning-1.rq"))
//		   .set(q->q.setLocal=true)
//		   .map(q->q.execute())
//		   .map(q->q.getBelief())
//		   .map(b->b.getLocalABox())
//		   .onFailure(e->e.printStackTrace(System.out))
//		   .onSuccess(m->localKB.add(m));	
		
	}
	
	
	
}
