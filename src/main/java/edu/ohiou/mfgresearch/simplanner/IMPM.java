package edu.ohiou.mfgresearch.simplanner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import edu.ohiou.mfgresearch.lambda.Uni;

public final class IMPM {
	
	static Map<String, String> unitMap = new HashMap<String, String>();	
	
	//namespaces
	private static final String default_ns = "http://www.ohio.edu/ontologies";
	private static final String abox_ns = "http://www.ohio.edu/simplanner";
	private static final String w3_ns = "http://www.w3.org";
	public static final String dcelements = "http://purl.org/dc/elements/1.1";
	public static final String dcterms = "http://dublincore.org/2012/06/14/dcterms";
	public static final String common_core = "http://www.ontologyrepository.com";
	
	//utilities
	public static String data = "resources/META-INF/data/";
	private static String _sessionPath="";
	private static String _sessionHash = "";
	public final static String hash = "#";
	public final static String fslash = "/";
	public final static String bslash = "\\";
	
	private final static Function<String, String> makeDefaultIRI = name-> default_ns + fslash + name;
	private final static Function<String, String> makeABoxIRI = name-> abox_ns + fslash + name;	
	
	//Internationalized Resource Identifiers (IRIs) [RFC3987]
	public static final String rdf = w3_ns+"/1999/02/22-rdf-syntax-ns#";
	public static final String rdfs = w3_ns + "/2000/01/rdf-schema#";
	public static final String owl = w3_ns+"/2002/07/owl#";
	public static final String cco = common_core+"/CommonCoreOntologies/";
	public static final String cpm = makeDefaultIRI.apply("CPM#");
	public static final String oam = makeDefaultIRI.apply("OAM#");
	public static final String impmu = makeDefaultIRI.apply("IMPMU#");
	public static final String impml = makeDefaultIRI.apply("IMPML#");
	public static final String design = makeDefaultIRI.apply("design#");
	public static final String capability = makeDefaultIRI.apply("manufacturing-capability#");
	public static final String capability_IMPM = makeDefaultIRI.apply("capability-implanner#");
	public static final String resource_IMPM = makeDefaultIRI.apply("resource-implanner#");
	public static final String mfg_plan	= makeDefaultIRI.apply("manufacturing-plan#");
	public static final String graph	= makeDefaultIRI.apply("graph5#");
	public static String design_ins = makeABoxIRI.apply("design"+createSessionPath()+createSessionHash()+"#");
	public static String process_ins = makeABoxIRI.apply("process"+createSessionPath()+createSessionHash()+"#");
	public static String plan_ins = makeABoxIRI.apply("plan"+createSessionPath()+createSessionHash()+"#");
	
	
	//OWL classes
	public static final String Part = cpm+"Part";
	public static final String Feature = cpm+"Feature";
	public static final String Specification = cpm+"Specification";
	public static final String InformationBearingEntity = cco + "/InformationBearingEntity";
	
	//RDF-OWL
	public static final String _type = rdf+"type";
	public static final String _class = rdf+"Class";
	public static final String label = rdfs + "label";
	
	//dublin core (use as data properties)
	public static final String contributor = dcelements + "/contributor";
	public static final String creator = dcelements + "/creator";
	public static final String format = dcelements + "/format";
	public static final String identifier = dcelements + "/identifier";
	public static final String language = dcelements + "/language";
	public static final String publisher = dcelements + "/publisher";
	public static final String source = dcelements + "/source";
	public static final String subject = dcelements + "/subject";
	public static final String type = dcelements + "/type";
	public static final String fileformat = dcterms + "/file";
	//data properties	
	public static final String has_integer_value = cco+"/has_integer_value";
	public static final String has_decimal_value = cco+"/has_decimal_value";
	public static final String has_text_value = cco+"/has_text_value";
	
	
	//OWL properties
	public static final String hasFeature = cpm+"hasFeature";
	public static final String hasSpecification = impmu+"hasSpecification";
	public static final String hasNextFeature = impmu+"hasNextFeature";	
	public static final String prescribed_by = cco+"/prescribed_by";
	public static final String described_by = cco+"/described_by";	
	public static final String is_measured_by = cco+"/is_measured_by";	
	public static final String designated_by = cco+"/designated_by";	
	public static final String inheres_in = cco+"/inheres_in";
	
	public static String getUnit(String unit){
		if(unitMap.size()==0) loadUnit();
		for(String k:unitMap.keySet()){
			if(unitMap.get(k).contains(unit)){
				return k;
			}
		}
		return IMPM.cco+"/UnknownUnit";
	}
	
	public static void loadUnit(){
		unitMap.put(IMPM.cco+"/CentimeterMeasurementUnit", "cm, centimeter");
		unitMap.put(IMPM.cco+"/DecimeterMeasurementUnit", "dm, decimeter");
		unitMap.put(IMPM.cco+"/FootMeasurementUnit", "ft, foot");
		unitMap.put(IMPM.cco+"/InchMeasurementUnit", "in, inch");
		unitMap.put(IMPM.cco+"/KilometerMeasurementUnit", "km, kilometer");
		unitMap.put(IMPM.cco+"/MeterMeasurementUnit", "m, meter");
		unitMap.put(IMPM.cco+"/MileMeasurementUnit", "mile");
		unitMap.put(IMPM.cco+"/MillimeterMeasurementUnit", "mm, millimeter");
	}
	
	//utility functions
	public static String newHash(int length){
		Random rand = new Random();
		int l = (int) (Math.pow(10.0, length-1));
		int u = l*9;
		return String.valueOf(l + rand.nextInt(u));
	}
	
	
	/**
	 * Create a new folder if the path supplied does not exist
	 * @param path
	 */
	private static void createFolder(String path){
		Uni.of(Paths.get(path))
				.filter(p->!Files.exists(p))
				.set(p->Files.createDirectories(p));
	}
	
	public static String createSessionPath(){
		if(!_sessionPath.isEmpty()) return _sessionPath;
		LocalDateTime dt = LocalDateTime.now();
		_sessionPath = 			dt.getYear()
								+fslash
								+dt.getMonthValue()
								+fslash
								+dt.getDayOfMonth()
								+fslash;
		return _sessionPath;
	}
	
	/**
	 * Generate a session folder under data folder
	 * in path data/<year>/<month>/<day>
	 */
	public static String createSessionFolder(String p){
		String path = p.length()>0?p:data+createSessionPath();
		createFolder(path);
		return path;
	}
	
	public static void clearSessionPath(){
		if(!_sessionPath.isEmpty()) _sessionPath="";
	}
	
	/**
	 * Generate a new session
	 */
	public static String createSessionHash(){
		//createSessionFolder();
		if (!_sessionHash.isEmpty())
			return _sessionHash;
		else	
			return _sessionHash = newHash(6);
	}
	
}
