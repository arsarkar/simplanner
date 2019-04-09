package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.rules.RunRules;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;
import edu.ohiou.mfgresearch.reader.part.IMPlanXMLLoader;
import edu.ohiou.mfgresearch.reader.part.PartFeatureLoader;

/**
 * Execute part specification loading rules
 * 
 * @author sarkara1
 *
 */
public class PartSpecificationGraph {

	static Logger log;
	{
		log = LogManager.getLogManager().getLogger(PartSpecificationGraph.class.getSimpleName());
	}
	
	//file path
	static String designXMLPath = "C:/Users/sarkara1/git/simplanner/resources/META-INF/implan/SimplePart-v2-from-prt-with-tol.xml";
	static String designKBPath = "C:/Users/sarkara1/git/SIMPOM/impm-ind/partrdf/features4.rdf";
	static String designTBoxPath = "C:/Users/sarkara1/git/SIMPOM/product-model/design_bfo.owl";
	
	PartFeatureLoader loader;	
	Func<String, String> newIndiForType =c->IMPM.design_ins+c.toLowerCase()+IMPM.newHash(4);	
	public OntModel m;
	
	/**
	 * Assert all feature pecifications for the part
	 */
	Suppl<Query> ruleFeature1 = ()->{
		return
		Uni.of(ConstructBuilder::new)
		   .set(b->b.addPrefix("rdf", IMPM.rdf))
		   .set(b->b.addPrefix("owl", IMPM.owl))
		   .set(b->b.addPrefix("cco", IMPM.cco))
		   .set(b->b.addPrefix("design", IMPM.design))
		   .set(b->b.addPrefix("this", "edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph"))
		   .set(b->b.addWhere("?p", "rdf:type", "design:PartSpecification"))	
		   .set(b->b.addWhere("?i1", "rdf:type", "design:LabelBearingEntity"))	
		   .set(b->b.addWhere("?p", "cco:inheres_in", "?i1"))
		   .set(b->b.addWhere("?i1", "cco:has_text_value", "?pn"))
		   .set(b->b.addConstruct("?f", "rdf:type", "design:FeatureSpecification"))
		   .set(b->b.addConstruct("?pf", "rdf:type", "design:PartFeatureMap"))
		   .set(b->b.addConstruct("?i2", "rdf:type", "design:LabelBearingEntity"))
		   .set(b->b.addConstruct("design:describes_map_with", "rdf:type", "owl:ObjectProperty"))
		   .set(b->b.addConstruct("?i2", "cco:has_text_value", "?fn"))
		   .set(b->b.addConstruct("?f", "cco:inheres_in", "?i2"))
		   .set(b->b.addConstruct("?pf", "design:describes_map_with", "?f"))
		   .set(b->b.addConstruct("?pf", "design:describes_map_with", "?p"))
		   .map(b->b.build())
		   .get();
	};
	
	/**
	 * Assert all feature dimensions
	 */
	Suppl<Query> ruleFeatureDimension1 = ()->{
		return
		Uni.of(ConstructBuilder::new)
		   .set(b->b.addPrefix("rdf", IMPM.rdf))
		   .set(b->b.addPrefix("owl", IMPM.owl))
		   .set(b->b.addPrefix("cco", IMPM.cco))
		   .set(b->b.addPrefix("design", IMPM.design))
		   .set(b->b.addPrefix("this", "edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph"))
		   .set(b->b.addWhere("?f", "rdf:type", "design:FeatureSpecification"))	
		   .set(b->b.addWhere("?i2", "rdf:type", "design:LabelBearingEntity"))
		   .set(b->b.addWhere("?f", "cco:inheres_in", "?i2"))
		   .set(b->b.addWhere("?i2", "cco:has_text_value", "?fn"))
		   .set(b->b.addConstruct("?d", "rdf:type", "design:QualitySpecification"))
		   .set(b->b.addConstruct("?fd", "rdf:type", "design:FeatureQualityMap"))
		   .set(b->b.addConstruct("?i3", "rdf:type", "design:TypeBearingEntity"))
		   .set(b->b.addConstruct("design:describes_map_with", "rdf:type", "owl:ObjectProperty"))
		   .set(b->b.addConstruct("cco:has_URI_value", "rdf:type", "owl:DatatypeProperty"))
		   .set(b->b.addConstruct("?i3", "cco:has_URI_value", "?dt"))
		   .set(b->b.addConstruct("?d", "cco:inheres_in", "?i3"))
		   .set(b->b.addConstruct("?fd", "design:describes_map_with", "?f"))
		   .set(b->b.addConstruct("?fd", "design:describes_map_with", "?d"))
		   .map(b->b.build())
		   .get();
	};
	
