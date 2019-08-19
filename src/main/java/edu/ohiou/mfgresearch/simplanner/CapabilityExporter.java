package edu.ohiou.mfgresearch.simplanner;

import java.util.Set;

import edu.ohiou.mfgresearch.implanner.processes.*;
import edu.ohiou.mfgresearch.reader.PropertyReader;

public class CapabilityExporter {
	static Set<ProcessCapability> pcSet;
	static ProcessCapabilityGraph graph;
	
	{
	Holemaking.parseCapability();
	pcSet = Holemaking.getCapabilityList();
	}
	public static void main(String[] args) {
		
		PropertyReader prop = new PropertyReader();		
		graph = new ProcessCapabilityGraph(prop.getProperty("CAPABILITY_ABOX"), null);
		for (ProcessCapability c : pcSet) {
			try {
				importCapability (c, graph);
			} catch (Exception e) {
				System.out.println("problem with " + c.getName());
				e.printStackTrace();
			}
		}

	}
	
	
	public static void importCapability (ProcessCapability pc, ProcessCapabilityGraph pcg) throws Exception 
	{
		
		String propFunction = pcg.postFunction("HoleStarting", "http://www.ohio.edu/ontologies/capability-implanner#" + pc.getProcess().getSimpleName());
		for (String prop : pc.getCapabilityNames()) {
			String value = pc.getCapabilityValue(prop);
			
			if ( !value.contains("arg")) {
				pcg.postCapability("function="+propFunction, 
						"capability=" + prop, 
						"min=" + value, 
						"max=Infinity", 
						"reference=http://www.ohio.edu/ontologies/design#" + prop + "Specification");
				}
		}
		graph.writePartGraph("C:/Users/sormaz/Documents/GitHub/SIMPOM/resource/aboxes/process-capability-inch1-dns.owl", "RDF/XML");

	}

}
