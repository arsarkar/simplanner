package edu.ohiou.mfgresearch.services;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ResultSetFormatter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.labimp.graph.Graph;
import edu.ohiou.mfgresearch.labimp.graph.GraphViewer;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.plan.IPlanner;
import edu.ohiou.mfgresearch.plan.PlanUtil;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.reader.graph.FeatureProcessLayouter;
import edu.ohiou.mfgresearch.reader.graph.ColoredArc;
import edu.ohiou.mfgresearch.reader.graph.ColoredNode;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class FeatureProcessSelection {

	static Logger log = LoggerFactory.getLogger(FeatureProcessSelection.class);
	
	Model localKB;
	String localPath;
	static PropertyReader prop = PropertyReader.getProperty();
	FeatureProcessMatching matchingService;
	List<Node> processNodes = null;
	String featureSpec = "";	
	CloneModel cm;
	boolean execute = true;
	
	public Model getLocalKB() {
		return localKB;
	}

	public void reloadKB(String fileName){
		Uni.of(localPath+fileName)
		   .map(File::new)
		   .map(FileOutputStream::new)
		   .set(s->localKB.write(s, "RDF/XML"))
		   .set(s->s.flush())
		   .set(s->s.close());
		localKB = ModelFactory.createDefaultModel().read(localPath+fileName);	
	}
	
	public FeatureProcessSelection(String[] localIRI) {
		if(localKB == null) {
			localKB = ModelFactory.createDefaultModel();
			localKB.setNsPrefix("", IMPM.plan_ins);
		}
		if(localIRI.length>0) localIRI = new String[]{localPath};
		Omni.of(localIRI)
			.map(path->localKB.add(ModelFactory.createDefaultModel().read(path)));
		IMPM.clearSessionPath();
		localPath = IMPM.createSessionFolder("");
	}

	/**
	 * event to update local belief
	 */
	public Model loadSpecifications(String featureName){
		//load specifications for the given feature
		return
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
		   .set(q->q.addPlan("resources/META-INF/rules/core/transfer-feature-specifications0.rq"))
		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createStringLiteral(featureName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .get();
	}
	
	/***
	 * Service to plan feature, delegate to corresponding services as per feature type
	 * @param featureSpecification
	 * @param featureType
	 * @return 
	 */
	public static Node[] ask_to_plan_feature(Node featureSpecification, String featureType){
		
//		if(!featurePlans.containsKey(featureSpecification.getURI())){
//			featurePlans.put(featureSpecification.getURI(), new AnonGraph(featureSpecification));
//		}
		
		featureType = featureType.replaceAll("\"", ""); 
		
		log.info("\n-----------------------------# Begin ask-to-plan-feature #-------------------------------");
		log.info("# feature specification : " + featureSpecification.getLocalName() + " of type " + featureType+ "\n");
		
		if(featureType.equals("Hole")){
			return ask_to_select_holemaking_processes(featureSpecification);
		}
		else if(featureType.equals("Slot")){
			return ask_to_select_open_slotmaking_processes(featureSpecification);
		}
		else if(featureType.equals("ClosedSlot")){
			return ask_to_select_closed_slotmaking_processes(featureSpecification);
		}
		else if(featureType.equals("Slab")){
			return ask_to_select_slabmaking_processes(featureSpecification);
		}
		else if(featureType.equals("Pocket")){
			return ask_to_select_open_pocketmaking_processes(featureSpecification);
		}
		else if(featureType.equals("ClosedPocket")){
			return ask_to_select_closed_pocketmaking_processes(featureSpecification);
		}
//		else if(featureType.equals("http://www.ohio.edu/ontologies/design#Chamfer")){
//			return ask_to_select_pocketmaking_processes(featureSpecification);
//		}
		else{
			return new Node[0];
		}
	}
	
	private void createStockFeature(Node featureSpecification) {
//		if(GlobalKnowledge.getPart()==null){
//			GlobalKnowledge.setPart();
//		}
		processNodes = new LinkedList<Node>();
		
		System.out.println("\n## Create stock feature as dummy root process.");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create_stock_feature2.rq"))
		   .set(q->q.getPlan(0).addVarBinding("f", ResourceFactory.createResource(featureSpecification.getURI())))
		   .set(q->q.setLocal=true)
		   .set(q->q.setServicePostProcess(tab->{
			   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
				   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
			   }
			   if(!tab.isEmpty()){
				   log.info("Stock feature " + tab.rows().next().get(Var.alloc("f1")).getLocalName() + " is created.");
			   }
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->GlobalKnowledge.getCurrentPart().add(m));
		
		//assert the stock feature is output of the root planned process
		System.out.println("\n## Assert root feature as output of the root process.");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))  
		   .set(q->q.addABox(GlobalKnowledge.getCurrentPart())) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/create_stock_feature1.rq"))
		   .set(q->q.getPlan(0).addVarBinding("f", ResourceFactory.createResource(featureSpecification.getURI())))
		   .set(q->q.setLocal=true)
		   .set(q->q.setServicePostProcess(tab->{
			   if(!tab.isEmpty()){
				   log.info("Stock feature " + tab.rows().next().get(Var.alloc("f1")).getLocalName() + " is assigned as output of root process "+ tab.rows().next().get(Var.alloc("p")).getLocalName());
				   tab.rows().forEachRemaining(b->{
					   processNodes.add(b.get(Var.alloc("p")));
				   });
			   }			   
			   return tab;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
		
	}
	
	private Node[] getRootProcesses(){
		
		Resource rootNode;
		if(execute){
			rootNode = ResourceFactory.createResource(processNodes.get(0).getURI());
		}
		else{
			rootNode = ResourceFactory.createResource(cm.getRootProcess().getURI());
			processNodes = new LinkedList<Node>();
			processNodes.add(rootNode.asNode());
		}
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getCurrentPlan()))
		   .set(q->q.addABox(localKB))
		   .set(q->q.addPlan("resources/META-INF/rules/core/select-root-processes.q"))
		   .set(q->q.setLocal=true)
		   .set(q->q.getPlan(0).addVarBinding("p0", rootNode))
		   .set(q->q.setSelectPostProcess(t->{
			   t.rows().forEachRemaining(b->{
				   processNodes.add(b.get(Var.alloc("pNext")));
			   });
			   return t;
		   }))
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out));
		
		
		//save the intermediate RDF for bug fixing
//		Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/pfeature-plan-begin-"+featureSpec+".rdf")
//			.map(File::new)
//			.map(FileOutputStream::new)
//			.set(s->Uni.of(ModelFactory.createDefaultModel())
//						.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//						.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//						.set(m->m.write(s, "RDF/XML")));
		
		GlobalKnowledge.refreshCurrentPart();
		GlobalKnowledge.refreshCurrentPlan();
		
		log.info("\n-----------------------------# End ask-to-plan-feature #-------------------------------\n");
		
		if(processNodes.size()>1){
			return processNodes.subList(1, processNodes.size()).toArray(new Node[0]);
		}
		else{
			return null;
		}
	}
	
	/**
	 * Checks if there is any specification asserted for the part specification
	 * @param featureSpecification
	 * @return true if there is at least one specification, false otherwise
	 */
	private static boolean anySpecificationOfFeature(Node featureSpecification) {
		boolean isAvailable = 
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
				   .set(q->q.addPlan("resources/META-INF/rules/core/select_any_specification.q"))
				   .set(q->q.setLocal=true)
				   .set(q->q.getPlan(0).addVarBinding("fs", ResourceFactory.createResource(featureSpecification.getURI())))
				   .map(q->q.execute())
				   .map(q->q.isQuerySuccess())
				   .onFailure(e->e.printStackTrace(System.out))
				   .get();	
		log.info("Is there any specification for the given feature? "+ String.valueOf(isAvailable));
		return isAvailable;
	}
	
	private BasicPattern createMeasurementQuality(){
		return
			Uni.of(ConstructBuilder::new)
			   .set(b->b.addPrefix("rdf", IMPM.rdf))
			   .set(b->b.addPrefix("owl", IMPM.owl))
			   .set(b->b.addPrefix("cco", IMPM.cco))
			   .set(b->b.addPrefix("design", IMPM.design))
			   .set(b->b.addConstruct("design:describes_map_with", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("cco:inheres_in", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("cco:represents", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("cco:uses_measurement_unit", "rdf:type", "owl:ObjectProperty"))
			   .set(b->b.addConstruct("cco:has_decimal_value", "rdf:type", "owl:DatatypeProperty"))
			   
			   .set(b->b.addConstruct("?fq", "rdf:type", "design:FeatureQualityMap"))
			   .set(b->b.addConstruct("?fq", "design:describes_map_with", "?fs"))
			   .set(b->b.addConstruct("?fq", "design:describes_map_with", "?d"))
			   .set(b->b.addConstruct("?rd", "rdf:type", "design:ToleranceRepresentation"))
			   .set(b->b.addConstruct("?d", "rdf:type", "?dType"))
			   .set(b->b.addConstruct("?d", "cco:inheres_in", "?dm"))
			   .set(b->b.addConstruct("?d", "cco:represents", "?rd"))
			   .set(b->b.addConstruct("?dm", "rdf:type", "design:MeasurementBearingEntity"))
			   .set(b->b.addConstruct("?dm", "cco:uses_measurement_unit", "?unit"))
			   .set(b->b.addConstruct("?dm", "cco:has_decimal_value", "?val"))
			   .map(b->b.build())
			   .map(PlanUtil::getConstructBasicPattern)
			   .get();
	}	
	
	public void createDefaultToleranceMeasurement(String featureSpecification, String toleranceType, double value) throws Exception{
		Function<String, String> newIndiForType =c->IMPM.design_ins +c.toLowerCase()+IMPM.newHash(6);
		Model m = ModelFactory.createDefaultModel();
		BasicPattern pat = createMeasurementQuality();
		Function<Table, BasicPattern> expander = IPlanner.createPatternExpander(pat);
		Function<BasicPattern, BasicPattern> updater = IPlanner.createUpdateExecutor(m);
		Table t = TableFactory.create();
		Binding b = BindingFactory.binding();
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("fs"), NodeFactory.createURI(featureSpecification)));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("fq"), NodeFactory.createURI(newIndiForType.apply("FeatureQualityMap"))));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("rd"), NodeFactory.createURI(newIndiForType.apply("ToleranceRepresentation"))));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("dType"), NodeFactory.createURI(newIndiForType.apply("ToleranceSpecification"))));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("d"), NodeFactory.createURI(newIndiForType.apply(toleranceType))));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("dm"), NodeFactory.createURI(newIndiForType.apply("MeasurementBearingEntity"))));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("val"), NodeFactory.createLiteral(String.valueOf(value), XSDDatatype.XSDdouble)));
		b = Algebra.merge(b, BindingFactory.binding(Var.alloc("unit"), NodeFactory.createURI(GlobalKnowledge.getUnit())));
		t.addBinding(b);
		expander.andThen(updater).apply(t);
		GlobalKnowledge.getSpecification().add(m);
	}
	
	/**
	 * Service to plan holemaking
	 * @param featureName
	 */
	public static Node[] ask_to_select_holemaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();
		
		if(Boolean.parseBoolean(prop.getProperty("MEMOIZE_FEATURE_PLAN").trim())){
			Model archivedPlan = GlobalKnowledge.retrieveCurrentPlan(featureSpecification);
			Model archivedPart = GlobalKnowledge.retrieveCurrentPart(featureSpecification);
			//Uni.of(archivedPlan).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//plan_"+featureSpecification.getLocalName()+".rdf"))));		  
			//Uni.of(archivedPart).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//part_"+featureSpecification.getLocalName()+".rdf"))));		  
			if(archivedPlan!=null && archivedPart!=null){
				log.info("\n Memoized plan is found for feature specification " + featureSpecification);
				fpSel.cm = new CloneModel(archivedPlan, archivedPart);
				fpSel.cm.perform();
				GlobalKnowledge.getCurrentPlan().add(fpSel.cm.getClonedPlan());
				GlobalKnowledge.getCurrentPart().add(fpSel.cm.getClonedPart());
				
//				//save the intermediate RDF for bug fixing
//				Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/pfeature-plan-cloned-"+featureSpecification.getLocalName()+".rdf")
//					.map(File::new)
//					.map(FileOutputStream::new)
//					.set(s->Uni.of(ModelFactory.createDefaultModel())
//								.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//								.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//								.set(m->m.write(s, "RDF/XML")))
//					.set(s->s.flush())
//					.set(s->s.close());
				
				fpSel.execute = false;
			}
		}

		if(fpSel.execute){

			//if there is no feature specifcation, then assert a default specification.
			if(!anySpecificationOfFeature(featureSpecification)){
				Uni.of(FunQL::new)
				.set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				.set(q->q.addABox(GlobalKnowledge.getSpecification()))			   
				.select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("inch")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-hole-inch.rq")) 
				.select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("mm")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-hole-mm.rq"))
				.set(q->q.setLocal=true)
				.set(q->q.getPlan(0).addVarBinding("fs", ResourceFactory.createResource(featureSpecification.getURI())))
				.map(q->q.execute())
				.map(q->q.getBelief())
				.map(b->b.getLocalABox())
				.onFailure(e->e.printStackTrace(System.out))
				.onSuccess(m->GlobalKnowledge.getSpecification().add(m));		
			}

			//create stock feature and link it to the dummy root process of the feature, which is then removed 
			//and only the children of the root process is supplied
			//this needs to be done in the local knowledge base
			fpSel.createStockFeature(featureSpecification);

			//load process precedence for the particular service 
			log.info("\n##loading process precedence by rule process-precedence-drilling.q");
			Uni.of(FunQL::new)
				.set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
				.set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
				.set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
				.set(q->q.addPlan(prop.getProperty("PROCESS_PRECEDENCE_RULE_HOLE")))
				.set(q->q.setLocal=true)
				.map(q->q.execute())
				.map(q->q.getBelief())
				.map(b->b.getLocalABox())
				.onFailure(e->e.printStackTrace(System.out))
				.onSuccess(m->fpSel.localKB.add(m));
			
			//check to see if spot drilling is applicable 
//			Uni.of(FunQL::new)
//				.set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
//				.set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
//				.set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
//				.set(q->q.addABox(fpSel.localKB))
//				.set(q->q.addPlan("resources/META-INF/rules/core/process-planning-holestarting.rq"))
//				.set(q->q.setLocal=true)
//				.set(q->q.setServicePostProcess(tab->{
//					if(!tab.isEmpty()){
//						Node f2 = tab.rows().next().get(Var.alloc("f2"));
//						
//					}
//					return tab;
//				}))
//				.map(q->q.execute())
//				.map(q->q.getBelief())
//				.map(b->b.getLocalABox())
//				.onFailure(e->e.printStackTrace(System.out))
//				.onSuccess(m->fpSel.localKB.add(m));			

			fpSel.execute();
			
			GlobalKnowledge.memoizeCurrentPlan(featureSpecification);
			GlobalKnowledge.memoizeCurrentPart(featureSpecification);

		}
		
		return fpSel.getRootProcesses();
	}

	/**
	 * service to plan slotmaking
	 * @param featureName
	 */
	public static Node[] ask_to_select_open_slotmaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();

		if(Boolean.parseBoolean(prop.getProperty("MEMOIZE_FEATURE_PLAN").trim())){
			Model archivedPlan = GlobalKnowledge.retrieveCurrentPlan(featureSpecification);
			Model archivedPart = GlobalKnowledge.retrieveCurrentPart(featureSpecification);
			//Uni.of(archivedPlan).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//plan_"+featureSpecification.getLocalName()+".rdf"))));		  
			//Uni.of(archivedPart).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//part_"+featureSpecification.getLocalName()+".rdf"))));		  
			if(archivedPlan!=null && archivedPart!=null){
				log.info("\n Memoized plan is found for feature specification " + featureSpecification);
				fpSel.cm = new CloneModel(archivedPlan, archivedPart);
				fpSel.cm.perform();
				GlobalKnowledge.getCurrentPlan().add(fpSel.cm.getClonedPlan());
				GlobalKnowledge.getCurrentPart().add(fpSel.cm.getClonedPart());
				
//				//save the intermediate RDF for bug fixing
//				Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/pfeature-plan-cloned-"+featureSpecification.getLocalName()+".rdf")
//					.map(File::new)
//					.map(FileOutputStream::new)
//					.set(s->Uni.of(ModelFactory.createDefaultModel())
//								.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//								.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//								.set(m->m.write(s, "RDF/XML")))
//					.set(s->s.flush())
//					.set(s->s.close());
				
				fpSel.execute = false;
			}
		}	
		
		if(fpSel.execute){
			
			//if there is no feature specifcation, then assert a default specification.
			if(!anySpecificationOfFeature(featureSpecification)){
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification()))			   
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("inch")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slot_pocket-inch.rq")) 
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("mm")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slot_pocket-mm.rq"))
				   .set(q->q.setLocal=true)
				   .set(q->q.getPlan(0).addVarBinding("fs", ResourceFactory.createResource(featureSpecification.getURI())))
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getLocalABox())
				   .onFailure(e->e.printStackTrace(System.out))
				   .onSuccess(m->GlobalKnowledge.getSpecification().add(m));		
			}
			
			//create stock feature and link it to the dummy root process of the feature, which is then removed 
			//and only the children of the root process is supplied
			//this needs to be done in the local knowledge base
			fpSel.createStockFeature(featureSpecification);
			
			//load process precedence for the particular service 
			log.info("\n##loading slot making process precedence by rule process-precedence-openslot.q");
			Uni.of(FunQL::new)
			   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
			   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
			   .set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
			   .set(q->q.addPlan(prop.getProperty("PROCESS_PRECEDENCE_RULE_OPENSLOT")))
			   .set(q->q.setLocal=true)
			   .map(q->q.execute())
			   .map(q->q.getBelief())
			   .map(b->b.getLocalABox())
			   .onFailure(e->e.printStackTrace(System.out))
			   .onSuccess(m->fpSel.localKB.add(m));
			
			fpSel.execute();
			
			GlobalKnowledge.memoizeCurrentPlan(featureSpecification);
			GlobalKnowledge.memoizeCurrentPart(featureSpecification);
		}
		
		return fpSel.getRootProcesses();
	}
	
	/**
	 * service to plan slotmaking
	 * @param featureName
	 */
	public static Node[] ask_to_select_closed_slotmaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();

		if(Boolean.parseBoolean(prop.getProperty("MEMOIZE_FEATURE_PLAN").trim())){
			Model archivedPlan = GlobalKnowledge.retrieveCurrentPlan(featureSpecification);
			Model archivedPart = GlobalKnowledge.retrieveCurrentPart(featureSpecification);
			//Uni.of(archivedPlan).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//plan_"+featureSpecification.getLocalName()+".rdf"))));		  
			//Uni.of(archivedPart).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//part_"+featureSpecification.getLocalName()+".rdf"))));		  
			if(archivedPlan!=null && archivedPart!=null){
				log.info("\n Memoized plan is found for feature specification " + featureSpecification);
				fpSel.cm = new CloneModel(archivedPlan, archivedPart);
				fpSel.cm.perform();
				GlobalKnowledge.getCurrentPlan().add(fpSel.cm.getClonedPlan());
				GlobalKnowledge.getCurrentPart().add(fpSel.cm.getClonedPart());
				
//				//save the intermediate RDF for bug fixing
//				Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/pfeature-plan-cloned-"+featureSpecification.getLocalName()+".rdf")
//					.map(File::new)
//					.map(FileOutputStream::new)
//					.set(s->Uni.of(ModelFactory.createDefaultModel())
//								.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//								.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//								.set(m->m.write(s, "RDF/XML")))
//					.set(s->s.flush())
//					.set(s->s.close());
				
				fpSel.execute = false;
			}
		}	
		
		if(fpSel.execute){
			
			//if there is no feature specifcation, then assert a default specification.
			if(!anySpecificationOfFeature(featureSpecification)){
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification()))			   
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("inch")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slot_pocket-inch.rq")) 
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("mm")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slot_pocket-mm.rq"))
				   .set(q->q.setLocal=true)
				   .set(q->q.getPlan(0).addVarBinding("fs", ResourceFactory.createResource(featureSpecification.getURI())))
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getLocalABox())
				   .onFailure(e->e.printStackTrace(System.out))
				   .onSuccess(m->GlobalKnowledge.getSpecification().add(m));		
			}
			
			//create stock feature and link it to the dummy root process of the feature, which is then removed 
			//and only the children of the root process is supplied
			//this needs to be done in the local knowledge base
			fpSel.createStockFeature(featureSpecification);
			
			//load process precedence for the particular service 
			log.info("\n##loading slot making process precedence by rule process-precedence-openslot.q");
			Uni.of(FunQL::new)
			   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
			   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
			   .set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
			   .set(q->q.addPlan(prop.getProperty("PROCESS_PRECEDENCE_RULE_CLOSEDSLOT")))
			   .set(q->q.setLocal=true)
			   .map(q->q.execute())
			   .map(q->q.getBelief())
			   .map(b->b.getLocalABox())
			   .onFailure(e->e.printStackTrace(System.out))
			   .onSuccess(m->fpSel.localKB.add(m));
			
			fpSel.execute();
			
			GlobalKnowledge.memoizeCurrentPlan(featureSpecification);
			GlobalKnowledge.memoizeCurrentPart(featureSpecification);
		}
		
		return fpSel.getRootProcesses();
	}
	
	/**
	 * service to plan pocket making
	 * @param featureName
	 */
	public static Node[] ask_to_select_open_pocketmaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();
		
		if(Boolean.parseBoolean(prop.getProperty("MEMOIZE_FEATURE_PLAN").trim())){
			Model archivedPlan = GlobalKnowledge.retrieveCurrentPlan(featureSpecification);
			Model archivedPart = GlobalKnowledge.retrieveCurrentPart(featureSpecification);
			//Uni.of(archivedPlan).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//plan_"+featureSpecification.getLocalName()+".rdf"))));		  
			//Uni.of(archivedPart).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//part_"+featureSpecification.getLocalName()+".rdf"))));		  
			if(archivedPlan!=null && archivedPart!=null){
				log.info("\n Memoized plan is found for feature specification " + featureSpecification);
				fpSel.cm = new CloneModel(archivedPlan, archivedPart);
				fpSel.cm.perform();
				GlobalKnowledge.getCurrentPlan().add(fpSel.cm.getClonedPlan());
				GlobalKnowledge.getCurrentPart().add(fpSel.cm.getClonedPart());
				
//				//save the intermediate RDF for bug fixing
//				Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/pfeature-plan-cloned-"+featureSpecification.getLocalName()+".rdf")
//					.map(File::new)
//					.map(FileOutputStream::new)
//					.set(s->Uni.of(ModelFactory.createDefaultModel())
//								.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//								.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//								.set(m->m.write(s, "RDF/XML")))
//					.set(s->s.flush())
//					.set(s->s.close());
				
				fpSel.execute = false;
			}
		}	
		
		if(fpSel.execute){
			//if there is no feature specifcation, then assert a default specification.
			if(!anySpecificationOfFeature(featureSpecification)){
				
//				try {
//					fpSel.createDefaultToleranceMeasurement(featureSpecification.getURI(), "http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification", 0.05);
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification()))			   
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("inch")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slot_pocket-inch.rq")) 
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("mm")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slot_pocket-mm.rq"))
				   .set(q->q.setLocal=true)
				   .set(q->q.getPlan(0).addVarBinding("fs", ResourceFactory.createResource(featureSpecification.getURI())))
				   .set(q->q.setSelectPostProcess(tab->{
					   ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
					   return tab;
				   }))
				   .set(q->q.setServicePostProcess(tab->{
					   ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
					   return tab;
				   }))				   
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getLocalABox())
				   .onFailure(e->e.printStackTrace(System.out))
				   .onSuccess(m->GlobalKnowledge.getSpecification().add(m));		
			}
			
			//create stock feature and link it to the dummy root process of the feature, which is then removed 
			//and only the children of the root process is supplied
			//this needs to be done in the local knowledge base
		
			fpSel.createStockFeature(featureSpecification);
			
			//load process precedence for the particular service 
			log.info("\n##loading process precedence by rule process-precedence-milling.q");
			Uni.of(FunQL::new)
			   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
			   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
			   .set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
			   .set(q->q.addPlan(prop.getProperty("PROCESS_PRECEDENCE_RULE_OPENPOCKET")))	
			   .set(q->q.setLocal=true)
			   .set(q->q.setSelectPostProcess(tab->{
				   ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
				   return tab;
			   }))
			   .map(q->q.execute())
			   .map(q->q.getBelief())
			   .map(b->b.getLocalABox())
			   .onFailure(e->e.printStackTrace(System.out))
			   .onSuccess(m->fpSel.localKB.add(m));

			fpSel.execute();	
			
			GlobalKnowledge.memoizeCurrentPlan(featureSpecification);
			GlobalKnowledge.memoizeCurrentPart(featureSpecification);
		}

		return fpSel.getRootProcesses();
	}

	/**
	 * service to plan pocket making
	 * @param featureName
	 */
	public static Node[] ask_to_select_closed_pocketmaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();
		
		if(Boolean.parseBoolean(prop.getProperty("MEMOIZE_FEATURE_PLAN").trim())){
			Model archivedPlan = GlobalKnowledge.retrieveCurrentPlan(featureSpecification);
			Model archivedPart = GlobalKnowledge.retrieveCurrentPart(featureSpecification);
			//Uni.of(archivedPlan).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//plan_"+featureSpecification.getLocalName()+".rdf"))));		  
			//Uni.of(archivedPart).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//part_"+featureSpecification.getLocalName()+".rdf"))));		  
			if(archivedPlan!=null && archivedPart!=null){
				log.info("\n Memoized plan is found for feature specification " + featureSpecification);
				fpSel.cm = new CloneModel(archivedPlan, archivedPart);
				fpSel.cm.perform();
				GlobalKnowledge.getCurrentPlan().add(fpSel.cm.getClonedPlan());
				GlobalKnowledge.getCurrentPart().add(fpSel.cm.getClonedPart());
				
//				//save the intermediate RDF for bug fixing
//				Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/pfeature-plan-cloned-"+featureSpecification.getLocalName()+".rdf")
//					.map(File::new)
//					.map(FileOutputStream::new)
//					.set(s->Uni.of(ModelFactory.createDefaultModel())
//								.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//								.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//								.set(m->m.write(s, "RDF/XML")))
//					.set(s->s.flush())
//					.set(s->s.close());
				
				fpSel.execute = false;
			}
		}	
		
		if(fpSel.execute){
			//if there is no feature specifcation, then assert a default specification.
			if(!anySpecificationOfFeature(featureSpecification)){
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification()))			   
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("inch")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slot_pocket-inch.rq")) 
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("mm")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slot_pocket-mm.rq"))
				   .set(q->q.setLocal=true)
				   .set(q->q.getPlan(0).addVarBinding("fs", ResourceFactory.createResource(featureSpecification.getURI())))
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getLocalABox())
				   .onFailure(e->e.printStackTrace(System.out))
				   .onSuccess(m->GlobalKnowledge.getSpecification().add(m));		
			}
			
			//create stock feature and link it to the dummy root process of the feature, which is then removed 
			//and only the children of the root process is supplied
			//this needs to be done in the local knowledge base
		
			fpSel.createStockFeature(featureSpecification);
			
			//load process precedence for the particular service 
			log.info("\n##loading process precedence by rule process-precedence-milling.q");
			Uni.of(FunQL::new)
			   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
			   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
			   .set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
			   .set(q->q.addPlan(prop.getProperty("PROCESS_PRECEDENCE_RULE_CLOSEDPOCKET")))	
			   .set(q->q.setLocal=true)
			   .set(q->q.setSelectPostProcess(tab->{
				   ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
				   return tab;
			   }))
			   .map(q->q.execute())
			   .map(q->q.getBelief())
			   .map(b->b.getLocalABox())
			   .onFailure(e->e.printStackTrace(System.out))
			   .onSuccess(m->fpSel.localKB.add(m));

			fpSel.execute();	
			
			GlobalKnowledge.memoizeCurrentPlan(featureSpecification);
			GlobalKnowledge.memoizeCurrentPart(featureSpecification);
		}

		return fpSel.getRootProcesses();
	}
	
	/**
	 * service to plan slabmaking
	 * @param featureName
	 */
	public static Node[] ask_to_select_slabmaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();
		
		if(Boolean.parseBoolean(prop.getProperty("MEMOIZE_FEATURE_PLAN").trim())){
			Model archivedPlan = GlobalKnowledge.retrieveCurrentPlan(featureSpecification);
			Model archivedPart = GlobalKnowledge.retrieveCurrentPart(featureSpecification);
			//Uni.of(archivedPlan).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//plan_"+featureSpecification.getLocalName()+".rdf"))));		  
			//Uni.of(archivedPart).set(m->m.write(new FileOutputStream(new File("C://Users//sarkara1//Ohio University//Sormaz, Dusan - sarkar-shared//dissertation//experiment//simple-slot//part_"+featureSpecification.getLocalName()+".rdf"))));		  
			if(archivedPlan!=null && archivedPart!=null){
				log.info("\n Memoized plan is found for feature specification " + featureSpecification);
				fpSel.cm = new CloneModel(archivedPlan, archivedPart);
				fpSel.cm.perform();
				GlobalKnowledge.getCurrentPlan().add(fpSel.cm.getClonedPlan());
				GlobalKnowledge.getCurrentPart().add(fpSel.cm.getClonedPart());
				
//				//save the intermediate RDF for bug fixing
//				Uni.of(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/pfeature-plan-cloned-"+featureSpecification.getLocalName()+".rdf")
//					.map(File::new)
//					.map(FileOutputStream::new)
//					.set(s->Uni.of(ModelFactory.createDefaultModel())
//								.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//								.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//								.set(m->m.write(s, "RDF/XML")))
//					.set(s->s.flush())
//					.set(s->s.close());
				
				fpSel.execute = false;
			}
		}
		
		if(fpSel.execute){
			//if there is no feature specifcation, then assert a default specification.
			if(!anySpecificationOfFeature(featureSpecification)){
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification()))			   
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("inch")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slab-inch.rq")) 
				   .select(q->GlobalKnowledge.getUnit().equals(IMPM.getUnit("mm")), q->q.addPlan("resources/META-INF/rules/core/add-default-tolerance-slab-mm.rq"))
				   .set(q->q.setLocal=true)
				   .set(q->q.getPlan(0).addVarBinding("fs", ResourceFactory.createResource(featureSpecification.getURI())))
				   .map(q->q.execute())
				   .map(q->q.getBelief())
				   .map(b->b.getLocalABox())
				   .onFailure(e->e.printStackTrace(System.out))
				   .onSuccess(m->GlobalKnowledge.getSpecification().add(m));		
			}
			
			//create stock feature and link it to the dummy root process of the feature, which is then removed 
			//and only the children of the root process is supplied
			//this needs to be done in the local knowledge base
			
			fpSel.createStockFeature(featureSpecification);
			
			//load process precedence for the particular service 
			log.info("\n##loading process precedence by rule process-precedence-milling.q");
			Uni.of(FunQL::new)
			   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
			   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
			   .set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
			   .set(q->q.addPlan(prop.getProperty("PROCESS_PRECEDENCE_RULE_SLAB")))	
			   .set(q->q.setLocal=true)
			   .set(q->q.setSelectPostProcess(tab->{
				   ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
				   return tab;
			   }))
			   .map(q->q.execute())
			   .map(q->q.getBelief())
			   .map(b->b.getLocalABox())
			   .onFailure(e->e.printStackTrace(System.out))
			   .onSuccess(m->fpSel.localKB.add(m));

			fpSel.execute();
			
			GlobalKnowledge.memoizeCurrentPlan(featureSpecification);
			GlobalKnowledge.memoizeCurrentPart(featureSpecification);
		}
		
		return fpSel.getRootProcesses();
	}
	
	public void execute(){		
		
		//get the latest process planned 
		boolean stopIteration = false;

		Graph g = new Graph();
		FeatureProcessLayouter fpl =  new FeatureProcessLayouter(g, 10.0, 3, 5, 1.3, true);
		
		GraphViewer v = new GraphViewer(g,fpl, GraphViewer.VIEW_2D);
		if(Boolean.parseBoolean(prop.getProperty("SHOW_PROCESS_GRAPH").trim())) 
			v.display("Plan for" + featureSpec);
		
		//function to select result of process-plan-1 rule and plot in FeatureProcessLayouter
		Function<Table, Table> plotProcessSelectionTree = tab->{
			if(!Boolean.parseBoolean(prop.getProperty("SHOW_PROCESS_GRAPH").trim())) return tab; 
			int numChildren = tab.size();
			if(fpl.getRank()>0) fpl.nextOrbit();
			
			//save the intermediate RDF for bug fixing
//			try {
//				OutputStream os = new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-feature-graph.rdf"));
//			Uni.of(ModelFactory.createDefaultModel())
//				.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//				.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//				.set(m->m.add(localKB))
//				.set(m->m.write(os, "RDF/XML"));
//				os.flush();
//				os.close();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			fpl.setNumPlanets(numChildren);
			tab.rows().forEachRemaining(b->{
				//add the last process planned, only fires for start process
				Node parent = b.get(Var.alloc("pCurrent"));	
				if(fpl.getRank()==0) {
//					g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (parent.getLocalName()));
					g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (new ColoredNode(parent.getLocalName(), Color.BLACK)));
					fpl.nextOrbit();
				}
				//get the new process individual created
				Node child = b.get(Var.alloc("pNext1"));
				if (!g.hasObject(child.getLocalName())) {
//					g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (child.getLocalName()));
					g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (new ColoredNode(child.getLocalName(), Color.BLACK)));
					Uni.of(g)
//					.set(g1->g1.addDirectedArc(parent.getLocalName(), child.getLocalName(), new ColoredArc("", Color.GREEN)))
					.set(g1->g1.addDirectedArc(new ColoredNode(parent.getLocalName(), Color.BLACK), new ColoredNode(child.getLocalName(), Color.BLACK), new ColoredArc("precedes", Color.GREEN)))
					.onFailure(e->e.printStackTrace(System.out));
					//get new interm feature created
					Node iFeature = b.get(Var.alloc("f2"));
					
					Uni.of(FunQL::new)
					   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
					   .set(q->q.addABox(GlobalKnowledge.getCurrentPart()))
					   .set(q->q.addPlan("resources/META-INF/rules/reader/read-interm-feature.q"))
					   .set(q->q.getPlan(0).addVarBinding("f1", ResourceFactory.createResource(iFeature.getURI())))
					   .set(q->q.setSelectPostProcess(tab1->{
						   if(tab1.isEmpty()){
							   g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (new ColoredNode(iFeature.getLocalName(), Color.CYAN)));
							   Uni.of(g)
							      .set(g1->g1.addDirectedArc(new ColoredNode(child.getLocalName(), Color.BLACK), new ColoredNode(iFeature.getLocalName(), Color.BLACK), new ColoredArc("has_output", Color.MAGENTA)))
								  .onFailure(e->e.printStackTrace(System.out));
							   return tab1;	
						   }
						   else{
							   ResultSetFormatter.out(System.out, tab1.toResultSet(), q.getAllPrefixMapping());
							   Table filtTab = TableFactory.create();
							   StringBuilder s = new StringBuilder();
							   tab1.rows().forEachRemaining(b1->{
								   String ft = b1.get(Var.alloc("ft")).getURI();
								   if(ft.equals("http://www.ohio.edu/ontologies/design#FormFeature")){
									   //get satisfied dimension and tolerances
									   if(b1.contains(Var.alloc("d"))){
										   s.append(" concretizes ");
										   s.append(" ").append(b1.get(Var.alloc("d")).getLocalName());
									   }
									   return;
								   }
								   filtTab.addBinding(b1);
							   });

							   ColoredNode cn = null; 
							   if(!filtTab.isEmpty()){
								   String ft = filtTab.rows().next().get(Var.alloc("ft")).getURI();
								   
								   if(ft.equals("http://www.ohio.edu/ontologies/design#IntermediateFormFeature")){
									   cn = new ColoredNode(iFeature.getLocalName(), Color.BLUE);
									   cn.setTooltip(s.toString());
								   }
								   else if(ft.equals("http://www.ohio.edu/ontologies/design#UnsatisfiedFeature")){
									   cn = new ColoredNode(iFeature.getLocalName(), Color.RED);
									   cn.setTooltip(s.toString());
								   }
//								   else if(ft.equals("http://www.ohio.edu/ontologies/design#FinalFeature")){
//									   cn = new ColoredNode(iFeature.getLocalName(), Color.GREEN);
//									   cn.setTooltip(s.toString());
//								   }
							   }
							   else{
								   cn = new ColoredNode(iFeature.getLocalName(), Color.GREEN);
								   cn.setTooltip(s.toString());
							   }
							   
							   g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (cn));
							   final ColoredNode chNode = cn;
							   Uni.of(g)
							      .set(g1->g1.addDirectedArc(new ColoredNode(child.getLocalName(), Color.BLACK), chNode, new ColoredArc("has_output", Color.MAGENTA)))
								  .onFailure(e->e.printStackTrace(System.out));
							   return tab1;							   
						   }
					   }))
					   .map(q->q.execute());
					
//					g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (new ColoredNode(iFeature.getLocalName(), Color.ORANGE)));
//					Uni.of(g)
//					.set(g1->g1.addDirectedArc(new ColoredNode(child.getLocalName(), Color.BLACK), new ColoredNode(iFeature.getLocalName(), Color.BLACK), new ColoredArc("has_output", Color.MAGENTA)))
//					.onFailure(e->e.printStackTrace(System.out));
				}
			});
			fpl.repositionEdges();
			return tab;
		};
	
		if(GlobalKnowledge.getPlan()==null){
			GlobalKnowledge.setPlan();
		}
		
		int counter = 0;
		while(!stopIteration){
			counter += 1;
			log.info("\n---------------------------------------------------------------------------------------------------------");
			log.info("match feature by process-planning-1.rq. iteration ---> " + counter);
			
			//save the intermediate RDF for bug fixing
//			try {
//				OutputStream os = new FileOutputStream(new File(PropertyReader.getProperty().getNS("git1")+"impm-ind/plan/psec-feature-"+counter+".rdf"));
//			Uni.of(ModelFactory.createDefaultModel())
//				.set(m->m.add(GlobalKnowledge.getCurrentPart()))
//				.set(m->m.add(GlobalKnowledge.getCurrentPlan()))
//				.set(m->m.add(localKB))
//				.set(m->m.write(os, "RDF/XML"));
//				os.flush();
//				os.close();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			boolean	isSuccessful = 	
					Uni.of(FunQL::new)
					   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
					   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
					   .set(q->q.addABox(localKB))
					   .set(q->q.addABox(GlobalKnowledge.getCurrentPart()))
					   .set(q->q.addABox(GlobalKnowledge.getCurrentPlan()))
					   .set(q->q.addPlan("resources/META-INF/rules/core/process-planning-preparation.rq"))
//					   .set(q->q.getPlan(0).addVarBinding("p0", ResourceFactory.createResource(processNodes.get(0).getURI())))
					   .set(q->q.setLocal=true)
//					   .set(q->q.setServicePostProcess(plotProcessSelectionTree))
					   .set(q->q.setSelectPostProcess(tab->{
						   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
							   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
						   }
						   if(!tab.isEmpty()){
							   tab.rows().forEachRemaining(r->{
								   log.info("Process type " + r.get(Var.alloc("pType")).getLocalName() + " can be applied after current occurrence " + r.get(Var.alloc("pCurrent")).getLocalName() + "(" + r.get(Var.alloc("pt")).getLocalName() + ")");
							   });
						   }
						   return tab;
					   }))
					   .set(q->q.setServicePostProcess(plotProcessSelectionTree.andThen(tab->{
						   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
							   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
						   }
						   if(!tab.isEmpty()){
							   tab.rows().forEachRemaining(r->{
								   log.info("\nOccurrence " + r.get(Var.alloc("pNext1")).getLocalName() + "(" + r.get(Var.alloc("pType")).getLocalName() + ")" +
										   		" is applied after " + r.get(Var.alloc("pCurrent")).getLocalName() + " generating output feature " + r.get(Var.alloc("f2")).getLocalName());
							   });
						   }						   
						   return tab;
					   })))
					   .map(q->q.execute())
					   .set(q->{
						   GlobalKnowledge.getCurrentPlan().add(q.getBelief().getLocalABox());   
					   })
					   .map(q->q.isQuerySuccess())
					   .get();	
			
			if(!isSuccessful){
				isSuccessful = 	
						Uni.of(FunQL::new)
						   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
						   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
						   .set(q->q.addABox(localKB))
						   .set(q->q.addABox(GlobalKnowledge.getCurrentPart()))
						   .set(q->q.addABox(GlobalKnowledge.getCurrentPlan()))
						   .set(q->q.addPlan("resources/META-INF/rules/core/process-planning-1.rq"))
//						   .set(q->q.getPlan(0).addVarBinding("p0", ResourceFactory.createResource(processNodes.get(0).getURI())))
						   .set(q->q.setLocal=true)
//						   .set(q->q.setServicePostProcess(plotProcessSelectionTree))
						   .set(q->q.setSelectPostProcess(tab->{
							   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
								   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
							   }
							   if(!tab.isEmpty()){
								   tab.rows().forEachRemaining(r->{
									   log.info("Process type " + r.get(Var.alloc("pType")).getLocalName() + " can be applied after current occurrence " + r.get(Var.alloc("pCurrent")).getLocalName() + "(" + r.get(Var.alloc("pt")).getLocalName() + ")");
								   });
							   }
							   return tab;
						   }))
						   .set(q->q.setServicePostProcess(plotProcessSelectionTree.andThen(tab->{
							   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
								   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
							   }
							   if(!tab.isEmpty()){
								   tab.rows().forEachRemaining(r->{
									   log.info("\nOccurrence " + r.get(Var.alloc("pNext1")).getLocalName() + "(" + r.get(Var.alloc("pType")).getLocalName() + ")" +
											   		" is applied after " + r.get(Var.alloc("pCurrent")).getLocalName() + " generating output feature " + r.get(Var.alloc("f2")).getLocalName());
								   });
							   }						   
							   return tab;
						   })))
						   .map(q->q.execute())
						   .set(q->{
							   GlobalKnowledge.getCurrentPlan().add(q.getBelief().getLocalABox());   
						   })
						   .map(q->q.isQuerySuccess())
						   .get();	
			}	
			stopIteration = !isSuccessful;
		}
	}
}

