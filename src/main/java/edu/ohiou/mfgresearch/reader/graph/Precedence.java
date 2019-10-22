package edu.ohiou.mfgresearch.reader.graph;

public class Precedence {

	Occurrence pBefore = null;
	Occurrence pNext = null;
	
	public Precedence(Occurrence pBefore, Occurrence pNext) {
		super();
		this.pBefore = pBefore;
		this.pNext = pNext;
	}

	public Occurrence getpBefore() {
		return pBefore;
	}

	public Occurrence getpNext() {
		return pNext;
	}

	@Override
	public boolean equals(Object obj) {
		Precedence prec = (Precedence) obj;
		return getpBefore().equals(prec.getpBefore()) && getpNext().equals(prec.getpNext());
	}

	@Override
	public String toString() {
		return "<" + pBefore + "->" + pNext + ">";
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	
}
