package edu.ohiou.mfgresearch.reader;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This interface contains generic methods for reading an SIMPlan file
 * this interface is implemented by different plan readers such as SIMPXMLReader
 * @author sarkara1
 *
 */
public interface PartFeatureLoader {

	/**
	 * loads the file 
	 * @param file
	 */
	public PartFeatureLoader load(FileInputStream file);
	
	public String readPartName();
	
	public Map<String, String> readPartProperties();
	
	public List<String> readFeatures();
	
	public String readFeatureType(String featureName);
	
	public Map<String, String> readFeatureDimensions(String featureName);
	
	public String readNextFeature(String featureName);
	
	public String readPreviousFeature(String featureName);
	
	public Map<String, String> readTolerances(String featureName); 
	
}