	/**
	 * Assert all feature dimensions
	 */
	Suppl<Query> ruleFeatureDimension2 = ()->{
		return
		Uni.of(ConstructBuilder::new)
		   .set(b->b.addPrefix("rdf", IMPM.rdf))
		   .set(b->b.addPrefix("owl", IMPM.owl))
		   .set(b->b.addPrefix("cco", IMPM.cco))
		   .set(b->b.addPrefix("design", IMPM.design))
		   .set(b->b.addPrefix("this", "edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph"))
		   .set(b->b.addWhere("?f", "rdf:type", "design:FeatureSpecification"))	
		   .set(b->b.addWhere("?i1", "rdf:type", "design:LabelBearingEntity"))
		   .set(b->b.addWhere("?d", "rdf:type", "design:QualitySpecification"))
		   .set(b->b.addWhere("?fd", "rdf:type", "design:FeatureQualityMap"))
		   .set(b->b.addWhere("?i2", "rdf:type", "design:TypeBearingEntity"))
		   .set(b->b.addWhere("?f", "cco:inheres_in", "?i1"))
		   .set(b->b.addWhere("?i1", "cco:has_text_value", "?fn"))
		   .set(b->b.addWhere("?i2", "cco:has_URI_value", "?dt"))
		   .set(b->b.addWhere("?d", "cco:inheres_in", "?i2"))
		   .set(b->b.addWhere("?fd", "design:describes_map_with", "?f"))
		   .set(b->b.addWhere("?fd", "design:describes_map_with", "?d"))		   
		   .set(b->b.addConstruct("?i3", "rdf:type", "design:MeasurementBearingEntity"))
		   .set(b->b.addConstruct("?i3", "cco:has_text_value", "?dm"))
		   .set(b->b.addConstruct("?d", "cco:inheres_in", "?i3"))
		   .map(b->b.build())
		   .get();
	};
	
	private String partName = "";
	
	public PartSpecificationGraph(String partName, String path) {

//		log.info("Starting program for building design specification KB...");
		//create the IMPlanloader
		loader = new IMPlanXMLLoader(path);
		
		//create specification A-Box 
		m = Uni.of(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM))
			   .set(o->o.setNsPrefix("", IMPM.design_ins)) //default namespace
			   .set(o->o.setNsPrefix("design", IMPM.design)) //design ontology
			   .set(o->o.setNsPrefix("cco", IMPM.cco)) //design ontology
			   .get();
		
