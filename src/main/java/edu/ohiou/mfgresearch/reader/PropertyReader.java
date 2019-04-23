package edu.ohiou.mfgresearch.reader;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;

public class PropertyReader {

	String path = "resources/META-INF/simpm.properties";
	Properties prop = new Properties();
	PrefixMap nsMap = PrefixMapFactory.create();
	Map<String, String> iriMap = new HashMap<String, String>();
	
	public PropertyReader(String path) {
		//validate path
		Uni.of(path)
		   .select(p->p.length()>0, p->this.path = p)
		   .onFailure(e->e.printStackTrace(System.out));
		reload();
	}
	
	/**
	 * reload the property
	 */
	public void reload(){
		//load properties
				Uni.of(this.path)
				   .map(FileInputStream::new)
				   .set(s->prop.load(s))
				   .onFailure(e->e.printStackTrace(System.out));
				//load the prefixes		
				prop.forEach((k,v)->{
					if(k.toString().contains("PREFIX")){
						nsMap.add(k.toString().replaceAll("PREFIX.", ""), v.toString());
					}
				});
				Omni.of(prop.get("IRI_MAP").toString().split(";"))
				  	.set(s2->iriMap.put(s2.substring(0, s2.indexOf("<")), s2.substring(s2.indexOf("<")+1, s2.lastIndexOf(">"))));
	}
	
	public String getIRIPath(String uri){
		return nsMap.expand(iriMap.get(nsMap.abbreviate(uri)));
	}

}
