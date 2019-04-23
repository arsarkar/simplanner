package edu.ohiou.mfgresearch.services;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * This class is a place holder for distributed knowledg base. 
 * For now a static global knowledge is maintained for the entire process selection
 * @author sarkara1
 * 
 */
public class GlobalKnowledge {

	private static GlobalKnowledge KB;
	private Model specification;
	
	private GlobalKnowledge() {
	}

	public static Model getSpecification(){
		load();
		return KB.specification;
	}

	public static void loadSpecification(String url){
		load();
		KB.specification = ModelFactory.createDefaultModel().read(url);
	}
	
	private static void load() {
		if(KB==null){
			KB = new GlobalKnowledge();
		}
	}
}
