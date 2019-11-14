package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.reader.graph.Occurrence;
import edu.ohiou.mfgresearch.reader.graph.OccurrenceTree;
import edu.ohiou.mfgresearch.reader.graph.Precedence;
import edu.ohiou.mfgresearch.reader.graph.Routing;
import edu.ohiou.mfgresearch.services.GlobalKnowledge;

public class PlanReader {

	File planPath = null;
	File partPath = null;

	PropertyReader prop = PropertyReader.getProperty();
	OntModel planTBox = ModelFactory.createOntologyModel();
	
	List<String[]> precedings = new LinkedList<String[]>();
	List<Routing> routings  = new LinkedList<Routing>();
	
	/**
	 * Part and plan RDF 
	 * Part RDF should contain specification too.
	 * @param planPath
	 * @param partPath
	 */
	public PlanReader(String planPath, String partPath) {
		this.planPath = new File(planPath);
		if(partPath.length()>0) this.partPath = new File(partPath);
		planTBox.read(prop.getIRIPath(IMPM.mfg_plan));
	}
	
	/**
	 * read plan graph into nice format
	 * @throws FileNotFoundException 
	 */
	public void readPlan() throws FileNotFoundException{		
		
		//read the part and plan
		GlobalKnowledge.setPlan();
		GlobalKnowledge.getPlan().read(planPath.getPath(), "RDF/XML");
		if(partPath != null){
			GlobalKnowledge.setPart();
			GlobalKnowledge.getPart().read(partPath.getPath(), "RDF/XML");
		}
				
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(planTBox))
		   .set(q->q.addABox(GlobalKnowledge.getPlan()))
		   .select(q->partPath != null, q->q.addABox(GlobalKnowledge.getPart()))
//		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-plan2.q"))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No feature is completely processed ");
			   if(!tab.isEmpty()){				   			
				   System.out.println("Number of routings planned : " + tab.size());
				   tab.rows().forEachRemaining(r->{
					    Node leaf = r.get(Var.alloc("pCurrent"));
					    Routing rout = new Routing(new Occurrence(leaf.getLocalName(), "", ""));
					    
					    //for every successfully processed leaf occurrence get the complete routing.
						Uni.of(FunQL::new)
						   .set(q1->q1.addTBox(planTBox))
						   .set(q1->q1.addABox(GlobalKnowledge.getPlan()))
						   .select(q1->partPath != null, q1->q1.addABox(GlobalKnowledge.getPart()))
						   .set(q1->q1.addPlan("resources/META-INF/rules/reader/read-plan3.q"))
						   .set(q1->q1.getPlan(0).addVarBinding("pCurrent", ResourceFactory.createResource(leaf.getURI())))
						   .set(q1->q1.setSelectPostProcess(tab1->{
							   if(!tab1.isEmpty()){				   			
								   System.out.println("Number of occurrences in the routing ending with " +  leaf.getLocalName()   + " : " + tab1.size());
								   //for every successfully processed leaf occurrence get the complete routing.
								   tab1.rows().forEachRemaining(r1->{
									   Precedence prec = null;
									   Node p1 = r1.get(Var.alloc("p1"));
									   Node f1 = r1.get(Var.alloc("fName1"));
									   Occurrence o1 = new Occurrence(p1.getLocalName(), "", f1.getLiteralValue().toString());
									   if(r1.contains(Var.alloc("p2"))){										   
										   Node p2 = r1.get(Var.alloc("p2"));
										   Node f2 = r1.get(Var.alloc("fName2"));
										   Occurrence o2 = new Occurrence(p2.getLocalName(), "", f2.getLiteralValue().toString());
										   prec = new Precedence(o2, o1);
										   rout.addPrec(prec);
									   }
									   else{
										   Occurrence o2 = new Occurrence("root", "", "dummy");
										   prec = new Precedence(o2, o1);
										   rout.addPrec(prec);
									   }									   
								   });
							   }
							   return tab;
						   }))
						   .set(q1->q1.execute())
						   .onFailure(e->e.printStackTrace(System.out));
						rout.calculate();
						System.out.println(rout.toString());
						routings.add(rout);
				   });

			   }
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));	

		//print all route
//		System.out.println("Routings planned are -->");
//		for(int i=0;i<routings.size();i++){
//			routings.get(i).calculate();
//			System.out.println((i+1) + " : " + routings.get(i).toString());
//		}	
		
		OccurrenceTree tree = new OccurrenceTree(routings);
		tree.writeCSV(new PrintWriter(new File(planPath.getPath().replace(".rdf", ".csv"))));
		System.out.println("CSV is written successfully!");

	}
	
//	public void readRouting(String leaf){
//		routings.stream()
//				.filter(r->{
//					return r.getLeaf().getProcess().equals(leaf);
//				})
//				.
//	}
	
	

	public static void main(String[] args) {
		
//		String planPath = "C:/Users/sarkara1/Ohio University/Sormaz, Dusan - sarkar-shared/dissertation/experiment/netexample/NetExample-features-no-processes-plan.rdf";
//		String partPath = "C:/Users/sarkara1/git/SIMPOM/impm-ind/plan/PartProcess_iter_2_part.rdf";
//		String planPath = "C:/Users/sarkara1/Ohio University/Sormaz, Dusan - sarkar-shared/dissertation/experiment/one-process/part-one-spec1-plan3.rdf";
		
//		String planPath = "C:/Users/sarkara1/git/SIMPOM/impm-ind/plan/PartProcess_iter_7_plan.rdf";
//		String partPath = "C:/Users/sarkara1/git/SIMPOM/impm-ind/plan/PartProcess_iter_7_part.rdf";
		
		String planPath = "C:/Users/sarkara1/Ohio University/Sormaz, Dusan - sarkar-shared/dissertation/experiment/slider/trial1/PartProcess_iter_5_plan.rdf";
		String partPath = "C:/Users/sarkara1/Ohio University/Sormaz, Dusan - sarkar-shared/dissertation/experiment/slider/trial1/PartProcess_iter_5_part.rdf";
		
		PlanReader pr = new PlanReader(planPath, partPath);
		try {
			pr.readPlan();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
