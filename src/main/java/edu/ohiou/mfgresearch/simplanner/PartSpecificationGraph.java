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

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;
import edu.ohiou.mfgresearch.reader.IMPlanXMLLoader;
import edu.ohiou.mfgresearch.reader.PartFeatureLoader;

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
	static String designXMLPath = "C:/Users/sarkara1/git/simplanner/resources/META-INF/implan/slider_with_slabs.xml";
	static String designKBPath = "C:/Users/sarkara1/git/SIMPOM/product-model/aboxes/Slider.rdf";
	static String designTBoxPath = "C:/Users/sarkara1/git/SIMPOM/product-model/design_bfo.owl";
	
	PartFeatureLoader loader;	
	Func<String, String> newIndiForType =c->IMPM.design_ins+c.toLowerCase()+IMPM.newHash(4);	
	public OntModel m;
	
	/**
	 * Assert all feature specifications for the part
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
	 * service for ruleFeature1
	 * @return
	 */
	public String[] getFeatures(){
		return loader.readFeatures().stream().toArray(String[]::new);		
	}	
	
	/**
	 * Assert all feature type
	 */
	Suppl<Query> ruleFeature2 = ()->{
		return
		Uni.of(ConstructBuilder::new)
		   .set(b->b.addPrefix("rdf", IMPM.rdf))
		   .set(b->b.addPrefix("owl", IMPM.owl))
		   .set(b->b.addPrefix("cco", IMPM.cco))
		   .set(b->b.addPrefix("design", IMPM.design))
		   .set(b->b.addPrefix("this", "edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph"))
		   .set(b->b.addWhere("?f", "rdf:type", "design:FeatureSpecification"))	
		   .set(b->b.addWhere("?i1", "rdf:type", "design:LabelBearingEntity"))
		   .set(b->b.addWhere("?f", "cco:inheres_in", "?i1"))
		   .set(b->b.addWhere("?i1", "cco:has_text_value", "?fn"))
		   .set(b->b.addConstruct("?i2", "rdf:type", "design:TypeBearingEntity"))
		   .set(b->b.addConstruct("cco:has_URI_value", "rdf:type", "owl:DatatypeProperty"))
		   .set(b->b.addConstruct("?f", "cco:inheres_in", "?i2"))
		   .set(b->b.addConstruct("?i2", "cco:has_URI_value", "?ft"))
		   .map(b->b.build())
		   .get();
	};
	
	/**
	 * service for ruleFeature2
	 * @return
	 */
	public String getFeatureType(String featureName){
		return loader.readFeatureType(featureName);		
	}
	
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
	 * service for ruleFeatureDimension1
	 * dimensions are mapped to proper type
	 * @return
	 */
	public String[] getDimensions(String featureName){
		return
		Omni.of(loader.readFeatureDimensions(featureName).keySet().toArray(new String[0]))
//			.selectMap(d->d.equals("slotPoint"), d->"design:SlotPointSpecification")
//			.selectMap(d->d.equals("normal"), d->"design:NormalSpecification")
//			.selectMap(d->d.equals("Normal"), d->"design:NormalSpecification")
//			.selectMap(d->d.equals("sweep"), d->"design:SweepSpecification")
//			.selectMap(d->d.equals("width"), d->"design:WidthSpecification")
//			.selectMap(d->d.equals("bottomDist"), d->"design:BottomDistanceSpecification")
//			.selectMap(d->d.equals("positiveSweepLength"), d->"design:PositiveSweepLengthSpecification")
//			.selectMap(d->d.equals("negativeSweepLength"), d->"design:NegativeSweepLengthSpecification")
//			.selectMap(d->d.equals("radius"), d->"design:RadiusSpecification")
//			.selectMap(d->d.equals("diameter"), d->"design:DiameterSpecification")
//			.selectMap(d->d.equals("holeAxis"), d->"design:HoleDirectionSpecification")
//			.selectMap(d->d.equals("axisPoint"), d->"design:AxisPointSpecification")
//			.selectMap(d->d.equals("bottomDistance"), d->"design:BottomDistanceSpecification")
//			.selectMap(d->d.equals("PocketPoint"), d->"design:PocketPointSpecification")
//			.map(d->d.replace("design:", "http://www.ohio.edu/ontologies/design#"))
			.toList()
			.toArray(new String[0]);	
	}
	
	/**
	 * Assert dimension measures
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
	
	/**
	 * service for ruleFeatureDimension2
	 * @param featureName
	 * @param dimensionType
	 * @return
	 */
	public String getDimensionMeasure(String featureName, String dimensionType){
		return
		Uni.of(dimensionType)
//			.map(d->d.replace("http://www.ohio.edu/ontologies/design#", "design:"))
//			.selectMap(d->d.equals("design:SlotPointSpecification"), d->"slotPoint")
//			.selectMap(d->d.equals("design:NormalSpecification"), d->"normal")
//			.selectMap(d->d.equals("design:NormalSpecification"), d->"Normal")
//			.selectMap(d->d.equals("design:SweepSpecification"), d->"sweep")
//			.selectMap(d->d.equals("design:WidthSpecification"), d->"width")
//			.selectMap(d->d.equals("design:BottomDistanceSpecification"), d->"bottomDist")
//			.selectMap(d->d.equals("design:PositiveSweepLengthSpecification"), d->"positiveSweepLength")
//			.selectMap(d->d.equals("design:NegativeSweepLengthSpecification"), d->"negativeSweepLength")
//			.selectMap(d->d.equals("design:RadiusSpecification"), d->"radius")
//			.selectMap(d->d.equals("design:DiameterSpecification"), d->"diameter")
//			.selectMap(d->d.equals("design:HoleDirectionSpecification"), d->"holeAxis")
//			.selectMap(d->d.equals("design:AxisPointSpecification"), d->"axisPoint")
//			.selectMap(d->d.equals("design:BottomDistanceSpecification"), d->"bottomDistance")
//			.selectMap(d->d.equals("design:PocketPointSpecification"), d->"PocketPoint")
			.map(d->loader.readFeatureDimensions(featureName).get(dimensionType))
			.get();
	}
	
	/**
	 * Assert all feature tolerances
	 */
	Suppl<Query> ruleFeatureTolerance1 = ()->{
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
		   .set(b->b.addConstruct("?t", "rdf:type", "design:ToleranceSpecification"))
		   .set(b->b.addConstruct("?ft", "rdf:type", "design:FeatureQualityMap"))
		   .set(b->b.addConstruct("?i3", "rdf:type", "design:TypeBearingEntity"))
		   .set(b->b.addConstruct("design:describes_map_with", "rdf:type", "owl:ObjectProperty"))
		   .set(b->b.addConstruct("cco:has_URI_value", "rdf:type", "owl:DatatypeProperty"))
		   .set(b->b.addConstruct("?i3", "cco:has_URI_value", "?tt"))
		   .set(b->b.addConstruct("?t", "cco:inheres_in", "?i3"))
		   .set(b->b.addConstruct("?ft", "design:describes_map_with", "?f"))
		   .set(b->b.addConstruct("?ft", "design:describes_map_with", "?t"))
		   .map(b->b.build())
		   .get();
	};
	
	/**
	 * service for ruleFeatureDimension1
	 * dimensions are mapped to proper type
	 * @return
	 */
	public String[] getTolerance(String featureName){
		return
		Omni.of(loader.readTolerances(featureName).keySet().toArray(new String[0]))
			.toList()
			.toArray(new String[0]);	
	}
	
	/**
	 * Assert tolerance measures
	 */
	Suppl<Query> ruleFeatureTolerance2 = ()->{
		return
		Uni.of(ConstructBuilder::new)
		   .set(b->b.addPrefix("rdf", IMPM.rdf))
		   .set(b->b.addPrefix("owl", IMPM.owl))
		   .set(b->b.addPrefix("cco", IMPM.cco))
		   .set(b->b.addPrefix("design", IMPM.design))
		   .set(b->b.addPrefix("this", "edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph"))
		   .set(b->b.addWhere("?f", "rdf:type", "design:FeatureSpecification"))	
		   .set(b->b.addWhere("?i1", "rdf:type", "design:LabelBearingEntity"))
		   .set(b->b.addWhere("?t", "rdf:type", "design:ToleranceSpecification"))
		   .set(b->b.addWhere("?ft", "rdf:type", "design:FeatureQualityMap"))
		   .set(b->b.addWhere("?i2", "rdf:type", "design:TypeBearingEntity"))
		   .set(b->b.addWhere("?f", "cco:inheres_in", "?i1"))
		   .set(b->b.addWhere("?i1", "cco:has_text_value", "?fn"))
		   .set(b->b.addWhere("?i2", "cco:has_URI_value", "?tt"))
		   .set(b->b.addWhere("?t", "cco:inheres_in", "?i2"))
		   .set(b->b.addWhere("?ft", "design:describes_map_with", "?f"))
		   .set(b->b.addWhere("?ft", "design:describes_map_with", "?t"))		   
		   .set(b->b.addConstruct("?i3", "rdf:type", "design:MeasurementBearingEntity"))
		   .set(b->b.addConstruct("cco:has_decimal_value", "rdf:type", "owl:DatatypeProperty"))
		   .set(b->b.addConstruct("?i3", "cco:has_decimal_value", "?tm"))
		   .set(b->b.addConstruct("?t", "cco:inheres_in", "?i3"))
		   .map(b->b.build())
		   .get();
	};
	
	/**
	 * service for ruleFeatureTolerance2
	 * @param featureName
	 * @param dimensionType
	 * @return
	 */
	public Double getToleranceMeasure(String featureName, String toleranceType){
		return
		Uni.of(toleranceType)
			.map(t->loader.readTolerances(featureName).get(toleranceType))
			.map(t->Double.parseDouble(t))
			.get();
	}
	
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
		   .set(q->q.addPlan(ruleFeature1.get().serialize(), "?fn", "this:getFeatures()", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureType(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(designTBoxPath))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan(ruleFeature2.get().serialize(), "?ft", "this:getFeatureType(?fn)", this))
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
		   .set(q->q.addPlan(ruleFeatureDimension1.get().serialize(), "?dt", "this:getDimensions(?fn)", this))
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
		   .set(q->q.addPlan(ruleFeatureDimension2.get().serialize(), "?dm", "this:getDimensionMeasure(?fn,?dt)", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureTolerance(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(designTBoxPath))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan(ruleFeatureTolerance1.get().serialize(), "?tt", "this:getTolerance(?fn)", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureToleranceMeasure(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(designTBoxPath))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan(ruleFeatureTolerance2.get().serialize(), "?tm", "this:getToleranceMeasure(?fn,?tt)", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
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
//		   .onSuccess(s->kb.write(System.out, lang))
		   ;		
	}

	public static void main(String[] args) {
		
		if(args.length>0){
			designXMLPath = args[0];
			if(args.length>1){
				designKBPath = args[1];
			}
		}
		
		//create default knowledge with the part name
		PartSpecificationGraph partGraph = new PartSpecificationGraph("SimplePart", designXMLPath);
//		writePartGraph(partGraph.m, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all features for the part
		Model m1 = partGraph.runRule_FeatureSpecification(partGraph.m);
//		writePartGraph(m1, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all features for the part
		Model m2 = partGraph.runRule_FeatureType(partGraph.m);
//		writePartGraph(m2, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all dimensions of the features
		Model m3 = partGraph.runRule_FeatureDimension(m2);
//		writePartGraph(m3, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all dimensions of the features
		Model m4 = partGraph.runRule_FeatureDimensionMeasurement(m3);
//		writePartGraph(m4, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all dimensions of the features
		Model m5 = partGraph.runRule_FeatureTolerance(m4);
//		writePartGraph(m5, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all dimensions of the features
		Model m6 = partGraph.runRule_FeatureToleranceMeasure(m5);
		writePartGraph(m6, designKBPath, "NTRIPLE");
	}	
}


