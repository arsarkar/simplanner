package edu.ohiou.mfgresearch.simplanner;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.ohiou.mfgresearch.reader.ProcessCapabilityLoader;

public class ProcessCapabilityLoaderTest {

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
}