class CloneModel{
	
	Model planModel	= ModelFactory.createDefaultModel();
	Model partModel = ModelFactory.createDefaultModel();
	Model clonedPlan = ModelFactory.createDefaultModel();
	Model clonedPart = ModelFactory.createDefaultModel();

	List<Node> rootprocess = new LinkedList<Node>();
	
	public CloneModel(){
		
	}
	
	public CloneModel(Model plan, Model part){
		this();
		this.planModel = plan;
		this.partModel = part;
	}		
	
	public Model getClonedPlan() {
		return clonedPlan;
	}
	
	public Model getClonedPart() {
		return clonedPart;
	}
	
	public Node getRootProcess() {
		return rootprocess.get(0);
	}
	
	public void perform(){
		Map<Node, Resource> processMap = new HashMap<Node, Resource>();
		Map<Node, Resource> featureMap = new HashMap<Node, Resource>();
		Function<Node, String> renameNode = n->{
			String ns = n.getNameSpace();
			String name = n.getLocalName();
			String newName = name.replaceFirst("I[0-9]*(?!.*I[0-9]*)", "I"+IMPM.newHash(6));
			return ns+newName;
		};

	   clonedPlan.add(ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/has_input"), 
		   		  		ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
		   		  		ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));
	   clonedPlan.add(ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/has_output"), 
		  		  		ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
		  		  		ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));
	   clonedPlan.add(ResourceFactory.createProperty("http://www.ohio.edu/ontologies/manufacturing-plan#precedes"), 
		  		  		ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
		  		  		ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));
	   
	   //run query to extract all processes, their input and output and precedence
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
		   .set(q->q.addABox(planModel))
		   .set(q->q.addPlan("resources/META-INF/rules/core/clone-process-individual.rq"))
		   .set(q->q.setSelectPostProcess(t->{
			   //ResultSetFormatter.out(System.out, t.toResultSet(), q.getAllPrefixMapping());
			   t.rows().forEachRemaining(r->{
				   //?p1 rdf:type ?pt
				   Resource p1 = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("p1"))));
				   processMap.put(r.get(Var.alloc("p1")), p1);
				   clonedPlan.add(p1, 
						   		  ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
						   		  ResourceFactory.createResource(r.get(Var.alloc("pt")).getURI()));
				   //?p0 cco:has_input ?i1
				   Resource i1 = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("i1"))));
				   if(!featureMap.containsKey(r.get(Var.alloc("i1")))){
					   featureMap.put(r.get(Var.alloc("i1")), i1);
					   clonedPlan.add(i1, 
						   		  ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
						   		  ResourceFactory.createResource("http://www.ohio.edu/ontologies/design#FormFeature"));
				   }
				   else{
					   i1 = featureMap.get(r.get(Var.alloc("i1")));
				   }
				   clonedPlan.add(p1, 
					   		  ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/has_input"),
					   		  i1);
				   //?p0 cco:has_output ?o1
				   Resource o1 = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("o1"))));
				   if(!featureMap.containsKey(r.get(Var.alloc("o1")))){
					   featureMap.put(r.get(Var.alloc("o1")), o1);
					   clonedPlan.add(o1, 
						   		  ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
						   		  ResourceFactory.createResource("http://www.ohio.edu/ontologies/design#FormFeature"));					   
				   }
				   else{
					   o1 = featureMap.get(r.get(Var.alloc("o1")));
				   }
				   clonedPlan.add(p1, 
					   		  ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/has_output"),
					   		  o1);
			   });
			   t.rows().forEachRemaining(r->{
				   // ?p1	plan:precedes		?p2
				   if(r.get(Var.alloc("p2"))!=null){
					   clonedPlan.add(processMap.get(r.get(Var.alloc("p1"))), 
						   		  		ResourceFactory.createProperty("http://www.ohio.edu/ontologies/manufacturing-plan#precedes"),
						   		  		processMap.get(r.get(Var.alloc("p2"))));
				   }
			   });
			   return t;
		   }))
		   .map(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));
		
		//create root process
		Uni.of(FunQL::new)
		.set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		.set(q->q.addABox(planModel))
		.set(q->q.addPlan("resources/META-INF/rules/core/clone-root-process.rq"))
		.set(q->q.setSelectPostProcess(t->{
			//ResultSetFormatter.out(System.out, t.toResultSet(), q.getAllPrefixMapping());
			t.rows().forEachRemaining(r->{
				if(!processMap.containsKey(r.get(Var.alloc("p1")))){
					Resource p1 = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("p1"))));
					processMap.put(r.get(Var.alloc("p1")), p1);
					rootprocess.add(p1.asNode());
					   clonedPlan.add(p1, 
				   		  		ResourceFactory.createProperty("http://www.ohio.edu/ontologies/manufacturing-plan#precedes"),
				   		  		processMap.get(r.get(Var.alloc("p2"))));
				}
				else{
					   clonedPlan.add(processMap.get(r.get(Var.alloc("p1"))), 
				   		  		ResourceFactory.createProperty("http://www.ohio.edu/ontologies/manufacturing-plan#precedes"),
				   		  		processMap.get(r.get(Var.alloc("p2"))));
				}
			});
			return t;
		}))
		.map(q->q.execute())
		.onFailure(e->e.printStackTrace(System.out));

		//load all features and assign concretization
		Uni.of(FunQL::new)
			.set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
			.set(q->q.addABox(partModel))
			.set(q->q.addPlan("resources/META-INF/rules/core/clone-formfeature-concretization.rq"))
			.set(q->q.setSelectPostProcess(t->{
				//ResultSetFormatter.out(System.out, t.toResultSet(), q.getAllPrefixMapping());
				t.rows().forEachRemaining(r->{
					if(featureMap.containsKey(r.get(Var.alloc("f")))){
						Resource f = featureMap.get(r.get(Var.alloc("f")));
						clonedPart.add(f, 
								ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
								ResourceFactory.createResource("http://www.ohio.edu/ontologies/design#FormFeature"));
						if(r.get(Var.alloc("fs"))!=null){
							clonedPart.add(f, 
									ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/concretizes"),
									ResourceFactory.createResource(r.get(Var.alloc("fs")).getURI()));
						}
					}
				});
				return t;
			}))
			.map(q->q.execute())
			.onFailure(e->e.printStackTrace(System.out));
		
		//for each formfeature specify unsatisfied or intermediate
		Uni.of(FunQL::new)
			.set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
			.set(q->q.addABox(partModel))
			.set(q->q.addPlan("resources/META-INF/rules/core/clone-formfeature-individual.rq"))
			.set(q->q.setSelectPostProcess(t->{
				//ResultSetFormatter.out(System.out, t.toResultSet(), q.getAllPrefixMapping());
				t.rows().forEachRemaining(r->{
					if(featureMap.containsKey(r.get(Var.alloc("f")))){
						Resource f = featureMap.get(r.get(Var.alloc("f")));
						clonedPart.add(f, 
								ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
								ResourceFactory.createResource(r.get(Var.alloc("ft")).getURI()));
					}
				});
				return t;
			}))
			.map(q->q.execute())
			.onFailure(e->e.printStackTrace(System.out));
		
		clonedPart.add(ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/specified_by"), 
				ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));	
		
		//for each formfeature specify the specified in
		Uni.of(FunQL::new)
			.set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
			.set(q->q.addABox(partModel))
			.set(q->q.addPlan("resources/META-INF/rules/core/clone-formfeature-specification.rq"))
			.set(q->q.setSelectPostProcess(t->{
				//ResultSetFormatter.out(System.out, t.toResultSet(), q.getAllPrefixMapping());
				t.rows().forEachRemaining(r->{
					if(featureMap.containsKey(r.get(Var.alloc("f")))){
						Resource f = featureMap.get(r.get(Var.alloc("f")));
						clonedPart.add(ResourceFactory.createResource(r.get(Var.alloc("r")).getURI()), 
								ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/specified_by"),
								f);
					}
				});
				return t;
			}))
			.map(q->q.execute())
			.onFailure(e->e.printStackTrace(System.out));

		clonedPart.add(ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/designated_by"), 
				ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));	
		clonedPart.add(ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/inheres_in"), 
				ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));
		
		//form feature identifier
		Uni.of(FunQL::new)
			.set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
			.set(q->q.addABox(partModel))
			.set(q->q.addPlan("resources/META-INF/rules/core/clone-formfeature-identifier.rq"))
			.set(q->q.setSelectPostProcess(t->{
				//ResultSetFormatter.out(System.out, t.toResultSet(), q.getAllPrefixMapping());
				t.rows().forEachRemaining(r->{
					if(featureMap.containsKey(r.get(Var.alloc("f")))){
						Resource f = featureMap.get(r.get(Var.alloc("f")));
						Resource fi = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("fi"))));
						clonedPart.add(fi, 
								ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
								ResourceFactory.createResource("http://www.ontologyrepository.com/CommonCoreOntologies/FormFeatureIdentifier"));
						clonedPart.add(f, 
									ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/designated_by"),
									fi);
						clonedPart.add(fi, 
								ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/inheres_in"),
								ResourceFactory.createResource(r.get(Var.alloc("ibe")).getURI()));
					}
				});
				return t;
			}))
			.map(q->q.execute())
			.onFailure(e->e.printStackTrace(System.out));

		clonedPart.add(ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/concretizes"), 
				ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#ObjectProperty"));	
		
		//information quality entity
		Uni.of(FunQL::new)
			.set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
			.set(q->q.addABox(partModel))
			.set(q->q.addPlan("resources/META-INF/rules/core/clone-information-quality-entity.rq"))
			.set(q->q.setSelectPostProcess(t->{
				//ResultSetFormatter.out(System.out, t.toResultSet(), q.getAllPrefixMapping());
				t.rows().forEachRemaining(r->{
					if(featureMap.containsKey(r.get(Var.alloc("f")))){
						Resource f = featureMap.get(r.get(Var.alloc("f")));
						Resource iq = ResourceFactory.createResource(renameNode.apply(r.get(Var.alloc("iq"))));
						clonedPart.add(iq, 
								ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
								ResourceFactory.createResource("http://www.ontologyrepository.com/CommonCoreOntologies/InformationQualityEntity"));
						clonedPart.add(iq, 
									ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/inheres_in"),
									f);
						clonedPart.add(iq, 
								ResourceFactory.createProperty("http://www.ontologyrepository.com/CommonCoreOntologies/concretizes"),
								ResourceFactory.createResource(r.get(Var.alloc("s")).getURI()));
					}
				});
				return t;
			}))
			.map(q->q.execute())
			.onFailure(e->e.printStackTrace(System.out));
	}
	
}