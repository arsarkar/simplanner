package edu.ohiou.mfgresearch.reader;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.joox.JOOX;
import org.joox.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;

import edu.ohiou.mfgresearch.lambda.Uni;

public class IMPlanXMLLoader implements PartFeatureLoader {
	
	String propertyPath = "resources/META-INF/simpm.properties";
	public Properties property;
	final Logger logger;

	{
		  
		//JAVA8 improved logging is followed from https://garygregory.wordpress.com/2015/09/16/a-gentle-introduction-to-the-log4j-api-and-lambda-basics/
		logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
		
		//create a new Properties
		Uni.of(Properties::new)
		  //load the property file
		  .set(p->p.load(new FileInputStream(propertyPath)))
		  .onFailure(e->e.printStackTrace())
		  //on successful load store it in property variable
		  .onSuccess(p->{
			  property = p;
		  });
		
	}
	
	private Match m;

	/**
	 * Default constructor reads part XML 
	 * for key IMPLAN_XML from the property file at 
	 * resources/META-INF/simpm.properties 
	 */
	public IMPlanXMLLoader() {
		load(Uni.of(()->new FileInputStream(property.get("IMPLAN_XML").toString()))
				.onFailure(e->e.printStackTrace())
				.get());
	}
	
	/**
	 * Load the part XML from the given path
	 * @param path
	 */
	public IMPlanXMLLoader(String path){
		load(Uni.of(()->new FileInputStream(path))
				.onFailure(e->System.out.println("Error! "+e.getMessage()))
				.get());
	}

	@Override
	public IMPlanXMLLoader load(FileInputStream file) {
		Uni.of(file)
				.map(JOOX::$)
				.onFailure(e->System.out.println("Error! "+e.getMessage()))
				.onSuccess(m->this.m=m);
		return this;
	}

	@Override
	public String readPartName() {
		return m.first()
				.attr("partName")
				.toString();
	}

	@Override
	public Map<String, String> readPartProperties() {
		Map<String, String> props = new HashMap<String, String>();
		m.first()
		 .forEach(d->
		 		  Uni.of(d.getAttributes())
		 		  		  .set(c->{
								NamedNodeMap nMap = c;
								IntStream.range(0, nMap.getLength())
										 .forEach(i->{
											 Uni.of(nMap.item(i))
											 		 .set(n->{
											 			props.put(n.getNodeName(), n.getNodeValue()); 
											 		 });
								 });
		}));
		return props;
	}

	@Override
	public List<String> readFeatures() {
		List<String> features = new ArrayList<String>();
		//find all features 
		 m.find("featureList")
				.children()
				//find all feature instances
				.filter(c->JOOX.$(c).matchText(".*features.*").isEmpty())
				.each()
				.stream()
				.forEach(f->{					
					Uni.of(f.tag())
							.map(s->s.substring(s.lastIndexOf(".")+1, s.length()))
							.map(c->features.add(f.attr("featureName")));
				});								
		 return features;
	}
	
	@Override
	public String readFeatureType(String featureName) {
		
		String xPath = "//*[@featureName='" + featureName + "']" ;
		String featureClass = m.xpath(xPath).tag();
		return featureClass.substring(featureClass.lastIndexOf(".")+1, featureClass.length());
	}

	@Override
	public Map<String, String> readFeatureDimensions(String featureName) {
		Map<String, String> dimensions = new HashMap<String, String> ();
		String xPath = "//*[@featureName='" + featureName + "']" ;
		m.xpath(xPath)
		.find("Dimensions")
		.forEach(d->Stream.of(d.getAttributes())
						  .forEach(v->{
					 				IntStream.range(0, v.getLength())
							 		  		 .forEach(i->dimensions.put(v.item(i).getNodeName(), v.item(i).getNodeValue()));
		}));
		//change any value or key for repairing the mapping
		if (dimensions.containsKey("radius")){
			dimensions.put("diameter", String.valueOf(Double.parseDouble(dimensions.get("radius"))*2));
			dimensions.remove("radius");
		}
		if (dimensions.containsKey("bottomDist")){
			dimensions.put("bottomDistance", dimensions.get("bottomDist"));
			dimensions.remove("bottomDist");
		}
		return dimensions;		
	}

	@Override
	public String readNextFeature(String featureName) {
		String xPath = "//*[@featureName='" + featureName + "']" ;
		return m.xpath(xPath)
				.find("nextFeature")
				.attr("name");
	}

	@Override
	public String readPreviousFeature(String featureName) {
		String xPath = "//*[@featureName='" + featureName + "']" ;
		return m.xpath(xPath)
				.find("previousFeature")
				.attr("name");
	}

	@Override
	public Map<String, String> readTolerances(String featureName) {
		Map<String, String> tolerances = new HashMap<String, String> ();
		String xPath = "//*[@featureName='" + featureName + "']" ;
		m.xpath(xPath)
		.find("Tolerances")
		.children()
		.forEach(t->Uni.of(t.getAttributes())
							.set(c->{
								IntStream.range(0,c.getLength())
										 .forEach(i->tolerances.put(c.item(i).getNodeName(), c.item(i).getNodeValue()));
							}));
		return tolerances;
	}

}
