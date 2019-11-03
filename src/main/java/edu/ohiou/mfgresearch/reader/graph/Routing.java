package edu.ohiou.mfgresearch.reader.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Routing {
	
	Map<Occurrence, Precedence> precedences = new HashMap<Occurrence, Precedence>();
//	Queue<Occurrence> rout = new PriorityQueue<Occurrence>();
	List<Occurrence> rout = new LinkedList<Occurrence>();
	Occurrence leaf = null;
	
	public Routing(Occurrence leaf) {
		this.leaf = leaf;
	}

	public Occurrence getLeaf(){
		return leaf;
	}
	
	public void addPrec(Precedence prec){
		//adjust leaf
		if(leaf.getProcess().equals(prec.pNext.getProcess())){
			leaf.feature = prec.pNext.feature;
		}
		precedences.put(prec.pNext, prec);
	}
	
	public void calculate(){
		Occurrence pn = leaf;
		rout.add(leaf);
		while(pn!=null){
			//get the before occurrence
			if(precedences.get(pn)!=null){
				Occurrence pb = precedences.get(pn).pBefore;
				rout.add(pb);
				pn = pb;				
			}
			else{
				pn = null;
			}
		}
		Collections.reverse(rout);
	}

	@Override
	public String toString() {
		return rout.stream().map(o->o.toString()).collect(Collectors.joining(" -> "));
	}
	
}
