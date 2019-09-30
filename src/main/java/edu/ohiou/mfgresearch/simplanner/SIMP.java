package edu.ohiou.mfgresearch.simplanner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ohiou.mfgresearch.lambda.Uni;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

public class SIMP{

	public SIMP() {
	}

	@Option(names={"-v", "--verbose"}, description="write nodes with complete URI")
	private boolean verbose = false;
	
	@Option(names = "--help", usageHelp = true, description = "display this help")
	boolean help;
	
	private static Part part = null;
	private static Process process = null;
	private static Plan plan = null;
	
	public static Part getPart(){
		if(part==null) part = new Part();
		return part;
	}
	
	public static Process getProcess(){
		if(process==null) process = new Process();
		return process;
	}
	
	public static Plan getPlan(){
		if(plan==null) plan = new Plan();
		return plan;
	}
	
	public static void main(String[] args) {
		SIMP simp = new SIMP();	

		Scanner scanner = new Scanner(System.in);
		boolean cont=true; 
		while(cont){
			Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(scanner.nextLine());
			List<String> options = new LinkedList<>();
			while (m.find()){
				options.add(m.group(1).replaceAll("\"", ""));
			}
			m.reset();			
			args = options.toArray(new String[options.size()]);
			
			switch (args[0].trim().toLowerCase()) {
				case "part":
					CommandLine.run(getPart(), Arrays.copyOfRange(args, 1, args.length));
					break;
				case "process":
					CommandLine.run(getProcess(), Arrays.copyOfRange(args, 1, args.length));
					break;	
				case "plan":
					CommandLine.run(getPlan(), Arrays.copyOfRange(args, 1, args.length));
					break;	
				case "e":
					cont = false;
					scanner.close();
					break;
				case "exit":	
					cont = false;
					scanner.close();
					break;					
				default:
					System.out.println("command is not valid!");
					CommandLine.usage(getPart(), System.out);
					CommandLine.usage(getProcess(), System.out);
					System.out.println("-e or --exit to quit");
					break;
			}
			
			
			options.clear();
		}
	}
}
