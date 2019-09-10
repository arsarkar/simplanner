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
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.labimp.graph.AlreadyMemberException;
import edu.ohiou.mfgresearch.labimp.graph.Graph;
import edu.ohiou.mfgresearch.labimp.graph.GraphViewer;
import edu.ohiou.mfgresearch.labimp.graph.NotMemberException;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.reader.graph.ColoredArc;
import edu.ohiou.mfgresearch.reader.graph.ColoredNode;
import edu.ohiou.mfgresearch.reader.graph.FeatureProcessLayouter;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class PartProcessSelection {

	static Logger log = LoggerFactory.getLogger(PartProcessSelection.class);
	Model localKB;
	String localPath;
	static PropertyReader prop = PropertyReader.getProperty();
	String partName = "";
	Map<Node, edu.ohiou.mfgresearch.labimp.graph.Node> nodeMap = new HashMap<Node, edu.ohiou.mfgresearch.labimp.graph.Node>();
	
	public PartProcessSelection() {
		
	}

	public Model getLocalKB() {
		return localKB;
	}
		
	public PartProcessSelection(String[] localIRI) {
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
	
//	public static Node createStockFeature(String featureName){
//		
//		if(GlobalKnowledge.getPart() == null){
//			GlobalKnowledge.setPart();
//		}
//		List<Node> ff = new LinkedList<Node>();
//		Function<Table, Table> storeFormFeatureURI = tab->{
//			ff.add(tab.rows().next().get(Var.alloc("f")));
//			return tab;
//		};
//		//two queries are required to insert the assertions in different KB
//		//create the stock feature and save to the specification KB		
//		log.info("Create stock for feature " + featureName + " ----------------------------------------------------->");
//		Uni.of(FunQL::new)
//		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
//		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
//		   .set(q->q.addPlan("resources/META-INF/rules/core/create_stock_feature2.rq"))
//		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createPlainLiteral(featureName)))
//		   .set(q->q.setLocal=true)
//		   .set(q->q.setServicePostProcess(storeFormFeatureURI))
//		   .map(q->q.execute())
//		   .map(q->q.getBelief())
//		   .map(b->b.getLocalABox())
//		   .onFailure(e->e.printStackTrace(System.out))
//		   .onSuccess(m->GlobalKnowledge.appendPartKB(m));
//		
//		if(GlobalKnowledge.getPlan() == null){
//			GlobalKnowledge.setPlan();
//		}
//		
//		//assert the stock feature is output of the root planned process (required because two unknown is not supported in FunQL yet)
//		log.info("Assert the stock as output of root process ----------------------------------------------------->");
//		Uni.of(FunQL::new)
//		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
//		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
////		   .set(q->q.addABox(KB.planKB))  
//		   .set(q->q.addABox(GlobalKnowledge.getPart())) 
//		   .set(q->q.addPlan("resources/META-INF/rules/core/create_stock_feature1.rq"))
//		   .set(q->q.getPlan(0).addVarBinding("fName", ResourceFactory.createPlainLiteral(featureName)))
//		   .set(q->q.setLocal=true)
//		   .map(q->q.execute())
//		   .map(q->q.getBelief())
//		   .map(b->b.getLocalABox())
//		   .onFailure(e->e.printStackTrace(System.out))
//		   .onSuccess(m->GlobalKnowledge.appendPlanKB(m));		
//		return ff.get(0);
//	}
	
	
	public Node ask_to_plan(String partName){
		
		//read all the features of the part and load stock feature and root process for every feature
		//log.info("Create stock and root process tree for each feature----------------------------------------------------->");
//		Uni.of(FunQL::new)
//		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
//		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
//		   .set(q->q.addPlan("resources/META-INF/rules/core/create-stock-for-features.rq"))
//		   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
//		   .set(q->q.setLocal=true)
//		   .map(q->q.execute())
//		   .map(q->q.getBelief())
//		   .map(b->b.getLocalABox())
//		   .onFailure(e->e.printStackTrace(System.out))
//		   .onSuccess(m->localKB.add(m));
		this.partName = partName;
		//Initial plan
		log.info("load initial root for the part-planning occurrence tree");
		Node iProcess = GlobalKnowledge.loadInitialPlan();
		
		//Root Feature
		log.info("load root feature and representation");
		Node rootFeature = GlobalKnowledge.loadRootFeature();
		
		//transform feature precedence (add root representation)
		log.info("transform feature precedence");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
		   .set(q->q.addABox(GlobalKnowledge.getPart()))
		   .set(q->q.addABox(GlobalKnowledge.getPlan())) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/transform_feature_precedence.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->GlobalKnowledge.appendSpecificationKB(m));
		
		//feature precedence graph appender
		Graph g = new Graph();
		FeatureProcessLayouter fpl =  new FeatureProcessLayouter(g, 10.0, 3, 5, 1.3, false);
		GraphViewer v = new GraphViewer(g, fpl, GraphViewer.VIEW_2D);
		
		if(Boolean.parseBoolean(prop.getProperty("SHOW_FEATURE_GRAPH").trim())) 
			v.display();
		

		
		Function<Table, Table> plotProcessSelectionTree = tab->{
			if(!Boolean.parseBoolean(prop.getProperty("SHOW_FEATURE_GRAPH").trim())) return tab;

			if(fpl.getRank()>0) fpl.nextOrbit();
			
			if(fpl.getRank()==0) {
				//add root feature
				edu.ohiou.mfgresearch.labimp.graph.Node rootNode = new edu.ohiou.mfgresearch.labimp.graph.Node (new ColoredNode(rootFeature.getLocalName(), Color.BLACK));
				nodeMap.put(tab.rows().next().get(Var.alloc("rc")), rootNode);
				g.addNode(rootNode);
				fpl.nextOrbit();
			}			
			
			List<Map<Var, Node>> children = new LinkedList<Map<Var, Node>>();
			tab.rows().forEachRemaining(b->{
				Map<Var, Node> row  = new HashMap<Var, Node>();
				row.put(Var.alloc("fs"), b.get(Var.alloc("fs")));
				row.put(Var.alloc("fn"), b.get(Var.alloc("fn")));
				row.put(Var.alloc("ft"), b.get(Var.alloc("ft")));
				row.put(Var.alloc("rNext"), b.get(Var.alloc("rNext")));
				row.put(Var.alloc("rc"), b.get(Var.alloc("rc")));
//				row.put(Var.alloc("pCurrent"), b.get(Var.alloc("pCurrent")));
				if (!children.contains(row)){
					children.add(row);
				}
			});			
			int numChildren = children.size();
			fpl.setNumPlanets(numChildren);
			
			Map<Node, edu.ohiou.mfgresearch.labimp.graph.Node> nodeMap1 = new HashMap<Node, edu.ohiou.mfgresearch.labimp.graph.Node>();
			
			children.forEach(b->{
				String featureNodelabel = b.get(Var.alloc("fs")).getLocalName() + "(" + b.get(Var.alloc("fn")).getLiteralValue().toString() + "|" + 
											b.get(Var.alloc("ft")).getLiteralValue().toString() + ")";
				edu.ohiou.mfgresearch.labimp.graph.Node featureNode = new edu.ohiou.mfgresearch.labimp.graph.Node (new ColoredNode(featureNodelabel, Color.ORANGE));

				g.addNode(featureNode);
				
				//find the parent node
				edu.ohiou.mfgresearch.labimp.graph.Node parentNode = nodeMap.get(b.get(Var.alloc("rc")));
//				edu.ohiou.mfgresearch.labimp.graph.Node parentNode = null;
//				if(fpl.getRank()==1){
//					parentNode = nodeMap.get(nodeMap.keySet().iterator().next());
//				}
//				else{
//					for(Node n: nodeMap.keySet()){
//						boolean isSuccess =
//						Uni.of(FunQL::new)
//						   .set(q->q.addTBox(GlobalKnowledge.getPlanTBox()))
//						   .set(q->q.addABox(GlobalKnowledge.getPlan()))
//						   .set(q->q.addPlan("resources/META-INF/rules/core/feature-precedence-1.rq"))
//						   .set(q->q.getPlan(0).addVarBinding("pAncestor", ResourceFactory.createResource(n.getURI())))
//						   .set(q->q.getPlan(0).addVarBinding("pCurrent", ResourceFactory.createResource(b.get(Var.alloc("pCurrent")).getURI())))
//						   .set(q->q.setLocal=true)
//						   .map(q->q.execute())
//						   .map(q->q.isQuerySuccess())
//						   .get();	
//						if(isSuccess){
//							parentNode = nodeMap.get(n);
//							break;
//						}					
//					}
//				}
				//add arc from parent node
				try {
					g.addDirectedArc(parentNode.getUserObject(), featureNode.getUserObject(), new ColoredArc("precedes", Color.GREEN));
				} catch (AlreadyMemberException | NotMemberException e1) {
					e1.printStackTrace();
				}				
				nodeMap1.put(b.get(Var.alloc("rNext")), featureNode);
 			});
			nodeMap.clear();
			nodeMap.putAll(nodeMap1);
			nodeMap1.clear();
			fpl.repositionEdges();
			return tab;
		};
	
		
		//plan every feature
		log.info("Create occurrence tree for each feature----------------------------------------------------->");
		boolean stopIteration = false;
		int counter = 0;
		while(!stopIteration){
			log.info("Part process planning at iteration " + counter);
			
			String fileName = "C:/Users/sarkara1/git/SIMPOM/plan/plan_after_" + counter + ".rdf";
			Uni.of(ModelFactory.createDefaultModel())
			   .set(m->m.add(GlobalKnowledge.getSpecification()))
			   .set(m->m.add(GlobalKnowledge.getPlan()))
			   .set(m->m.add(GlobalKnowledge.getPart()))
			   .set(m->m.write(new FileOutputStream(new File(fileName)), "RDF/XML"));
			
			boolean	isSuccessful =
				Uni.of(FunQL::new)
				   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
				   .set(q->q.addTBox(GlobalKnowledge.getResourceTBox()))
				   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
				   .set(q->q.addABox(GlobalKnowledge.getPart()))
				   .set(q->q.addABox(GlobalKnowledge.getPlan())) 
				   .set(q->q.addPlan("resources/META-INF/rules/core/feature-precedence-1.rq"))
//				   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
				   .set(q->q.setLocal=true)
				   .set(q->q.setSelectPostProcess(plotProcessSelectionTree.andThen(tab->{
					   if(Boolean.parseBoolean(prop.getProperty("SHOW_SELECT_RESULT").trim())){
						   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
					   }
					   if(!tab.isEmpty()){
						   log.info("-----------------------------------------------------------------------------------------------------------------------");
						   tab.rows().forEachRemaining(r->{
							   log.info("After occurrent occurrences for " + r.get(Var.alloc("pCurrent")).getLocalName() + " occurrence sub tree for feature specification "+
									   r.get(Var.alloc("fs")).getLocalName() + "(" + r.get(Var.alloc("ft")).getLiteralValue() + ") can be planned");
						   });
					   }
					   return tab;
				   })))
				   .set(q->q.setServicePostProcess(tab->{
					   if(Boolean.parseBoolean(prop.getProperty("SHOW_CONSTRUCT_RESULT").trim())){
						   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping());
					   }
					   if(!tab.isEmpty()){
						   tab.rows().forEachRemaining(r->{
							   log.info("\n root of sub-occurrence-tree "+ r.get(Var.alloc("pNext")).getLocalName() + " is added as succeeding occurrence of "+ r.get(Var.alloc("pCurrent")).getLocalName());
						   });
						   log.info("-----------------------------------------------------------------------------------------------------------------------");
					   }
					   return tab;
				   }))
				   .map(q->q.execute())
				   .set(q->GlobalKnowledge.appendPlanKB(q.getBelief().getLocalABox()))
				   .map(q->q.isQuerySuccess())
				   .get();	
			counter++;
			stopIteration = !isSuccessful;
		}
		execute();
		return null;
	}
	
	public void createFeaturePrecedenceNetwork(String partName){
		//feature precedence network (add a start node)
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(GlobalKnowledge.getDesignTBox()))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification())) 
		   .set(q->q.addPlan("resources/META-INF/rules/core/feature-precedence-0.rq"))
		   .set(q->q.getPlan(0).addVarBinding("pName", ResourceFactory.createPlainLiteral(partName)))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));		
	}

	public void execute(){
		
		
		
	}
	
}
