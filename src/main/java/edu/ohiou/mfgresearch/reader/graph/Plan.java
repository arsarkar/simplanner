package edu.ohiou.mfgresearch.reader.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine;

public class Plan implements Runnable{

	static Plan plan;
	
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
		
		
	}
}
