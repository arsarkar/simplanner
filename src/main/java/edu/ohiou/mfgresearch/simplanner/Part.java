package edu.ohiou.mfgresearch.simplanner;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;

import edu.ohiou.mfgresearch.io.FunQL;
import edu.ohiou.mfgresearch.lambda.Uni;
import edu.ohiou.mfgresearch.reader.PropertyReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name="part", description="reads the specifications from a given part design", helpCommand = true)
class Part implements Runnable {	
	
	public Part() {
//		System.out.println("in Part constructor!");
	}

	PropertyReader prop = PropertyReader.getProperty();
	Model m = ModelFactory.createDefaultModel();
	
	private static Part part=null;
	
	@Option(names={"-v", "--verbose"}, description="write nodes with complete URI")
	private boolean verbose = false;
	
	@Option(names={"-g", "--graph"}, paramLabel="PATH", description="file path or URL of the RDF graph")
	private File path;	
	
	@Option(names={"-file"}, paramLabel="FILE", description="file path or URL of the specification XML")
	private File file;	
	
	@Option(names={"-f", "--feature"}, paramLabel="FEATURE", description="feature name or * for all features")
	private String featureName="";
	
	@Option(names={"-d", "--dimension"}, paramLabel="DIMENSTION", description="dimension type or * for all dimensions")
	private String dimType="";
	
	@Option(names={"-t", "--tolerance"}, paramLabel="TOLERANCE", description="tolerance type or * for all tolerances")
	private String tolType="";
	
	@Option(names={"-u", "--unit"}, paramLabel="Unit", description="set the unit, default unit is set to millimeter.")
	private String unit = "mm";
	
	@Option(names={"-prec"}, paramLabel="Precedence", description="file containing precedence.")
	private File precFile;		
	
	public static void main(String[] args) {
		part = new Part();
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
			CommandLine.run(part, args);
		}
	}

	@Override
	public void run() {

		if(path!=null){
			if(file!=null){
				PartSpecificationGraph spec = new PartSpecificationGraph("", file.getPath(), IMPM.getUnit(unit));
				if(precFile!=null) spec.addPrecedence(precFile);
				String label = spec.loadPart(path.getPath());
				System.out.println("Part specification " + label + " is loaded from "+ file.getAbsolutePath());
			}
			m.read(path.getPath());
			System.out.printf("RDF is read from %s\n",path.getAbsolutePath());
			//if precfile is given load precedence to the existing model
			if(precFile!=null){
				PartSpecificationGraph spec = new PartSpecificationGraph();
				spec.loadPrecedence(m, precFile, path.getPath());
			}
			path=null;
			
		}
		if(!m.isEmpty()){
			if(featureName.length()>0){
				readFeatures(featureName);
				featureName="";
				dimType="";
				tolType="";
			}
		}
		else{
			System.out.println("Model is empty!");
		}		
	}

	private void readFeatures(String name){
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-feature-by-name.q"))
		   .select(q->!name.equals("*"), q->q.getPlan(0).addVarBinding("FeatureName", ResourceFactory.createPlainLiteral(name)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet()); 
			   else System.out.println("No feature is found for feature name " + name);
			   tab.rows().forEachRemaining(r->{
				   String fName = r.get(Var.alloc("FeatureName")).getLiteralValue().toString();
				   if(dimType.length()>0) readDimension(fName, dimType);
				   if(tolType.length()>0) readTolerance(fName, tolType);
			   });
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;		
	}
	
	private void readDimension(String name, String type){
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-feature-dimension.q"))
		   .select(q->!name.equals("*"), q->q.getPlan(0).addVarBinding("FeatureName", ResourceFactory.createPlainLiteral(name)))
		   .select(q->!type.equals("*"), q->q.getPlan(0).addVarBinding("DimensionType", ResourceFactory.createPlainLiteral(type)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet());
			   else System.out.println("No dimension is found for feature name " + name + " or with dimension type "+ type);
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;	
	}
	
	private void readTolerance(String name, String type){
		Uni.of(FunQL::new)
		   .set(q->q.addTBox(prop.getIRIPath(IMPM.design)))
		   .set(q->q.addABox(m))
		   .set(q->q.addPlan("resources/META-INF/rules/reader/read-feature-tolerance.q"))
		   .select(q->!name.equals("*"), q->q.getPlan(0).addVarBinding("FeatureName", ResourceFactory.createPlainLiteral(name)))
		   .select(q->!type.equals("*"), q->q.getPlan(0).addVarBinding("ToleranceType", ResourceFactory.createPlainLiteral(type)))
		   .set(q->q.setSelectPostProcess(tab->{
			   if(!tab.isEmpty()) ResultSetFormatter.out(System.out, tab.toResultSet());
			   else System.out.println("No tolerance is found for feature name " + name + " or with tolerance type "+ type);
			   return tab;
		   }))
		   .set(q->q.execute())
		   .onFailure(e->e.printStackTrace(System.out))
		   ;	
	}

	public Model getModel() {
		return m;
	}
}
