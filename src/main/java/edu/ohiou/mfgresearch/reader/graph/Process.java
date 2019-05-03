package edu.ohiou.mfgresearch.reader.graph;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name="process", description="reads the capabiliy of processes")
class Process implements Runnable {
	
	PropertyReader prop = new PropertyReader();
	Model m = ModelFactory.createDefaultModel();
	static Process process;
	
	@Option(names={"-g", "--graph"}, paramLabel="PATH", description="file path or URL of the RDF graph")
	private File path;	
	
	@Option(names={"-p", "--process"}, paramLabel="ProcessType", description="process type or * for every type of process")
	private String processType="";	
	
	@Option(names={"-f", "--function"}, paramLabel="FunctionType", description="function type or * for every type of function")
	private String functionType="";	
	
	public Process() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		process = new Process();
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
			CommandLine.run(process, args);
			options.clear();
		}
	}

	@Override
	public void run() {
		if(path!=null){
			m.read(path.getPath());
			System.out.printf("RDF is read from %s\n",path.getAbsolutePath());
			path=null;
		}
		if(!m.isEmpty()){
			if(processType.length()>0 && functionType.length()==0){
				readProcess(processType, "*");
			}
			else if(processType.length()==0 && functionType.length()>0){
				readProcess("*", functionType);
			}
			else if(processType.length()>0 && functionType.length()>0){
				readProcess(processType, functionType);
			}
		}
		else{
			System.out.println("Model is empty!");
		}

		processType ="";
		functionType = "";
	}

	public void readProcess(String processType, String functionType){
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-process-by-type.q"))
		   .select(q->!processType.equals("*"), q->q.getPlan(0).addVarBinding("ProcessType", ResourceFactory.createResource(processType)))
		   .select(q->!functionType.equals("*"), q->q.getPlan(0).addVarBinding("FunctionType", ResourceFactory.createResource(functionType)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No process is found for process type " + processType);
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;
	}
	
	public void readCapability(String function, String capabilityType){
		Table table = TableFactory.create();
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.capability)))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-capability-by-type.q"))
		   .set(q->q.setSelectPostProcess(tab->{
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;
	}
	
	
}
