package edu.ohiou.mfgresearch.simplanner;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.lambda.Omni;
import edu.ohiou.mfgresearch.reader.IMPlanXMLLoader;
import edu.ohiou.mfgresearch.reader.PartFeatureLoader;
import edu.ohiou.mfgresearch.reader.ProcessCapabilityLoader;
import edu.ohiou.mfgresearch.reader.PropertyReader;

public class LoaderTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void test1() {
		ProcessCapabilityLoader loader = new ProcessCapabilityLoader("C:/Users/sarkara1/git/simplanner/resources/META-INF/implan/ProcessCapability-mm.xml");
		System.out.println("Processes -->");
		loader.readProcesses()
			  .forEach(p->p.set(System.out::println));
		
	}
	
	@Test
	public void testProperty(){
		PropertyReader prop = new PropertyReader();
		System.out.println(prop.getIRIPath("http://www.ohio.edu/ontologies/capability-implanner#"));
		System.out.println(prop.getProperty("DESIGN_XML"));
	}
	
	@Test
	public void testStringMatch(){
		String s = "http://www.ohio.edu/ontologies/capability-implanner#";
		String s1 = "http://www.ohio.edu/ontologies/";
		boolean flag = s.contains(s1);
		System.out.println(flag);
	}
	
	@Test
	public void testNextFeature(){
		PartFeatureLoader loader = new IMPlanXMLLoader("C:/Users/sarkara1/git/simplanner/resources/META-INF/implan/slider_with_slabs.xml");
		Omni.of(loader.readFeatures())
			.set(f->{
				System.out.println(f+"--next-->");
				Omni.of(loader.readNextFeature(f))
					.set(f1->System.out.println(f1));
			});
	}
	
	@Test
	public void testPreviousFeature(){
		PartFeatureLoader loader = new IMPlanXMLLoader("C:/Users/sarkara1/git/simplanner/resources/META-INF/implan/slider_with_slabs.xml");
		Omni.of(loader.readFeatures())
			.set(f->{
				System.out.println(f+"--next-->");
				Omni.of(loader.readPreviousFeature(f))
					.set(f1->System.out.println(f1));
			});
	}
}
