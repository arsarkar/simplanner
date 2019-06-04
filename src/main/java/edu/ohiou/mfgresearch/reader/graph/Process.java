package edu.ohiou.mfgresearch.reader.graph;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.ResultSetUtils;

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
	ProcessCapabilityGraph pcg = null;
	
	public Process() {
	}
	
	Model m = ModelFactory.createDefaultModel();
	static Process process;
	
	@Option(names={"-g", "--graph"}, paramLabel="PATH", description="file path or URL of the RDF graph")
	private File path;	
	
	@Option(names={"-save"})
	private boolean save = false;
	
	@Option(names={"-p", "--process"}, paramLabel="ProcessURI", description="process type or * for every type of process. When used as new pass a process individual URI.")
	private String processURI="";	
	
	@Option(names={"-func", "--function"}, paramLabel="FunctionURI", description="function type or * for every type of function")
	private String functionURI="";	
	
	@Option(names={"-capa", "--capability"}, paramLabel="CapabilityType", description="function type or * for every type of function")
	private String capaType="";	
	
//	@Option(names={"-prof", "--profile"}, paramLabel="profile", description="a particular capability profile represented by an individual of type processualFunction or its sub-type")
//	private String profile="";	
	
	@Option(names={"-mach", "--machine"}, paramLabel="Machine", description="a particular machine represented by an individual of type machine or its sub-type")
	private String machine="";
	
	@Option(names={"-tool"}, paramLabel="Tool", description="a particular tool represented by an individual of type Tool or its sub-type")
	private String tool="";
	
	@Option(names={"-new", "--create"}, description="Add/modify capability for a process. the capability is added to new function if '-func' is provided")
	private boolean create = false;
	
	
	@Option(names = "-min") double minLimit=Double.NaN;
	@Option(names = "-max") double maxLimit=Double.NaN;		
	@Option(names = "-ref") String reference;
	
	@ArgGroup(exclusive = false, multiplicity = "0..1")
	private Equation equation;
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
			if(save){
				Uni.of(path)
				   .map(p->p.getPath())
				   .map(File::new)
				   .map(FileOutputStream::new)
				   .map(fs->m.write(fs, "RDF/XML"))
				   .onFailure(e->System.err.println(e.getMessage()))
				   .onSuccess(m->System.out.printf("RDF is saved at %s\n",path.getAbsolutePath()));
				save = false;
			}
			else{
				Uni.of(path)
				   .map(p->p.getPath())
				   .map(pa->m.read(pa))
				   .onFailure(e->System.err.println(e.getMessage()))
				   .onSuccess(m->System.out.printf("RDF is read from %s\n",path.getAbsolutePath()));				
			}
			path=null;
		}
		//read process function capability
		if(!m.isEmpty()){
			if(processURI.length()>0 && functionURI.length()==0){
				readProcessByType(processURI, "*");
			}
			else if(processURI.length()==0 && functionURI.length()>0){
				readProcessByType("*", functionURI);
			}
			else if(processURI.length()>0 && functionURI.length()>0){
				readProcessByType(processURI, functionURI);
			}
		}
		else{
			if(!create) System.out.println("Model is empty!");
		}
		//if create is true then proceed for creating a new model or modify existing
		if(create){
			try {
				pcg = new ProcessCapabilityGraph(prop.getProperty("CAPABILITY_ABOX"), null);
				createCapability();
			} catch (Exception e) {
				System.err.println("Failed to create capability due to \n"+e.getMessage());
			}
			create = false;
			minLimit=Double.NaN;
			maxLimit=Double.NaN;		
			return;
		}
		
		processURI ="";
		functionURI = "";
		capaType = "";
	}
	
	
	
	private boolean isTypeURI(String uri){
		return tBox.getOntClass(uri)!=null;
	}

	/**
	 * Create new or modify exisiting capability
	 * @throws Exception 
	 */
	private void createCapability() throws Exception {
		
		if(processURI.length()==0 || processURI.trim().equals("*")){
			System.err.println("need at least a process type or process instance to continue...");
			return;
		}
		if(functionURI.length()==0|| functionURI.trim().equals("*")){
			System.err.println("provide the function or function instance to continue...");
			return;
		}	
		
		String functionIns = "";
		if(isTypeURI(functionURI)){
			String processIns = "";
			if(isTypeURI(processURI)){
				processIns = pcg.createNewProcess(processURI);
				System.out.println("new process instance " + processIns + " of type "+ processURI + " added.");
			}
			else{
				processIns = processURI;
			}
			functionIns = pcg.createNewFunction(functionURI.trim(), processIns).getURI();
			System.out.println("new function added " + functionIns);
			m = pcg.getCapabilityKB();
			readProcess(processIns.trim(), functionIns);
		}
		else{
			functionIns = functionURI.trim();
		}
		if(capaType.length()>0){
			//check limits 
			if(maxLimit==Double.NaN && minLimit==Double.NaN){
				System.err.println("need at least a min or max limit");
			}
			else{
				Node maxICE = null, minICE = null;
				if(equation!=null && minLimit!=Double.NaN){
					System.out.println("Setting max limit = " + equation.eqn.trim() + ", min limit = " + minLimit);
					//process equation as max 
					maxICE = pcg.createCapabilityEquation(equation.eqn.trim(), findArgs(equation.eqn.trim(), equation.argms), true);
					minICE = pcg.createCapabilityLimit(minLimit, false);
				}
				else if(equation!=null && maxLimit!=Double.NaN){
					System.out.println("Setting max limit = " + maxLimit + ", min limit = " + equation.eqn.trim());
					//process equation as min
					maxICE = pcg.createCapabilityLimit(maxLimit, true);
					minICE = pcg.createCapabilityEquation(equation.eqn.trim(), findArgs(equation.eqn.trim(), equation.argms), false);
				}
				else if(maxLimit!=Double.NaN && minLimit!=Double.NaN){
					System.out.println("Setting max limit = " + maxLimit + ", min limit = " + minLimit);
					//both limits are crisp
					maxICE = pcg.createCapabilityLimit(maxLimit, true);
					minICE = pcg.createCapabilityLimit(minLimit, false);
				}
				else{
					System.err.println("Not enough data is provided to proceed...");
				}

				Node capabilityIns = pcg.crateCapability(ResourceFactory.createResource(functionIns).asNode(), 
											capaType.trim(), reference.trim(), maxICE, minICE);
				System.out.println("New Capability added to profile "+ capabilityIns.getURI());
				m = pcg.getCapabilityKB();
				readCapability(functionIns, capaType);
			}			
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
		if(options.size()>0){
			for(int i= 0; i<options.size();i++){
				argTypes.put(options.get(i), argms[i]);
			}
		}
		return argTypes;
	}
	
	public void readProcessByType(String processType, String functionType){
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
			   }
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));
	}
	
	public void readProcess(String processIns, String functionIns){
		Uni.of(FunQL::new)
			.set(q->q.addTBox(tBox))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-process-by-type.q"))
		   .set(q->q.getPlan(0).addVarBinding("Process", ResourceFactory.createResource(processIns)))
		   .set(q->q.getPlan(0).addVarBinding("Function", ResourceFactory.createResource(functionIns)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
			   else System.out.println("No process is found for process " + processIns);
			   //get all capabilities 
			   if(capaType.length()>0){
				   tab.rows().forEachRemaining(r->{
					   String f = r.get(Var.alloc("Function")).getURI();
					   readCapability(f, capaType);
				   });
			   }
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));
	}
	
	public void readCapability(String function, String capabilityType){
		
		Uni.of(FunQL::new)
		.set(q->q.addTBox(tBox))
		.set(q->q.addABox(m))
	   .set(q->q.addPlan("resources/META-INF/rules/reader/read-capability.q"))
	   .select(q->!function.equals("*"), q->q.getPlan(0).addVarBinding("func", ResourceFactory.createResource(function)))
	   .select(q->!capabilityType.equals("*"), q->q.getPlan(0).addVarBinding("Capability", ResourceFactory.createResource(capabilityType)))
	   .set(q->q.setSelectPostProcess(tab->{
		   if(tab.isEmpty()) System.out.println("No capability is found for capability type " + capabilityType);
		   return tab;
	   }))
	   .set(q->q.execute())
	   .onFailure(e->e.printStackTrace(System.out));
		
		Uni.of(FunQL::new)
			.set(q->q.addTBox(tBox))
			.set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-capability-measurement.q"))
		   .select(q->!function.equals("*"), q->q.getPlan(0).addVarBinding("func", ResourceFactory.createResource(function)))
		   .select(q->!capabilityType.equals("*"), q->q.getPlan(0).addVarBinding("Capability", ResourceFactory.createResource(capabilityType)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet(), q.getAllPrefixMapping()); 
//			   else System.out.println("No capability is found for capability type " + capabilityType);
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
//			   else System.out.println("No capability is found for capability type " + capabilityType);
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
//			   else System.out.println("No capability is found for capability type " + capabilityType);
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out));		
	}
	
}
