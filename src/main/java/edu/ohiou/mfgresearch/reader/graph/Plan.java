package edu.ohiou.mfgresearch.reader.graph;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.services.FeatureProcessSelection;
import edu.ohiou.mfgresearch.services.GlobalKnowledge;
import edu.ohiou.mfgresearch.simplanner.IMPM;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class Plan implements Runnable{

	static Plan plan;
	PropertyReader prop = PropertyReader.getProperty();
	
	@Option(names={"-feature",}, paramLabel="Feature", description="Currently takes a feature name, but may change in future")
	private String feature="";	
	
	@Option(names={"-g", "--graph"}, paramLabel="PATH", description="file path or URL of the part specification RDF")
	private File path;	
	
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
		if(path!=null){
			GlobalKnowledge.loadSpecification(path.getAbsolutePath());
			Uni.of(path)
			   .map(p->p.getAbsolutePath())
			   .set(GlobalKnowledge::loadSpecification)
			   .onFailure(e->System.err.println("Part specification RDF could not be loaded! "+ e.getMessage()));
			System.out.printf("Specification RDF is read from %s\n",path.getAbsolutePath());
			path=null;
		}
		if(feature.length()>0){
			GlobalKnowledge.loadInitialPlan();
			GlobalKnowledge.loadStockFeature(feature);
			FeatureProcessSelection selection = new FeatureProcessSelection(new String[]{});
			selection.ask_to_select_holemaking_processes(NodeFactory.createBlankNode());
			readPlan();
			feature = "";
		}
		
	}
	
	public void readPlan(){
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.mfg_plan)))
		   .set(q->q.addABox(GlobalKnowledge.getPlan()))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-plan1.q"))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No feature is completely processed ");
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));	
	}
}
