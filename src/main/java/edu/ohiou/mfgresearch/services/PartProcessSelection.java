package edu.ohiou.mfgresearch.services;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class PartProcessSelection {

	Model localKB;
	String localPath;
	PropertyReader prop = new PropertyReader();
	
	public PartProcessSelection() {
		
	}

	public Model getLocalKB() {
		return localKB;
	}
		
	public PartProcessSelection(String[] localIRI) {
		if(localKB == null) {
			localKB = ModelFactory.createDefaultModel();
			localKB.setNsPrefix("", IMPM.plan_ins);
		}
		if(localIRI.length>0) localIRI = new String[]{localPath};
		Omni.of(localIRI)
			.map(path->localKB.add(ModelFactory.createDefaultModel().read(path)));
		IMPM.clearSessionPath();
		localPath = IMPM.createSessionFolder("");
	}

	public Node ask_to_plan(){
		
		
		execute();
		return null;
	}

	public void execute(){
		
		
		
	}
	
}
