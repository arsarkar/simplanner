package edu.ohiou.mfgresearch.simplanner;

import edu.ohiou.mfgresearch.reader.PropertyReader;

public class IMPlannerCapabilityLoaderINCH {
	
	static ProcessCapabilityGraph graph;
	static String newKBPath;

	public IMPlannerCapabilityLoaderINCH(String kbPath) {
		
	}

	public static void main(String[] args) {
		PropertyReader prop = PropertyReader.getProperty();		
		graph = new ProcessCapabilityGraph(prop.getProperty("CAPABILITY_ABOX"), null);
//		newKBPath = args[0];
		//create hole starting function and capability
		
		try {
			
			//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			//Drilling
			//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			//Hole starting function
			//spot drilling profile
			String holeStarting1 = graph.postFunction("HoleStarting", "http://www.ohio.edu/ontologies/capability-implanner#spotdrilling0101");
			graph.postCapability("function="+holeStarting1, "capability=TruePosition", "min=0.000154", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=ToolDiameter", "min=1.5875", "max=50.8", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=PositiveTolerance", "min=((0.3527 * (sqrt ?arg1)) + 0.762)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=NegativeTolerance", "min=(0.3527 * (sqrt ?arg1) )", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=Straightness", "min=((0.127 * (pow (?arg1 / ?arg2), 3)) + 0.508)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=Roundness", "min=0.16", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=SurfaceFinish", "min=2.0", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=Parallelism", "min=(((pow (?arg1 / ?arg2), 3) * 0.254) + 0.762)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=DepthLimit", "min=0", "max=(12 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//center drilling profile
			String holeStarting2 = graph.postFunction("HoleStarting", "http://www.ohio.edu/ontologies/capability-implanner#centerdrilling0101");
			
			//hole making function
			//twist drilling profile (*)
			String holeMaking1 = graph.postFunction("HoleMaking", "http://www.ohio.edu/ontologies/capability-implanner#twistdrilling0101");
			graph.postCapability("function="+holeMaking1, "capability=TruePosition", "min=0.008", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeMaking1, "capability=ToolDiameter", "min=0.0625", "max=2.00", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeMaking1, "capability=PositiveTolerance", "min=(+ (* 0.007(sqrt ?arg1) ) 0.003)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeMaking1, "capability=NegativeTolerance", "min=(* 0.007(sqrt ?arg1 ) )", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeMaking1, "capability=Straightness", "min=(+ (* 0.005(** (/ ?arg1 ?arg2) 3)) 0.002)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeMaking1, "capability=Roundness", "min=0.004", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeMaking1, "capability=SurfaceFinish", "min=100", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeMaking1, "capability=Parallelism", "min=(+ (* (** (/ ?arg1 ?arg2) 3) 0.001) 0.003)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeMaking1, "capability=DepthLimit", "min=0", "max=(* 12 ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//End drilling profile (*)
			String holeMaking2 = graph.postFunction("HoleMaking", "http://www.ohio.edu/ontologies/capability-implanner#enddrilling0101");
			graph.postCapability("function="+holeMaking2, "capability=TruePosition", "min=0.008", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeMaking2, "capability=ToolDiameter", "min=0.125", "max=1.00", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeMaking2, "capability=PositiveTolerance", "min=0.001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeMaking2, "capability=NegativeTolerance", "min=0.001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeMaking2, "capability=Straightness", "min=0.0025", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeMaking2, "capability=Roundness", "min=0.004", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeMaking2, "capability=SurfaceFinish", "min=63", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeMaking2, "capability=Parallelism", "min=0.0035", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeMaking2, "capability=DepthLimit", "min=0", "max=(2 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Spade drilling profile (*)
			String holeMaking3 = graph.postFunction("HoleMaking", "http://www.ohio.edu/ontologies/capability-implanner#spadedrilling0101");
			graph.postCapability("function="+holeMaking3, "capability=TruePosition", "min=0.008", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeMaking3, "capability=ToolDiameter", "min=0.75", "max=4.00", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeMaking3, "capability=PositiveTolerance", "min=(+ (* 0.005 (sqrt ?arg1) ) 0.003)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeMaking3, "capability=NegativeTolerance", "min=(+ (* 0.004 (sqrt ?arg1 ) ) 0.0025)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeMaking3, "capability=Straightness", "min=(+ (* 0.0003 (** (/ ?arg1 ?arg2) 3)) 0.002)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeMaking3, "capability=Roundness", "min=0.004", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeMaking3, "capability=SurfaceFinish", "min=100", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeMaking3, "capability=Parallelism", "min=(+ (* 0.006 (** (/ ?arg1 ?arg2) 3)) 0.003)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeMaking3, "capability=DepthLimit", "min=0", "max=(4 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Gun drilling profile (*)
			String holeMaking4 = graph.postFunction("HoleMaking", "http://www.ohio.edu/ontologies/capability-implanner#gundrilling0101");
			graph.postCapability("function="+holeMaking4, "capability=TruePosition", "min=0.002", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeMaking4, "capability=ToolDiameter", "min=0.075", "max=2.00", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeMaking4, "capability=PositiveTolerance", "min=0.0029", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeMaking4, "capability=NegativeTolerance", "min=0.0024", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeMaking4, "capability=Straightness", "min=(+ (* 0.0003 (** (/ ?arg1 ?arg2) 3)) 0.001)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeMaking4, "capability=Roundness", "min=0.002", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeMaking4, "capability=SurfaceFinish", "min=30", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeMaking4, "capability=Parallelism", "min=(+ (* 0.001 (** (/ ?arg1 ?arg2) 3)) 0.003)", "max=Infinity", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeMaking4, "capability=DepthLimit", "min=0", "max=(40 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//hole improving function
			//boring profile (*)
			String holeImproving1 = graph.postFunction("HoleImproving", "http://www.ohio.edu/ontologies/capability-implanner#boring0101");
			graph.postCapability("function="+holeImproving1, "capability=TruePosition", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeImproving1, "capability=ToolDiameter", "min=0.375", "max=10.0", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeImproving1, "capability=PositiveTolerance", "min=0.0003", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeImproving1, "capability=NegativeTolerance", "min=0.0003", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeImproving1, "capability=Straightness", "min=0.0005", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeImproving1, "capability=Roundness", "min=0.0005", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeImproving1, "capability=SurfaceFinish", "min=8", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeImproving1, "capability=Parallelism", "min=0.001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeImproving1, "capability=DepthLimit", "min=0", "max=(9 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Precision Boring profile (*)
			String holeImproving2 = graph.postFunction("HoleImproving", "http://www.ohio.edu/ontologies/capability-implanner#precboring0101");
			graph.postCapability("function="+holeImproving2, "capability=TruePosition", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeImproving2, "capability=ToolDiameter", "min=0.375", "max=10.00", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeImproving2, "capability=PositiveTolerance", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeImproving2, "capability=NegativeTolerance", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeImproving2, "capability=Straightness", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeImproving2, "capability=Roundness", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeImproving2, "capability=SurfaceFinish", "min=8", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeImproving2, "capability=Parallelism", "min=0.0035", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeImproving2, "capability=DepthLimit", "min=0", "max=(9 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Reaming profile (*)
			String holeImproving3 = graph.postFunction("HoleImproving", "http://www.ohio.edu/ontologies/capability-implanner#reaming0101");
			graph.postCapability("function="+holeImproving3, "capability=TruePosition", "min=0.01", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeImproving3, "capability=ToolDiameter", "min=0.0675", "max=4.00", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeImproving3, "capability=PositiveTolerance", "min=0.0004", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeImproving3, "capability=NegativeTolerance", "min=0.0004", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeImproving3, "capability=Straightness", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeImproving3, "capability=Roundness", "min=0.0005", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeImproving3, "capability=SurfaceFinish", "min=16", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeImproving3, "capability=Parallelism", "min=0.01", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeImproving3, "capability=DepthLimit", "min=0", "max=(16 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Hole Finishing
			//Honing profile (*)
			String holeFinishing1 = graph.postFunction("HoleFinishing", "http://www.ohio.edu/ontologies/capability-implanner#honing0101");
			graph.postCapability("function="+holeFinishing1, "capability=PositiveTolerance", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeFinishing1, "capability=NegativeTolerance", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeFinishing1, "capability=SurfaceFinish", "min=2", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			
			//Hole grinding profile (*)
			String holeFinishing2 = graph.postFunction("HoleFinishing", "http://www.ohio.edu/ontologies/capability-implanner#holegrinding0101");
			graph.postCapability("function="+holeFinishing2, "capability=PositiveTolerance", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeFinishing2, "capability=NegativeTolerance", "min=0.0001", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeFinishing2, "capability=SurfaceFinish", "min=4", "max=Infinity", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			
			//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
			//Milling
			//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
					
			
			graph.writePartGraph("C:/Users/sarkara1/git/SIMPOM/resource/aboxes/process-capability-inch1.owl", "RDF/XML");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