		//create initial knowledge 		
		String partNameURI = Uni.of("PartSpecification")
							 .map(newIndiForType)
							 .get();
		Individual partIndi = m.createIndividual(partNameURI, m.createClass(IMPM.design+"PartSpecification"));
		partIndi.addProperty(m.createObjectProperty(IMPM.cco+"inheres_in"), createIBE(partName));		
		this.partName = partName;
	}
	
	public Model runRule_FeatureSpecification(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(designTBoxPath))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan(ruleFeature1.get().serialize(), "?fn", "this:loadFeatures()", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureDimension(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(designTBoxPath))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan(ruleFeatureDimension1.get().serialize(), "?dt", "this:loadDimensions(?fn)", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureDimensionMeasurement(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(designTBoxPath))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan(ruleFeatureDimension2.get().serialize(), "?dm", "this:loadDimensionMeasure(?fn,?dt)", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}

	/**
	 * service for ruleFeature1
	 * @return
	 */
	public String[] loadFeatures(){
		return loader.readFeatures().stream().toArray(String[]::new);		
	}
	
	/**
	 * service for ruleFeatureDimension1
	 * dimensions are mapped to proper type
	 * @return
	 */
	public String[] loadDimensions(String featureName){
		return
		Omni.of(loader.readFeatureDimensions(featureName).keySet().toArray(new String[0]))
			.selectMap(d->d.equals("slotPoint"), d->"design:SlotPointSpecification")
			.selectMap(d->d.equals("normal"), d->"design:NormalSpecification")
			.selectMap(d->d.equals("Normal"), d->"design:NormalSpecification")
			.selectMap(d->d.equals("sweep"), d->"design:SweepSpecification")
			.selectMap(d->d.equals("width"), d->"design:WidthSpecification")
			.selectMap(d->d.equals("bottomDist"), d->"design:BottomDistanceSpecification")
			.selectMap(d->d.equals("positiveSweepLength"), d->"design:PositiveSweepLengthSpecification")
			.selectMap(d->d.equals("negativeSweepLength"), d->"design:NegativeSweepLengthSpecification")
			.selectMap(d->d.equals("radius"), d->"design:RadiusSpecification")
			.selectMap(d->d.equals("diameter"), d->"design:DiameterSpecification")
			.selectMap(d->d.equals("holeAxis"), d->"design:HoleDirectionSpecification")
			.selectMap(d->d.equals("axisPoint"), d->"design:AxisPointSpecification")
			.selectMap(d->d.equals("bottomDistance"), d->"design:BottomDistanceSpecification")
			.selectMap(d->d.equals("PocketPoint"), d->"design:PocketPointSpecification")
			.map(d->d.replace("design:", "http://www.ohio.edu/ontologies/design#"))
			.toList()
			.toArray(new String[0]);	
	}
	
	/**
	 * service for ruleFeatureDimension2
	 * @param featureName
	 * @param dimensionType
	 * @return
	 */
	public String loadDimensionMeasure(String featureName, String dimensionType){
		Uni.of(dimensionType)
			.map(d->d.replace("http://www.ohio.edu/ontologies/design#", "design:"))
			.select(d->d.equals("design:SlotPointSpecification"), d->"slotPoint")
			.select(d->d.equals("design:NormalSpecification"), d->"normal")
			.select(d->d.equals("Normal"), d->"design:NormalSpecification")
			.select(d->d.equals("sweep"), d->"design:SweepSpecification")
			.select(d->d.equals("width"), d->"design:WidthSpecification")
			.select(d->d.equals("bottomDist"), d->"design:BottomDistanceSpecification")
			.select(d->d.equals("positiveSweepLength"), d->"design:PositiveSweepLengthSpecification")
			.select(d->d.equals("negativeSweepLength"), d->"design:NegativeSweepLengthSpecification")
			.select(d->d.equals("radius"), d->"design:RadiusSpecification")
			.select(d->d.equals("diameter"), d->"design:DiameterSpecification")
			.select(d->d.equals("holeAxis"), d->"design:HoleDirectionSpecification")
			.select(d->d.equals("axisPoint"), d->"design:AxisPointSpecification")
			.select(d->d.equals("bottomDistance"), d->"design:BottomDistanceSpecification")
			.select(d->d.equals("PocketPoint"), d->"design:PocketPointSpecification")
		return loader.readFeatureDimensions(featureName).get(dimensionType);
	}
	
	private Individual createIBE(String label) {
		String ibeURI = Uni.of("ibe")
						.map(newIndiForType)
						.get();
		Individual ibe = m.createIndividual(ibeURI, m.createClass(IMPM.design+"LabelBearingEntity"));
		ibe.addProperty(m.createDatatypeProperty(IMPM.cco+"has_text_value"), m.createTypedLiteral(label, XSDDatatype.XSDstring));
		return ibe;
	}
	
	public static void writePartGraph(Model kb, String outputPath, String lang){
		Uni.of(outputPath)
		   .map(File::new)
		   .map(FileOutputStream::new)
		   .set(s->kb.write(s, "RDF/XML"))
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(s->kb.write(System.out, lang));		
	}

	public static void main(String[] args) {
		//create default knowledge with the part name
		PartSpecificationGraph partGraph = new PartSpecificationGraph("SimplePart", designXMLPath);
		writePartGraph(partGraph.m, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all features for the part
		Model m1 = partGraph.runRule_FeatureSpecification(partGraph.m);
		writePartGraph(m1, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all dimensions of the features
		Model m2 = partGraph.runRule_FeatureDimension(m1);
		writePartGraph(m2, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all dimensions of the features
		Model m3 = partGraph.runRule_FeatureDimensionMeasurement(m2);
		writePartGraph(m3, designKBPath, "NTRIPLE");
	}	
}


