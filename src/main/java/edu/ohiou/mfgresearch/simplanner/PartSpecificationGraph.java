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
import edu.ohiou.mfgresearch.labimp.basis.ViewObject;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.lambda.functions.Suppl;
import edu.ohiou.mfgresearch.reader.IMPlanXMLLoader;
import edu.ohiou.mfgresearch.reader.PartFeatureLoader;
import edu.ohiou.mfgresearch.reader.PropertyReader;

/**
 * Execute part specification loading rules
 * 
 * @author sarkara1
 *
 */
public class PartSpecificationGraph {

	Logger log;
	PropertyReader prop;
	{
		log = LogManager.getLogManager().getLogger(PartSpecificationGraph.class.getSimpleName());		
		prop = new PropertyReader();
	}
	
	PartFeatureLoader loader;	
	Func<String, String> newIndiForType =c->IMPM.design_ins+c.toLowerCase()+IMPM.newHash(4);	
	public OntModel m;
	
	/**
	 * service for ruleFeature1
	 * @return
	 */
	public String[] getFeatures(){
		return loader.readFeatures().stream().toArray(String[]::new);		
	}	
	
	/**
	 * service for ruleFeature2
	 * @return
	 */
	public String getFeatureType(String featureName){
		return loader.readFeatureType(featureName);		
	}
	
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
	
	/**
	 * service for calcuating diameter from radius
	 * @param featureName
	 * @param dimensionType
	 * @return
	 */
	public Double calculateDiameter(String radius){
		return Double.parseDouble(radius)*2.0;
	}
	
	/**
	 * service for calcuating depth of the hole feature
	 * need stock measurement here
	 * @param featureName
	 * @param dimensionType
	 * @return
	 */
	public Double calculateDepth(String dep){
		return Double.parseDouble(dep);
	}
	
	
	private String partLabel = "";
	
	public PartSpecificationGraph(String partName, String path) {

//		log.info("Starting program for building design specification KB...");
		//create the IMPlanloader
		loader = new IMPlanXMLLoader(path);
		this.partLabel = partName.length()>0?partName:loader.readPartName();
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
		partIndi.addProperty(m.createObjectProperty(IMPM.cco+"inheres_in"), createIBE(partLabel));
	}
	
	public Model runRule_FeatureSpecification(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/read-feature-for-part.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureType(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/read-type-for-feature.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureDimension(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/read-dimension-for-feature.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureDimensionMeasurement(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/read-dimension-measures-for-feature.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureTolerance(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/read-tolerance-for-feature.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_FeatureToleranceMeasure(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/read-tolerance-measurement-for-feature.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_inferFeatureType(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-feature-type-hole.q", this))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-feature-type-slot.q", this))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-feature-type-pocket.q", this))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-feature-type-slab.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_inferMeasurementTypeDiameter(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-quality-type-diameter.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_inferMeasurementTypeDepth(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-quality-type-depth.q", this))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getaBox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	public Model runRule_inferToleranceType(Model kb){
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(kb))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-tolerance-type-roundness.q", this))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-tolerance-type-surfacefinish.q", this))
		   .set(q->q.addPlan("resources/META-INF/rules/specification/infer-tolerance-type-dimension-tolerance.q", this))
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
		   .onSuccess(s->kb.write(System.out, lang))
		   ;		
	}

	public static void main(String[] args) {
		
		PropertyReader prop = new PropertyReader();
		
		//create default knowledge with the part name
		PartSpecificationGraph partGraph = new PartSpecificationGraph("", prop.getProperty("DESIGN_XML"));
		String designKBPath = prop.getProperty("DESIGN_PART_XML");
		
//		writePartGraph(partGraph.m, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all features for the part
		Model m1 = partGraph.runRule_FeatureSpecification(partGraph.m);
//		writePartGraph(m1, designKBPath, "NTRIPLE");
		System.out.println("================================================================================================================");
		//load all features for the part
		Model m2 = partGraph.runRule_FeatureType(m1);
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
		System.out.println("================================================================================================================");
		writePartGraph(m6, designKBPath, "NTRIPLE");
		//infer type
		Model m7 = ModelFactory.createDefaultModel().read(designKBPath);
		Model m8 = partGraph.runRule_inferFeatureType(m7);
		Model m9 = partGraph.runRule_inferMeasurementTypeDiameter(m8);
		Model m10 = partGraph.runRule_inferMeasurementTypeDepth(m9);
		Model m11 = partGraph.runRule_inferToleranceType(m10);
		writePartGraph(m11, designKBPath, "NTRIPLE");
	}	
}


