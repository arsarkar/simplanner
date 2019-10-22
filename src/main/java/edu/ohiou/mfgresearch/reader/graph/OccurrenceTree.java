package edu.ohiou.mfgresearch.reader.graph;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class OccurrenceTree {

	List<Routing> routings = new LinkedList<Routing>();
	Set<Precedence> precedences = new HashSet<Precedence>();
	
	public OccurrenceTree(List<Routing> routs) {
		routings = routs;
		calculate();
	}

	private void calculate() {
		for(Routing r:routings){
			for(Precedence p : r.precedences.values()){
				if(!precedences.contains(p)){
					precedences.add(p);
				}
			}
		}
	}

	public void writeCSV(PrintWriter ps){
		precedences.forEach(prec->{
			ps.write(prec.getpBefore().toString() + "," + prec.getpNext().toString() + "\n");
		});
		ps.flush();
		ps.close();
	}
}
