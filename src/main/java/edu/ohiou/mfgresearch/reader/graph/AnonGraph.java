package edu.ohiou.mfgresearch.reader.graph;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class AnonGraph {
	
	Model m;
	Node n;

	public AnonGraph(Node n) {
		this.n = n;
		m = ModelFactory.createDefaultModel();
	}
	
	
	
}
