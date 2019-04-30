package edu.ohiou.mfgresearch.services;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class FeatureProcessSelection {

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

	public void execute(){
		
		//load specification
		localKB.add(GlobalKnowledge.getSpecification());
		
		//load process precedence
		
		
		//if there is no 
		
	}
	
}
