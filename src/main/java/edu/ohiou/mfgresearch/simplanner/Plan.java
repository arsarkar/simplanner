package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.services.FeatureProcessSelection;
import edu.ohiou.mfgresearch.services.GlobalKnowledge;
import edu.ohiou.mfgresearch.services.PartProcessSelection;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class Plan implements Runnable{

	static Plan plan;
	PropertyReader prop = PropertyReader.getProperty();
	
	@Option(names={"-feature",}, paramLabel="Feature", description="Currently takes a feature name")
	private String feature="";	
	
	@Option(names={"-part",}, paramLabel="Feature", description="Currently takes a part name")
	private String part="";	
	
	@Option(names={"-g", "--graph"}, paramLabel="PATH", description="file path or URL of the part specification RDF")
	private File path;		
	
	@Option(names={"-save"}, paramLabel="", description = "file to save the plan")
	private File planPath;
	
	@Option(names={"-u", "--unit"}, paramLabel="Unit", description="set the unit, default unit is set to millimeter.")
	private String unit = "mm";
	
	public static void main(String[] args) {
		plan = new Plan();
		Scanner scanner = new Scanner(System.in);
		boolean cont=true; 
		while(cont){
//			args = scanner.nextLine().split(" ");
			Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(scanner.nextLine());
			List<String> options = new LinkedList<>();
			while (m.find()){
				options.add(m.group(1).replaceAll("\"", ""));
			}
			m.reset();
			args = options.toArray(new String[options.size()]);
			if(args[0].equals("-e")||args[0].equals("--exit")){
				cont = false;
				scanner.close();
				continue;
			}
			CommandLine.run(plan, args);
			options.clear();
		}
	}

	@Override
	public void run() {
		if(unit.length()>0){
			GlobalKnowledge.setUnit(unit);
		}
		if(path!=null){
//			GlobalKnowledge.loadSpecification(path.getAbsolutePath());
			Uni.of(path)
			   .map(p->p.getAbsolutePath())
			   .set(GlobalKnowledge::loadSpecification)
			   .onFailure(e->System.err.println("Part specification RDF could not be loaded! "+ e.getMessage()))
			   .onSuccess(p->System.out.printf("Specification RDF is read from %s\n",path.getAbsolutePath()));
			path=null;
		}
		if(feature.length()>0){
//			GlobalKnowledge.loadInitialPlan();
			GlobalKnowledge.loadStockFeature(feature);
			Uni.of(FunQL::new)
			   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
			   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
			   .set(q->q.addPlan("resources/META-INF/rules/reader/read-feature-by-name.q"))
			   .select(q->!feature.equals("*"), q->q.getPlan(0).addVarBinding("FeatureName", ResourceFactory.createPlainLiteral(feature)))
			   .set(q->q.setSelectPostProcess(tab->{
				   if(tab.isEmpty()) System.out.println("No feature is found for feature name " + feature);
				   tab.rows().forEachRemaining(r->{
					   String fName = r.get(Var.alloc("FeatureName")).toString();
					   String fType = r.get(Var.alloc("FeatureType")).toString();
					   System.out.println("Starting to plan for "+ fName + " of type " + fType);
					   FeatureProcessSelection.ask_to_plan_feature(r.get(Var.alloc("FeatureSpecification")), fType);
				   });
				   return tab;
			   }))
			   .set(q->q.execute())
			   .onFailure(e->e.printStackTrace(System.out));
			
			readPlan();
			feature = "";
		}
		if(part.length()>0){
			PartProcessSelection selection = new PartProcessSelection(new String[]{});
			selection.ask_to_plan(part.trim());
			readPlan();
			part = "";
		}
		if(planPath!=null){
			savePlan();
		}
	}
	
	public void savePlan(){
		
		try {
			OutputStream fs = new FileOutputStream(planPath);
			System.out.println("Saving the plan file at " + planPath);
			Uni.of(ModelFactory.createDefaultModel())
				.set(m->m.add(GlobalKnowledge.getPlan()))
				.set(m->m.add(GlobalKnowledge.getPart()))
				.set(m->m.add(GlobalKnowledge.getSpecification()))
				.set(m->m.write(fs))
				.onFailure(e->e.printStackTrace(System.out));
			fs.flush();
			fs.close();
			planPath = null;
			System.out.println("Plan is successfully saaved");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * read plan graph into nice format
	 */
	public void readPlan(){		
		
		List<String[]> precedings = new LinkedList<String[]>();
		
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.mfg_plan)))
		   .set(q->q.addABox(GlobalKnowledge.getPlan()))
		   .set(q->q.addABox(GlobalKnowledge.getPart()))
		   .set(q->q.addABox(GlobalKnowledge.getSpecification()))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-plan1.q"))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No feature is completely processed ");
			   if(!tab.isEmpty()){
				   			   
				   Iterator<Binding> tabIter = tab.rows();
				   while(tabIter.hasNext()){
					   Binding b = tabIter.next();
					   if(b.contains(Var.alloc("pBefore"))){
						   if(b.get(Var.alloc("pBefore")).getLocalName().contains("RootProcess")){
							   tabIter.remove();
						   }
						   else{
							   precedings.add(new String[]{b.get(Var.alloc("pBefore")).getLocalName(), b.get(Var.alloc("p1")).getLocalName(), b.get(Var.alloc("fName")).getLiteralValue().toString()});
						   }
					   }
				   }

			   }
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));	

		if(planPath!=null){
			try (PrintWriter pw = new PrintWriter(new File(planPath.getPath().replace(".rdf", ".csv")))) {
				precedings.stream().map(r->{
					return Stream.of(r).collect(Collectors.joining(","));
				})
				.forEach(pw::println);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			planPath.renameTo(new File(planPath.getPath().replace(".csv", ".rdf")));
		}
	}
}
