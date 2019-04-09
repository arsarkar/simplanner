package edu.ohiou.mfgresearch.reader;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.joox.Context;
import org.joox.Filter;
import org.joox.JOOX;
import org.joox.Match;
import org.w3c.dom.Element;

import edu.ohiou.mfgresearch.lambda.Uni;

public class ProcessCapabilityLoader {

	String capabilities[] = {"smallestToolDiameter",
							 "largestToolDiameter",
							 "negativeTolerance",
							 "positiveTolerance",
							 "straightness",
							 "roundness",
							 "parallelism",
						     "depthLimit",
							 "truePosition",
							 "surfaceFinish"};

	Match m;
	
	public ProcessCapabilityLoader(String path){
		load(Uni.of(()->new FileInputStream(path))
				.onFailure(e->e.printStackTrace())
				.get());
	}

	public void load(FileInputStream f) {
		Uni.of(f)
				.map(JOOX::$)
				.onFailure(e -> e.printStackTrace())
				.onSuccess(m -> this.m = m);
	}

	/**
	 * Returns stream of process names (monadic) 
	 * @return
	 */
	public Stream<Uni<String>> readProcesses() {
		return m.first()
				.children()
				.tags()
				.stream()
				.map(p->p.substring(p.lastIndexOf(".") + 1, p.length()))
				.map(p->Uni.of(p));
	}
	
	
	
	public Stream<Uni<String>> readCapabilities(String processName){
		Stream<Element> params =  m.first()
									.child("edu.ohiou.mfgresearch.implanner.processes."+processName)
									.children("Parameter")
									.get()
									.stream();
		
		List<Element> paramList = params.collect(Collectors.toList());
		List<String> allCapas = Arrays.asList(capabilities);
		List<String> capas = new ArrayList<>();
		
		for(Element e:paramList){
			int numAttr = e.getAttributes().getLength();
			for(int i=0;i<numAttr;i++){
				if(allCapas.contains(e.getAttributes().item(i).getLocalName())){
					capas.add(e.getAttributes().item(i).getLocalName());
				}
			}
		}
		
		return capas.stream().map(c->Uni.of(c));
	}
	
	public String getCapabilityType(String processName, String capabilityName){
		
		Stream<Element> params =  m.first()
				.child("edu.ohiou.mfgresearch.implanner.processes."+processName)
				.children("Parameter")
				.get()
				.stream();

		List<Element> paramList = params.collect(Collectors.toList());
		List<String> allCapas = Arrays.asList(capabilities);

		for(Element e:paramList){
			int numAttr = e.getAttributes().getLength();
			for(int i=0;i<numAttr;i++){
				if(e.getAttributes().item(i).getLocalName().equals(capabilityName)){
					return e.getAttribute("type");
				}
			}
		}
		return "";
	}

	public String getCapabilityValue(String processName, String capabilityName){
		
		Stream<Element> params =  m.first()
				.child("edu.ohiou.mfgresearch.implanner.processes."+processName)
				.children("Parameter")
				.get()
				.stream();

		List<Element> paramList = params.collect(Collectors.toList());
		List<String> allCapas = Arrays.asList(capabilities);

		for(Element e:paramList){
			int numAttr = e.getAttributes().getLength();
			for(int i=0;i<numAttr;i++){
				if(e.getAttributes().item(i).getLocalName().equals(capabilityName)){
					return e.getAttribute(capabilityName);
				}
			}
		}
		return "";
	}	
	
	public Stream<Uni<String>> getArguments(String processName, String capabilityName){
		
		Stream<Element> params =  m.first()
				.child("edu.ohiou.mfgresearch.implanner.processes."+processName)
				.children("Parameter")
				.get()
				.stream();

		List<Element> paramList = params.collect(Collectors.toList());
		List<String> allCapas = Arrays.asList(capabilities);
		List<String> args = new ArrayList<String>();
		
		for(Element e:paramList){
			int numAttr = e.getAttributes().getLength();
			for(int i=0;i<numAttr;i++){
				if(e.getAttributes().item(i).getLocalName().equals(capabilityName)){
					if(e.getAttribute("arg1").length()>0){
						args.add(e.getAttribute("arg1"));
					}
					if(e.getAttribute("arg2").length()>0){
						args.add(e.getAttribute("arg2"));
					}
					return args.stream().map(a->Uni.of(a));
				}
			}
		}
		return null;
	}
			 	   
}
