package edu.ohiou.mfgresearch.services;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.labimp.graph.Graph;
import edu.ohiou.mfgresearch.labimp.graph.GraphViewer;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.reader.graph.FeatureProcessLayouter;
import edu.ohiou.mfgresearch.reader.graph.AnonGraph;
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
	
	static Map<String, AnonGraph> featurePlans = new HashMap<String, AnonGraph>();
	
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

		if(featureType.equals("Hole")){
			return ask_to_select_holemaking_processes(featureSpecification);
		}
		else if(featureType.equals("Slot")){
			return ask_to_select_open_slotmaking_processes(featureSpecification);
		}
		else if(featureType.equals("Slab")){
			return ask_to_select_slabmaking_processes(featureSpecification);
		}
		else if(featureType.equals("Pocket")){
			return ask_to_select_open_pocketmaking_processes(featureSpecification);
		}
//		else if(featureType.equals("http://www.ohio.edu/ontologies/design#Chamfer")){
//			return ask_to_select_pocketmaking_processes(featureSpecification);
//		}
		else{
			return new Node[0];
		}
	}
	
	private void createStockFeature(Node featureSpecification) {
		if(GlobalKnowledge.getPart()==null){
			GlobalKnowledge.setPart();
		}
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
		   .onSuccess(m->GlobalKnowledge.appendPartKB(m));
		
		//assert the stock feature is output of the root planned process
		System.out.println("\n## Assert root feature as output of the root process.");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))  
		   .set(q->q.addABox(GlobalKnowledge.getPart())) 
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
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getPlan()))
		   .set(q->q.addABox(localKB))
		   .set(q->q.addPlan("resources/META-INF/rules/core/select-root-processes.q"))
		   .set(q->q.setLocal=true)
		   .set(q->q.getPlan(0).addVarBinding("p0", ResourceFactory.createResource(processNodes.get(0).getURI())))
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
		if(processNodes.size()>1){
			return processNodes.subList(1, processNodes.size()).toArray(new Node[0]);
		}
		else{
			return null;
		}
	}
	
	/**
	 * Service to plan holemaking
	 * @param featureName
	 */
	public static Node[] ask_to_select_holemaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();
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
		   .set(q->q.addPlan("resources/META-INF/rules/core/process-precedence-drilling-wo-holestarting.q"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->fpSel.localKB.add(m));
		
		fpSel.execute();
		
		return fpSel.getRootProcesses();
	}

	/**
	 * service to plan slotmaking
	 * @param featureName
	 */
	public static Node[] ask_to_select_open_slotmaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();
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
		   .set(q->q.addPlan("resources/META-INF/rules/core/process-precedence-openslot.q"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->fpSel.localKB.add(m));
		
		fpSel.execute();
		
		return fpSel.getRootProcesses();
	}
	
	
	/**
	 * service to plan pocket making
	 * @param featureName
	 */
	public static Node[] ask_to_select_open_pocketmaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();
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
		   .set(q->q.addPlan("resources/META-INF/rules/core/process-precedence-openpocket.q"))	
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

		return fpSel.getRootProcesses();
	}

	/**
	 * service to plan slabmaking
	 * @param featureName
	 */
	public static Node[] ask_to_select_slabmaking_processes(Node featureSpecification){
		
		FeatureProcessSelection fpSel = new FeatureProcessSelection(new String[]{});
		fpSel.featureSpec = featureSpecification.getLocalName();
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
		   .set(q->q.addPlan("resources/META-INF/rules/core/process-precedence-slab.q"))	
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
		
		return fpSel.getRootProcesses();
	}
	
	public void execute(){		
		
		//get the latest process planned 
		boolean stopIteration = false;

		Graph g = new Graph();
		FeatureProcessLayouter fpl =  new FeatureProcessLayouter(g, 10.0, 3, 5, 1.3, true);
		
		GraphViewer v = new GraphViewer(g,fpl, GraphViewer.VIEW_2D);
		if(Boolean.parseBoolean(prop.getProperty("SHOW_PROCESS_GRAPH").trim())) 
			v.display();
		
		//function to select result of process-plan-1 rule and plot in FeatureProcessLayouter
		Function<Table, Table> plotProcessSelectionTree = tab->{
			if(!Boolean.parseBoolean(prop.getProperty("SHOW_PROCESS_GRAPH").trim())) return tab; 
			int numChildren = tab.size();
			if(fpl.getRank()>0) fpl.nextOrbit();
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
					   .set(q->q.addABox(GlobalKnowledge.getPart()))
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
							   String ft = tab1.rows().next().get(Var.alloc("ft")).getURI();
							   //get satisfied dimension and tolerances
							   StringBuilder s = new StringBuilder();
							   s.append(" concretizes ");
							   tab1.rows().forEachRemaining(b1->{
								   s.append(" ").append(b1.get(Var.alloc("d")).getLocalName());
							   });

							   ColoredNode cn = null; 
							   if(ft.equals("http://www.ohio.edu/ontologies/design#IntermediateFormFeature")){
								   cn = new ColoredNode(iFeature.getLocalName(), Color.BLUE);
								   cn.setTooltip(s.toString());
							   }
							   else if(ft.equals("http://www.ohio.edu/ontologies/design#UnsatisfiedFeature")){
								   cn = new ColoredNode(iFeature.getLocalName(), Color.RED);
								   cn.setTooltip(s.toString());
							   }
							   else if(ft.equals("http://www.ohio.edu/ontologies/design#FinalFeature")){
								   cn = new ColoredNode(iFeature.getLocalName(), Color.CYAN);
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
			log.info("match feature by process-planning-1.rq. iteration ---> " + counter);
			boolean	isSuccessful = 	
					Uni.of(FunQL::new)
					   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
					   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
					   .set(q->q.addABox(localKB))
					   .set(q->q.addABox(GlobalKnowledge.getPart()))
					   .set(q->q.addABox(GlobalKnowledge.getPlan()))
					   .set(q->q.addPlan("resources/META-INF/rules/core/process-planning-1.rq"))
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
						   GlobalKnowledge.appendPlanKB(q.getBelief().getLocalABox());   
					   })
					   .map(q->q.isQuerySuccess())
					   .get();			
			
			
			stopIteration = !isSuccessful;
		
		}
	}
	
	
	
}
