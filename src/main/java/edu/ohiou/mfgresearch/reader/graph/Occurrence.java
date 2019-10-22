package edu.ohiou.mfgresearch.reader.graph;

public class Occurrence {

	String process = "";
	String output = "";
	String feature = "";
	
	public Occurrence(String process, String output, String feature) {
		super();
		this.process = process;
		this.output = output;
		this.feature = feature;
	}

	public String getProcess() {
		return process;
	}

	public String getOutput() {
		return output;
	}

	public String getFeature() {
		return feature;
	}
	
	@Override
	public boolean equals(Object obj) {
		Occurrence o = (Occurrence) obj;
		return this.process.equals(o.process);
	}

	@Override
	public String toString() {
		return process+"@"+feature;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
