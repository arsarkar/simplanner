package edu.ohiou.mfgresearch.services;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.labimp.graph.Graph;
import edu.ohiou.mfgresearch.labimp.graph.GraphViewer;
import edu.ohiou.mfgresearch.labimp.graph.NotMemberException;
import edu.ohiou.mfgresearch.labimp.graph.AlreadyMemberException;
import edu.ohiou.mfgresearch.labimp.graph.Arc;
import edu.ohiou.mfgresearch.labimp.graph.DirectedArc;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.reader.graph.FeatureProcessLayouter;
import edu.ohiou.mfgresearch.simplanner.IMPM;

public class FeatureProcessSelection {

	static Logger log = LoggerFactory.getLogger(FeatureProcessSelection.class);
	
	Model localKB;
	String localPath;
	PropertyReader prop = new PropertyReader();
	FeatureProcessMatching matchingService;
	
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
	
	public void loadProcessPrecedence(){
		System.out.println("\n||"+this.getClass().getSimpleName()+"||>>"+"loading process precedence by rule process-precedence-drilling-wo-holestarting.q");
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.mfg_plan)))
		   .set(q->q.addABox(prop.getProperty("CAPABILITY_ABOX_MM")))
		   .set(q->q.addPlan("resources/META-INF/rules/core/process-precedence-drilling-wo-holestarting.q"))
		   .set(q->q.setLocal=true)
		   .map(q->q.execute())
		   .map(q->q.getBelief())
		   .map(b->b.getLocalABox())
		   .onFailure(e->e.printStackTrace(System.out))
		   .onSuccess(m->localKB.add(m));
	}
	
	public void ask_to_select_processes(String featureName){
		
		//register known matching services and load specification to them
//		matchingService = new FeatureProcessMatching(new String[]{});	
		//is it required for this agent to know specification? I don't think so
//		matchingService.loadLocalKB(loadSpecifications(featureName)); 
		//load the plan KB 
		//localKB.add(GlobalKnowledge.getPlan());
		execute();
	}
	
	public void execute(){
		
		//load process precedence
		loadProcessPrecedence();		
		
		//get the latest process planned 
		boolean stopIteration = false;

		Graph g = new Graph();

//		FeatureProcessLayouter fpl =  new FeatureProcessLayouter(g, new Point2D.Double(0,0));

		FeatureProcessLayouter fpl =  new FeatureProcessLayouter(g, 10.0);

		GraphViewer v = new GraphViewer(g,fpl, GraphViewer.VIEW_2D);
		v.display();
		int counter = 0;
//		
//		List<edu.ohiou.mfgresearch.labimp.graph.Node> nodes = new LinkedList<edu.ohiou.mfgresearch.labimp.graph.Node>();
//		List<Arc> arcs = new LinkedList<Arc>();

//		
//>>>>>>> df48bb43aecb02eab998c0e3b45cf21787386286

		while(!stopIteration){
			counter += 1;
//			System.out.println("\n||"+this.getClass().getSimpleName()+"||>>"+"match feature by process-planning-1.rq. iteration ---> " + counter);
			boolean	isSuccessful = 	
					Uni.of(FunQL::new)
					   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
					   .set(q->q.addTBox(prop.getIRIPath(IMPM.mfg_plan)))
					   .set(q->q.addABox(localKB))
					   .set(q->q.addABox(GlobalKnowledge.getPart()))
					   .set(q->q.addABox(GlobalKnowledge.getPlan()))
					   .set(q->q.addPlan("resources/META-INF/rules/core/process-planning-1.rq"))
					   .set(q->q.setLocal=true)

					   .set(q->q.setServicePostProcess(tab->{
						   int numChildren = tab.size();
						   if(fpl.getRank()>0) fpl.nextOrbit();
						   fpl.setNumPlanets(numChildren);
						   tab.rows().forEachRemaining(b->{
							   Node parent = b.get(Var.alloc("pCurrent"));	
							   if(fpl.getRank()==0) {
								   g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (parent.getLocalName()));
								   fpl.nextOrbit();
							   }
							   Node child = b.get(Var.alloc("pNext1"));
							   if (!g.hasObject(child.getLocalName())) {
								   g.addNode(new edu.ohiou.mfgresearch.labimp.graph.Node (child.getLocalName()));
								   Uni.of(g)
								   	  .set(g1->g1.addDirectedArc(parent.getLocalName(), child.getLocalName(), new String("precedes")))
								   	  .onFailure(e->e.printStackTrace(System.out));
							   }

						   });
						   return tab;
					   }))
					   .map(q->q.execute())
					   .set(q->GlobalKnowledge.appendPlanKB(q.getBelief().getLocalABox()))
					   .map(q->q.isQuerySuccess())
					   .get();	
			
			//display the new planned processes in the tree display

			stopIteration = !isSuccessful;

		
		}
	}
	
	
	
}
