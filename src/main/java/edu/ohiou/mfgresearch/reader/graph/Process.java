package edu.ohiou.mfgresearch.reader.graph;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import edu.ohiou.mfgresearch.simplanner.IMPM;
import edu.ohiou.mfgresearch.simplanner.ProcessCapabilityGraph;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name="process", description="reads the capabiliy of processes")
class Process implements Runnable {

	PropertyReader prop = new PropertyReader();
	OntModel tBox = Uni.of(ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM))
		 				.set(model->model.read(prop.getIRIPath(IMPM.capability)))
		 				.get();
	
	public Process() {
	}
	
	Model m = ModelFactory.createDefaultModel();
	static Process process;
	
	@Option(names={"-g", "--graph"}, paramLabel="PATH", description="file path or URL of the RDF graph")
	private File path;	
	
	@Option(names={"-p", "--process"}, paramLabel="ProcessType", description="process type or * for every type of process")
	private String processType="";	
	
	@Option(names={"-func", "--function"}, paramLabel="FunctionType", description="function type or * for every type of function")
	private String functionType="";	
	
	@Option(names={"-capa", "--capability"}, paramLabel="CapabilityType", description="function type or * for every type of function")
	private String capaType="";	
	
	@Option(names={"-new", "--create"}, description="Add/modify capability for a process. the capability is added to new function if '-func' is provided")
	private boolean create = false;
	
	@ArgGroup(exclusive = true, multiplicity = "0..1")
    CapaLimit capaLim;
	
	static class CapaLimit{
		@ArgGroup(exclusive = false, heading="enter equation for capability min limit")
		Equation eq;
		@Option(names = "-min", required=false) double minLimit=Double.NaN;
		@Option(names = "-max", required=false) double maxLimit=Double.NaN;		
		@Option(names = "-ref", required=false) String reference;
	}
	
	static class Equation {
		@Option(names = "-eq", required=false) String eqn;
		@Parameters(arity = "1..*") String[] argms;
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
			//if create is true then proceed for creating a new model or modify existing
			if(create){
				try {
					createCapability();
				} catch (Exception e) {
					System.err.println("Failed to create capability due to \n"+e.getMessage());
				}
				create = false;
				capaLim.minLimit=Double.NaN;
				capaLim.maxLimit=Double.NaN;		
				return;
			}
			m.read(path.getPath());
			System.out.printf("RDF is read from %s\n",path.getAbsolutePath());
			path=null;
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
		}
		processType ="";
		functionType = "";
	}

	/**
	 * Create new or modify exisiting capability
	 * @throws Exception 
	 */
	private void createCapability() throws Exception {
		if(processType.length()>0 && functionType.length()>0){
			//check limits 
			if(capaLim.maxLimit==Double.NaN && capaLim.minLimit==Double.NaN){
				System.err.println("need at least a min or max limit");
			}
			else{
				//create new ProcessCapabilityGraph
				ProcessCapabilityGraph pcg = new ProcessCapabilityGraph(prop.getProperty("CAPABILITY_ABOX"));
				Node maxICE, minICE;
				if(capaLim.eq.eqn.trim().length()>0 && capaLim.minLimit!=Double.NaN){
					//process equation as max 
					maxICE = pcg.createCapabilityEquation(capaLim.eq.eqn.trim(), findArgs(capaLim.eq.eqn.trim(), capaLim.eq.argms), true);
					minICE = pcg.createCapabilityLimit(capaLim.minLimit, false);
				}
				else if(capaLim.eq.eqn.trim().length()>0 && capaLim.maxLimit!=Double.NaN){
					//process equation as min
					maxICE = pcg.createCapabilityLimit(capaLim.maxLimit, true);
					minICE = pcg.createCapabilityEquation(capaLim.eq.eqn.trim(), findArgs(capaLim.eq.eqn.trim(), capaLim.eq.argms), false);
				}
				else{
					//both limits are crisp
					maxICE = pcg.createCapabilityLimit(capaLim.maxLimit, true);
					minICE = pcg.createCapabilityLimit(capaLim.minLimit, false);
				}
			}
		}
		else{
			System.err.println("need a process type and a function type to create new capability.");
		}
	}

	/**
	 * Find arguments in the equation and pair them with the supplied types
	 * @param equation
	 * @param argms
	 * @return
	 */
	private Map<String, String> findArgs(String equation, String[] argms){
		Map<String, String> argTypes = new HashMap<String, String>();
		Matcher m = Pattern.compile("(\\?[A-Za-z0-9]+)").matcher(equation);
		List<String> options = new LinkedList<>();
		while (m.find()){
			options.add(m.group().replaceAll("\"", ""));
		}
		m.reset();
		for(int i= 0; i<options.size();i++){
			argTypes.put(options.get(i), argms[i]);
		}
		return argTypes;
	}
	
	public void readProcess(String processType, String functionType){
		Uni.of(FunQL::new)
			.set(q->q.addTBox(tBox))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-process-by-type.q"))
		   .select(q->!processType.equals("*"), q->q.getPlan(0).addVarBinding("ProcessType", ResourceFactory.createResource(processType)))
		   .select(q->!functionType.equals("*"), q->q.getPlan(0).addVarBinding("FunctionType", ResourceFactory.createResource(functionType)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No process is found for process type " + processType);
			   //get all capabilities 
			   if(capaType.length()>0){
				   tab.rows().forEachRemaining(r->{
					   String f = r.get(Var.alloc("Function")).getURI();
					   readCapability(f, capaType);
				   });
				   capaType = "";
			   }
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));
	}
	
	public void readCapability(String function, String capabilityType){
		Table table = TableFactory.create();
		Uni.of(FunQL::new)
			.set(q->q.addTBox(tBox))
			.set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-capability-measurement.q"))
		   .select(q->!function.equals("*"), q->q.getPlan(0).addVarBinding("func", ResourceFactory.createResource(function)))
		   .select(q->!capabilityType.equals("*"), q->q.getPlan(0).addVarBinding("Capability", ResourceFactory.createResource(capabilityType)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No capability is found for capability type " + capabilityType);
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));
		
		Uni.of(FunQL::new)
			.set(q->q.addTBox(tBox))
			.set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-capability-limit-equation.q"))
		   .select(q->!function.equals("*"), q->q.getPlan(0).addVarBinding("func", ResourceFactory.createResource(function)))
		   .select(q->!capabilityType.equals("*"), q->q.getPlan(0).addVarBinding("Capability", ResourceFactory.createResource(capabilityType)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No capability is found for capability type " + capabilityType);
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));
		
		Uni.of(FunQL::new)
			.set(q->q.addTBox(tBox))
			.set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-capability-equation-limit.q"))
		   .select(q->!function.equals("*"), q->q.getPlan(0).addVarBinding("func", ResourceFactory.createResource(function)))
		   .select(q->!capabilityType.equals("*"), q->q.getPlan(0).addVarBinding("Capability", ResourceFactory.createResource(capabilityType)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No capability is found for capability type " + capabilityType);
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));
		
	}
	
}
