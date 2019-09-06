package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.lambda.functions.Func;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.reader.ProcessCapabilityLoader;
import edu.ohiou.mfgresearch.reader.PropertyReader;

public class ProcessCapabilityGraph {

	Logger log;
	PropertyReader prop;
	{
		log = LogManager.getLogManager().getLogger(PartSpecificationGraph.class.getSimpleName());		
		prop = PropertyReader.getProperty();
	}
	
	//file path
	String capabilityOntoPath = prop.getIRIPath(IMPM.capability);	
	String capabilityKBURI = "http://www.ohio.edu/ontologies/capability-implanner";
	String capabilityKBPath = prop.getProperty("CAPABILITY_ABOX");
	Model capabilityKB;
	public Model getCapabilityKB() {
		return capabilityKB;
	}

	ProcessCapabilityLoader loader;
	Function<String, String> newIndiForType =c->capabilityKBURI+ "#" +c.toLowerCase()+IMPM.newHash(4);
	
	/**
	 * returns the pattern for creating new process 
	 * @return
	 */
	private BasicPattern createProcess(){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("capa", IMPM.capability))
			   .set(b->b.addConstruct("?p", "rdf:type", "?pt"))
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}
	
	/**
	 * returns the pattern for creating a function individual tied to process individual
	 * @return
	 */
	private BasicPattern createFunction(){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("capa", IMPM.capability))
			   .set(b->b.addConstruct("?f", "rdf:type", "?ft"))
			   .set(b->b.addConstruct("cco:realizes", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("?p", "cco:realizes", "?f"))
			   
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}
	
	private BasicPattern createMeasurementLimit(){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("capa", IMPM.capability))

			   .set(b->b.addConstruct("?cm", "rdf:type", "cco:MeasurementInformationContentEntity"))
			   .set(b->b.addConstruct("?im", "rdf:type", "cco:InformationBearingEntity"))
			   .set(b->b.addConstruct("cco:inheres_in", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("?cm", "cco:inheres_in", "?im"))
			   .set(b->b.addConstruct("cco:has_decimal_value", "rdf:type", "owl:DatatypeProperty"))
			   .set(b->b.addConstruct("?im", "cco:has_decimal_value", "?limit"))
			   .set(b->b.addConstruct("cco:uses_measurement_unit", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("?im", "cco:uses_measurement_unit", "?unit"))
			   
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}	
	
	private BasicPattern createEquationLimit(int argCount){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("capa", IMPM.capability))
			   .set(b->b.addConstruct("?cmax", "rdf:type", "cco:Equation"))
			   .set(b->b.addConstruct("?ie1", "rdf:type", "cco:InformationBearingEntity"))
			   .set(b->b.addConstruct("cco:inheres_in", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("?cmax", "cco:inheres_in", "?ie1"))
			   .set(b->b.addConstruct("cco:has_string_value", "rdf:type", "owl:DatatypeProperty"))
			   .set(b->b.addConstruct("?ie1", "cco:has_string_value", "?eq"))
			   .set(b->b.addConstruct("cco:uses_measurement_unit", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("?ie1", "cco:uses_measurement_unit", "?unit"))
			   .set(b->b.addConstruct("cco:uses_equation_type", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("?ie1", "cco:uses_equation_type", "cco:JessEquation"))
			   .set(b->b.addConstruct("cco:expects", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("cco:is_tokenized_by", "rdf:type", "owl:DatatypeProperty"))
			   .set(b->IntStream.range(0, argCount)
					   	   		.forEach(i->{
					   	   			b.addConstruct("?a"+i, "rdf:type", "?at"+i);
					   	   			b.addConstruct("?a"+i, "cco:is_tokenized_by", "?ag"+i);
					   	   			b.addConstruct("?cmax", "cco:expects", "?a"+i);
					   	   		}))
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}

	private BasicPattern createNonSelfRefRangeCapability(){
		return
				Uni.of(ConstructBuilder::new)
				   .set(b->b.addPrefix("rdf", IMPM.rdf))
				   .set(b->b.addPrefix("owl", IMPM.owl))
				   .set(b->b.addPrefix("cco", IMPM.cco))
				   .set(b->b.addPrefix("capa", IMPM.capability))
				   .set(b->b.addConstruct("?c", "rdf:type", "?ct"))
				   .set(b->b.addConstruct("capa:demarcates", "rdf:type", "owl:ObjectProperty"))
				   .set(b->b.addConstruct("?c", "capa:demarcates", "?f"))
				   .set(b->b.addConstruct("cco:is_measured_by", "rdf:type", "owl:ObjectProperty"))
				   .set(b->b.addConstruct("?c", "cco:is_measured_by", "?cmax"))
				   .set(b->b.addConstruct("?c", "cco:is_measured_by", "?cmin"))
				   
				   .set(b->b.addConstruct("cco:references", "rdf:type", "owl:ObjectProperty"))
				   .set(b->b.addConstruct("?ref", "rdf:type", "?reft"))
				   .set(b->b.addConstruct("?c", "cco:references", "?ref"))
				   
				   .set(b->b.addConstruct("?max", "rdf:type", "cco:MaximumOrdinalMeasurementInformation"))
				   .set(b->b.addConstruct("?cmax", "cco:is_measured_by", "?max"))
				   .set(b->b.addConstruct("?min", "rdf:type", "cco:MinimumOrdinalMeasurementInformation"))
				   .set(b->b.addConstruct("?cmin", "cco:is_measured_by", "?min"))
				   
				   .set(b->b.addConstruct("?nom", "rdf:type", "cco:NominalMeasurementInformationContentEntity"))
				   .set(b->b.addConstruct("cco:describes_set_with_ordinality", "rdf:type", "owl:ObjectProperty"))
				   .set(b->b.addConstruct("?nom", "cco:describes_set_with_ordinality", "?max"))
				   .set(b->b.addConstruct("?nom", "cco:describes_set_with_ordinality", "?min"))
				   .map(b->b.build())
				   .map(PlanUtil::getConstructBasicPattern)
				   .get();
	}
	
	/**
	 * map function type
	 */
	private Func<String, String> mapFunctionType = f->{
		return
		Uni.of(f)
			.selectMap(s->s.equalsIgnoreCase("HoleFinishing"), s->"capa:HoleFinishing")
			.selectMap(s->s.equalsIgnoreCase("HoleImproving"), s->"capa:HoleImproving")
			.selectMap(s->s.equalsIgnoreCase("HoleMaking"), s->"capa:HoleMaking")
			.selectMap(s->s.equalsIgnoreCase("HoleStarting"), s->"capa:HoleStarting")
			.selectMap(s->s.equalsIgnoreCase("Countouring"), s->"capa:Countouring")
			.selectMap(s->s.equalsIgnoreCase("FineFinishing"), s->"capa:FineFinishing")
			.selectMap(s->s.equalsIgnoreCase("Finishing"), s->"capa:Finishing")
			.selectMap(s->s.equalsIgnoreCase("GrooveMaking"), s->"capa:GrooveMaking")
			.selectMap(s->s.equalsIgnoreCase("Plunging"), s->"capa:Plunging")
			.selectMap(s->s.equalsIgnoreCase("BlindPocketMilling"), s->"capa:BlindPocketMilling")
			.selectMap(s->s.equalsIgnoreCase("ThroughPocketMilling"), s->"capa:ThroughPocketMilling")
			.selectMap(s->s.equalsIgnoreCase("Roughing"), s->"capa:Roughing")
			.selectMap(s->s.equalsIgnoreCase("CloseSlotting"), s->"capa:CloseSlotting")
			.selectMap(s->s.equalsIgnoreCase("DeepSlotting"), s->"capa:DeepSlotting")
			.selectMap(s->s.equalsIgnoreCase("OpenSlotting"), s->"capa:OpenSlotting")
			.selectMap(s->s.equalsIgnoreCase("ShallowSlotting"), s->"capa:ShallowSlotting")
			.selectMap(s->s.contains("capa:"), s->s.replace("capa:", IMPM.capability))
			.selectMap(s->!s.contains("http"), s->IMPM.capability+s)
			.get()
			;
	};
	private Func<String, String> mapCapabilityType = c->{
		return
		Uni.of(c)
			.selectMap(s->s.equalsIgnoreCase("ToolDiameter"), s->"capa:ToolDiameterCapability")
			.selectMap(s->s.equalsIgnoreCase("NegativeTolerance"), s->"capa:NegativeToleranceCapability")
			.selectMap(s->s.equalsIgnoreCase("PositiveTolerance"), s->"capa:PositiveToleranceCapability")
			.selectMap(s->s.equalsIgnoreCase("Straightness"), s->"capa:StraightnessCapability")
			.selectMap(s->s.equalsIgnoreCase("Roundness"), s->"capa:RoundnessCapability")
			.selectMap(s->s.equalsIgnoreCase("Parallelism"), s->"capa:ParallelismCapability")
			.selectMap(s->s.equalsIgnoreCase("DepthLimit"), s->"capa:DepthLimitCapability")
			.selectMap(s->s.equalsIgnoreCase("TruePosition"), s->"capa:TruePositionCapability")
			.selectMap(s->s.equalsIgnoreCase("SurfaceFinish"), s->"capa:SurfaceFinishCapability")
			.selectMap(s->s.contains("capa:"), s->s.replace("capa:", IMPM.capability))
			.selectMap(s->!s.contains("http"), s->IMPM.capability+s)
			.get()
			;
	};
	
	public ProcessCapabilityGraph(String uri, Model m) {
		//load the capability KB
		capabilityKB = ModelFactory.createDefaultModel();
		if(uri.length()>0) capabilityKB.read(uri);
		if(m!=null) capabilityKB.add(m);
	}

	public Query getConstructQuery(String capability){
		return
		Uni.of(ConstructBuilder::new)
		   .set(b->b.addPrefix("rdf", IMPM.rdf))
		   .set(b->b.addPrefix("owl", IMPM.owl))
		   .set(b->b.addPrefix("cco", IMPM.cco))
		   .set(b->b.addPrefix("capa", IMPM.capability))
		   .set(b->b.addConstruct("?c", "rdf:type", capability))
		   .set(b->b.addConstruct("?c", "rdf:type", capability))		   
		   .map(b->b.build())
		   .get();
	}
	
	/**
	 * 
	 * @param functionInstance
	 * @param capability
	 * @param maxEq
	 * @param minEq
	 * @param argTypes
	 * @throws Exception
	 */
	public Node createCapabilityEquation(String equation, Map<String, String> argTypes, boolean isMax) throws Exception{		
		List<String> args = getArgsFromEquation(equation);
		List<String> aTypes = new LinkedList<String>();
		for(String a:args){
			if(argTypes.containsKey(a)){
				aTypes.add(argTypes.get(a));
			}
		}
		if(args.size() != aTypes.size()){
			throw new Exception("Arguments are not supplied properly!");
		}
		BasicPattern patMax = createEquationLimit(args.size()); 
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(patMax);
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(capabilityKB);
		Table t = TableFactory.create();
		Binding b = BindingFactory.binding();
		Node eqNode = NodeFactory.createURI(newIndiForType.apply("Equation"));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("cmax"), eqNode));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("ie1"), NodeFactory.createURI(newIndiForType.apply("InformationBearingEntity"))));
		for(int i=0; i<args.size(); i++){
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("at"+i), ResourceFactory.createResource(aTypes.get(i)).asNode()));
			String argT = aTypes.get(i).substring(aTypes.get(i).lastIndexOf("#")+1, aTypes.get(i).length());
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("a"+i), NodeFactory.createURI(newIndiForType.apply(argT))));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("ag"+i), NodeFactory.createLiteralByValue(args.get(i), XSDDatatype.XSDstring)));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("unit"), NodeFactory.createURI(IMPM.getUnit("mm"))));
		}
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("eq"), NodeFactory.createLiteralByValue(equation, XSDDatatype.XSDstring)));
		t.addBinding(b);
		expander.andThen(updater).apply(t);	
		return eqNode;
	}
	
	public Node createCapabilityEquationUnit(String equation, Map<String, String> argTypes, String unit, boolean isMax) throws Exception{		
		List<String> args = getArgsFromEquation(equation);
		List<String> aTypes = new LinkedList<String>();
		for(String a:args){
			if(argTypes.containsKey(a)){
				aTypes.add(argTypes.get(a));
			}
		}
		if(args.size() != aTypes.size()){
			throw new Exception("Arguments are not supplied properly!");
		}
		BasicPattern patMax = createEquationLimit(args.size()); 
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(patMax);
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(capabilityKB);
		Table t = TableFactory.create();
		Binding b = BindingFactory.binding();
		Node eqNode = NodeFactory.createURI(newIndiForType.apply("Equation"));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("cmax"), eqNode));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("ie1"), NodeFactory.createURI(newIndiForType.apply("InformationBearingEntity"))));
		for(int i=0; i<args.size(); i++){
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("at"+i), ResourceFactory.createResource(aTypes.get(i)).asNode()));
			String argT = aTypes.get(i).substring(aTypes.get(i).lastIndexOf("#")+1, aTypes.get(i).length());
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("a"+i), NodeFactory.createURI(newIndiForType.apply(argT))));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("ag"+i), NodeFactory.createLiteralByValue(args.get(i), XSDDatatype.XSDstring)));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("unit"), NodeFactory.createURI(IMPM.getUnit(unit))));
		}
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("eq"), NodeFactory.createLiteralByValue(equation, XSDDatatype.XSDstring)));
		t.addBinding(b);
		expander.andThen(updater).apply(t);	
		return eqNode;
	}
	
	public static List<String> getArgsFromEquation(String eq){
		List<String> args = new LinkedList<String>();
		Pattern p = Pattern.compile("(\\?[A-Za-z0-9]+)");
		Matcher m = p.matcher(eq);
		while(m.find()){
			args.add(m.group());
		}
		return args;
	}
	
	public Node createCapabilityLimit(double limit, boolean isMax) throws Exception{
	
		BasicPattern pat = createMeasurementLimit();
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(pat);
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(capabilityKB);
		Table t = TableFactory.create();
		Binding b = BindingFactory.binding();
		Node mNode = NodeFactory.createURI(newIndiForType.apply("MeasurementInformationContentEntity"));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("cm"), mNode));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("im"), NodeFactory.createURI(newIndiForType.apply("InformationBearingEntity"))));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("limit"), NodeFactory.createLiteral(String.valueOf(limit), XSDDatatype.XSDdouble)));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("unit"), NodeFactory.createURI(IMPM.getUnit("mm"))));
		t.addBinding(b);
		expander.andThen(updater).apply(t);
		return mNode;
	}

	public Node createCapabilityLimitUnit(double limit, String unit, boolean isMax) throws Exception{
		
		BasicPattern pat = createMeasurementLimit();
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(pat);
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(capabilityKB);
		Table t = TableFactory.create();
		Binding b = BindingFactory.binding();
		Node mNode = NodeFactory.createURI(newIndiForType.apply("MeasurementInformationContentEntity"));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("cm"), mNode));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("im"), NodeFactory.createURI(newIndiForType.apply("InformationBearingEntity"))));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("limit"), NodeFactory.createLiteral(String.valueOf(limit), XSDDatatype.XSDdouble)));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("unit"), NodeFactory.createURI(IMPM.getUnit(unit))));
		t.addBinding(b);
		expander.andThen(updater).apply(t);
		return mNode;
	}
	
	public Node crateCapability(Node functionInstance, String capability, String referenceType, Node maxICE, Node minICE) throws Exception{
		
		//check capability
		Node capabilityNode;
		if(!capability.contains("http")){
			capabilityNode = NodeFactory.createURI(newIndiForType.apply(capability));
			capability = Uni.of(capability).map(mapCapabilityType).get();
		}else{
			capabilityNode = NodeFactory.createURI(newIndiForType.apply(capability.substring(capability.lastIndexOf("#")+1, capability.length())));
		}		
		if(!isValidCapability(capability)) throw new Exception("Capability " + capability + " is not present in " + IMPM.capability);
		else {
			BasicPattern pat = createNonSelfRefRangeCapability();
			Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(pat);
			Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(capabilityKB);
			Table t = TableFactory.create();
			Binding b = BindingFactory.binding();			
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("c"), capabilityNode));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("ct"), ResourceFactory.createResource(capability).asNode()));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("f"), functionInstance));
			String refT = referenceType.substring((referenceType).lastIndexOf("#")+1, referenceType.length());
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("ref"), NodeFactory.createURI(newIndiForType.apply(refT))));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("reft"), ResourceFactory.createResource(referenceType).asNode()));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("cmax"), maxICE));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("cmin"), minICE));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("max"), NodeFactory.createURI(newIndiForType.apply("MaximumOrdinalMeasurementInformation"))));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("min"), NodeFactory.createURI(newIndiForType.apply("MinimumOrdinalMeasurementInformation"))));
			b = Algebra.merge(b, BindingFactory.binding(Var.alloc("nom"), NodeFactory.createURI(newIndiForType.apply("NominalMeasurementInformation"))));
			
			t.addBinding(b);
			expander.andThen(updater).apply(t);
			return capabilityNode;
		}
	}	
	
	private boolean isValidCapability(String capability) {
		return true;
	}
	
	/**
	 * Create new process from the given type URI
	 * @param processURI type URI
	 * @return
	 */
	public String createNewProcess(String processURI) {
		String pNode =  newIndiForType.apply((NodeFactory.createURI(processURI).getLocalName()));
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(createProcess());
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(capabilityKB);
		Table t = TableFactory.create();
		Binding b = BindingFactory.binding();		
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("p"), NodeFactory.createURI(pNode)));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("pt"), NodeFactory.createURI(processURI)));
		t.addBinding(b);
		expander.andThen(updater).apply(t);
		return pNode;
	}
	
	/**
	 * create a function individual related to a given process individual
	 * @param function expects a function type
	 * @param processURI expects the URI of the process individual
	 * @return a function individual
	 * @throws Exception
	 */
	public Node createNewFunction(String function, String processURI) throws Exception{
		//check function
		String funcURI = "";
		Node fNode;
		if(!function.contains("http")) {
			fNode = NodeFactory.createURI(newIndiForType.apply(function));
			funcURI = Uni.of(function).map(mapFunctionType).get();
		}
		else{
			funcURI = function;
			fNode = NodeFactory.createURI(newIndiForType.apply(NodeFactory.createURI(funcURI).getLocalName()));
			
		}
		if(!isValidFunction(funcURI)) throw new Exception("Function " + funcURI + " is not present in " + IMPM.capability); 
		
		Resource processNode = capabilityKB.getResource(processURI);
		
		if(processNode==null) throw new Exception("Process " + processURI + " is not present in the supplied KB");    
		
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(createFunction());
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(capabilityKB);
		Table t = TableFactory.create();
		Binding b = BindingFactory.binding();		
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("f"), fNode));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("ft"), NodeFactory.createURI(funcURI)));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("p"), processNode.asNode()));
		t.addBinding(b);
		expander.andThen(updater).apply(t);
		
		return fNode;
	}

	private boolean isValidFunction(String function) {
		
		return true;
	}
	
	public void writePartGraph(String outputPath, String lang){
		Uni.of(outputPath)
		   .map(File::new)
		   .map(FileOutputStream::new)
		   .set(s->capabilityKB.write(s, "RDF/XML"))
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(s->capabilityKB.write(System.out, lang))
		   ;		
	}
	
	public static boolean isLimitValue(String s){
		try {
			Double.parseDouble(s.trim());
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public String postFunction(String... args) throws Exception{
		Node functionNode = createNewFunction(args[0], args[1]);
		return functionNode.getURI();
	}
	
	public void postCapability(String... args){
		Map<String, String> params = new HashMap<String, String>();
		Node maxICE = null, minICE = null;
		Omni.of(args)
			.select(a->a.split("=")[0].equals("path"), a->params.put("path", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("process"), a->params.put("path", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("function"), a->params.put("function", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("capability"), a->params.put("capability", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("reference"), a->params.put("reference", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("max"), a->params.put("max", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("min"), a->params.put("min", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("arg1"), a->params.put("arg1", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("arg2"), a->params.put("arg2", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("arg3"), a->params.put("arg3", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("arg4"), a->params.put("arg4", a.split("=")[1]))
			.select(a->a.split("=")[0].equals("arg5"), a->params.put("arg5", a.split("=")[1]))
			;
		
		try {
//			capabilityGraph = new ProcessCapabilityGraph(params.containsKey("path")?capabilityKBPath:params.get("path").trim());
			
			if(!isValidFunction(params.get("function").trim())){
				//if function is a type then create an individual
				String functionURI = postFunction(params.get("function").trim(), params.get("process").trim());
				params.put("function", functionURI);
			}
			
			//if function is an individual
			String max = params.get("max").trim();
			if(isLimitValue(max)){
				maxICE = createCapabilityLimit(Double.parseDouble(max), true);
			}else{
//				max = max.replace(" ", "");
				//is an equation
				Map<String, String> argTypes = new HashMap<String, String>();
				if(params.containsKey("arg1")) argTypes.put("?arg1", params.get("arg1").trim());
				if(params.containsKey("arg2")) argTypes.put("?arg2", params.get("arg2").trim());
				if(params.containsKey("arg3")) argTypes.put("?arg3", params.get("arg3").trim());
				if(params.containsKey("arg4")) argTypes.put("?arg4", params.get("arg4").trim());
				if(params.containsKey("arg5")) argTypes.put("?arg5", params.get("arg5").trim());
				maxICE = createCapabilityEquation(max, argTypes, true);
			}
			String min = params.get("min").trim();
			if(isLimitValue(min)){
				minICE = createCapabilityLimit(Double.parseDouble(min), false);
			}else{
//				min = min.replace(" ", "");
				//is an equation
				Map<String, String> argTypes = new HashMap<String, String>();
				if(params.containsKey("arg1")) argTypes.put("?arg1", params.get("arg1").trim());
				if(params.containsKey("arg2")) argTypes.put("?arg2", params.get("arg2").trim());
				if(params.containsKey("arg3")) argTypes.put("?arg3", params.get("arg3").trim());
				if(params.containsKey("arg4")) argTypes.put("?arg4", params.get("arg4").trim());
				if(params.containsKey("arg5")) argTypes.put("?arg5", params.get("arg5").trim());
				minICE = createCapabilityEquation(min, argTypes, false);
			}
			Node funcNode = ResourceFactory.createResource(params.get("function").trim()).asNode();
			crateCapability(funcNode, params.get("capability"), params.get("reference"), maxICE, minICE);
			
//			writePartGraph(capabilityGraph.capabilityKB, "C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-mm1.owl", "NTRIPLE");
		} catch (Exception e1) {
			System.out.println("Error! failed to update" + e1.getMessage());
			e1.printStackTrace(System.out);
		}
	}
	
	public static void main(String[] args) {		
		
		Map<String, String> params = new HashMap<String, String>();
		Node maxICE = null, minICE = null;		
		ProcessCapabilityGraph capabilityGraph = new ProcessCapabilityGraph("", null);
		try {
			Map<String, String> argTypes = new HashMap<String, String>();
			argTypes.put("?arg1", "http://www.ohio.edu/ontologies/design#DepthSpecification");
			argTypes.put("?arg2", "http://www.ohio.edu/ontologies/design#DiameterSpecification");			
			maxICE = capabilityGraph.createCapabilityEquation("(((pow (?arg1 / ?arg2), 3) * 0.0254) + 0.0762)", argTypes, true);
			
			minICE = capabilityGraph.createCapabilityLimit(0.8, false);
			
			Node fNode = capabilityGraph.createNewFunction("HoleMaking", "http://www.ontologyrepository.com/CommonCoreOntologies/boring0101");
			capabilityGraph.crateCapability(fNode, "TruePosition", "http://www.ohio.edu/ontologies/design#HoleSpecification", maxICE, minICE);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		capabilityGraph.writePartGraph("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-mm1.owl", "NTRIPLE");
	}
}
