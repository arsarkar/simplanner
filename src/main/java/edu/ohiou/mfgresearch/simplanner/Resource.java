package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name="resource", description="create, read and assign resources")
public class Resource implements Runnable {

	PropertyReader prop = PropertyReader.getProperty();
	OntModel tBox = Uni.of(ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM))
		 				.set(model->model.read(prop.getIRIPath(IMPM.capability)))
		 				.get();
	
	@Option(names={"-g", "--graph"}, paramLabel="PATH", description="file path or URL of the RDF graph")
	private File path;
	
	@Option(names={"-process"}, paramLabel="CAPAPATH", description="file path or URL of the capability RDF graph")
	private File capaPath;
	
	@Option(names={"-save"})
	private boolean save = false;
	
	@Option(names={"-mach", "--machine"}, paramLabel="Machine", description="a particular machine represented by an individual of type machine or its sub-type")
	private String machine="";
	
	private org.apache.jena.rdf.model.Resource machineResource = null;
	
	@Option(names={"-tool"}, paramLabel="Tool", description="a particular tool represented by an individual of type Tool or its sub-type")
	private String tool="";
	
	private org.apache.jena.rdf.model.Resource toolResource = null;
	
	@Option(names={"-func", "--function"}, paramLabel="FunctionURI", description="function individual URI")
	private String[] function;	
	
	@Option(names={"-new", "--create"}, description="Add/modify capability for a process. the capability is added to new function if '-func' is provided")
	private boolean create = false;
	
	public Resource() {
		// TODO Auto-generated constructor stub
	}

	Model m = ModelFactory.createDefaultModel();
	static Resource resource;
	
	public static void main(String[] args) {
		resource = new Resource();
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
			CommandLine.run(resource, args);
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
				   .map(pa->ModelFactory.createDefaultModel().read(pa))
				   .set(m1->m.add(m1))
				   .onFailure(e->System.err.println(e.getMessage()))
				   .onSuccess(m->System.out.printf("RDF is read from %s\n",path.getAbsolutePath()));				
			}
			path=null;
		}
		if(machine!=null){
			if(create){
				RDFNode machineType = ResourceFactory.createResource(machine);
				String machineInstanceURI = IMPM.resource_IMPM + machineType.asNode().getLocalName() + "_I" + IMPM.newHash(4);
				machineResource = ResourceFactory.createResource(machineInstanceURI);
				m.add(machineResource, ResourceFactory.createProperty(IMPM.rdf+"type"), machineType);
				System.out.println("New machine instance " + machineInstanceURI + " of type " + machineType.asNode().getLocalName());
				machine = null;
			}
			else{
				machineResource = ResourceFactory.createResource(machine);
			}
		}
		if(tool!=null){
			if(create){
				RDFNode toolType = ResourceFactory.createResource(tool);
				String toolInstanceURI = IMPM.resource_IMPM + toolType.asNode().getLocalName() + "_I" + IMPM.newHash(4);
				toolResource = ResourceFactory.createResource(toolInstanceURI);
				m.add(toolResource, ResourceFactory.createProperty(IMPM.rdf+"type"), toolType);
				System.out.println("New tool instance " + toolInstanceURI + " of type " + toolType.asNode().getLocalName());
				tool = null;
			}
			else{
				toolResource = ResourceFactory.createResource(tool);
			}
		}
		if(function!=null){
			for(String func:function){
				m.add(machineResource, ResourceFactory.createProperty(IMPM.cco+"has_function"), func);
				m.add(toolResource, ResourceFactory.createProperty(IMPM.cco+"has_function"), func);
				System.out.println(machineResource.getLocalName() + ", " + toolResource.getLocalName() + " has function " + func);
			}
			function = null;
		}
	}

}
