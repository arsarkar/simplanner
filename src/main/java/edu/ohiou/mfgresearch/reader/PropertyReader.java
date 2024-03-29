package edu.ohiou.mfgresearch.reader;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;

public class PropertyReader {

	String path = "resources/META-INF/properties/simplanner.properties";
	static Properties prop = new Properties();
	Map<String, String> nsMap = new HashMap<String, String>();
	Map<String, String> iriMap = new HashMap<String, String>();
	private static PropertyReader property = null;
	private static String name = "simplanner";
	
	private PropertyReader() {
		//validate path
//		Uni.of(path)
//		   .select(p->p.length()>0, p->this.path = p)
//		   .onFailure(e->e.printStackTrace(System.out));
		reload();
	}
	
	public static PropertyReader getProperty(){
		
		try {
		      URL resourcePropertyURL = PropertyReader.class.getResource(
		      "/META-INF/properties/" + name + ".properties");
		      prop.load(resourcePropertyURL.openStream());
		      System.out.println(
		      "Properties loaded from " + PropertyReader.class.getSimpleName() + " resource, " + resourcePropertyURL);
		    }
		    catch (Exception ex) {
		      System.err.println(
		      "\nProperties "  + name + " not loaded from ViewObject resource file.");
		    }
		    try {
		      File propertyFile = new File(System.getProperty("user.home"),
		      name + ".properties");
		      prop.load(new FileInputStream(propertyFile));
		      System.out.println(
		      "Properties loaded from user home, file " + propertyFile);
		    }
		    catch (Exception ex) {
		      System.err.println(
		      "Properties " + name + " not loaded from user home.");
		    }
		    try {
		      File propertyFile = new File(System.getProperty("user.dir"),
		    		 name +  ".properties");
		      prop.load(new FileInputStream(propertyFile));
		      System.out.println(
		      "Properties loaded from current folder, file " + propertyFile);
		    }
		    catch (Exception ex) {
		      System.err.println(
		      "\nProperties " + name + " not loaded from current folder.");
		    }
		if(property == null){
			property = new PropertyReader();
		}
		return property;
	}
	
	/**
	 * reload the property
	 */
	public void reload(){
		//load properties
//		Uni.of(this.path)
//		   .map(FileInputStream::new)
//		   .set(s->prop.load(s))
//		   .onFailure(e->e.printStackTrace(System.out));
		//load the prefixes		
		prop.forEach((k,v)->{
			if(k.toString().contains("PREFIX")){
				nsMap.put(k.toString().replaceAll("PREFIX.", ""), v.toString());
			}
		});
		Omni.of(prop.get("IRI_MAP").toString().split(";"))
		  	.set(s2->iriMap.put(s2.substring(0, s2.indexOf("<")).trim(), s2.substring(s2.indexOf("<")+1, s2.lastIndexOf(">")).trim()));
	}
	
	private String abbreviate(String uri){
		for(String k:nsMap.keySet()){
			if(uri.trim().contains(nsMap.get(k).trim())){
				return k.trim()+":"+uri.trim().replace(nsMap.get(k).trim(), "");
			}
		}
		return uri;
	}
	
	private String expand(String uri){
		for(String k:nsMap.keySet()){
			if(uri.trim().contains(k.trim())){
				return nsMap.get(k).trim()+uri.trim().replace(k.trim()+":", "");
			}
		}
		return uri;
	}
	
	public String getProperty(String key){
		return expand(prop.getProperty(key));
	}
	
	public String getIRIPath(String uri){
		return expand(iriMap.get(abbreviate(uri)));
	}

	public String getNS(String prefix){
		return nsMap.get(prefix);
	}
}
