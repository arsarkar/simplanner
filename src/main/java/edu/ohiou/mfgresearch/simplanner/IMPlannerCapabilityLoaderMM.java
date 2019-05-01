package edu.ohiou.mfgresearch.simplanner;

import edu.ohiou.mfgresearch.reader.PropertyReader;

public class IMPlannerCapabilityLoaderMM {
	
	static ProcessCapabilityGraph graph;
	static String newKBPath;

	public IMPlannerCapabilityLoaderMM(String kbPath) {
		
	}

	public static void main(String[] args) {
		PropertyReader prop = new PropertyReader();
		graph = new ProcessCapabilityGraph(prop.getProperty("CAPABILITY_ABOX"));
//		newKBPath = args[0];
		//create hole starting function and capability
		
		try {
			//Hole starting function
			//spot drilling profile
			String holeStarting1 = graph.postFunction("HoleStarting", "http://www.ohio.edu/ontologies/capability-implanner#spotdrilling0101");
			graph.postCapability("function="+holeStarting1, "capability=TruePosition", "min=0.000154", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=ToolDiameter", "min=1.5875", "max=50.8", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=PositiveTolerance", "min=((0.3527 * (sqrt ?arg1)) + 0.762)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=NegativeTolerance", "min=(0.3527 * (sqrt ?arg1) )", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=Straightness", "min=((0.127 * (pow (?arg1 / ?arg2), 3)) + 0.508)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=Roundness", "min=0.16", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=SurfaceFinish", "min=2.0", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=Parallelism", "min=(((pow (?arg1 / ?arg2), 3) * 0.254) + 0.762)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
//			graph.postCapability("function="+holeStarting1, "capability=DepthLimit", "min=0", "max=(12 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//center drilling profile
			String holeStarting2 = graph.postFunction("HoleStarting", "http://www.ohio.edu/ontologies/capability-implanner#centerdrilling0101");
			
			//hole making function
			//twist drilling profile
			String holeMaking1 = graph.postFunction("HoleMaking", "http://www.ohio.edu/ontologies/capability-implanner#twistdrilling0101");
			graph.postCapability("function="+holeMaking1, "capability=TruePosition", "min=0.2032", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeMaking1, "capability=ToolDiameter", "min=1.5875", "max=50.8", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeMaking1, "capability=PositiveTolerance", "min=(+ (* 0.03527 (sqrt ?arg1) ) 0.0762)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeMaking1, "capability=NegativeTolerance", "min=(* 0.03527  (sqrt ?arg1 ) )", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeMaking1, "capability=Straightness", "min=(+ (* 0.127 (** (/ ?arg1 ?arg2) 3)) 0.0508)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeMaking1, "capability=Roundness", "min=0.1016", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeMaking1, "capability=SurfaceFinish", "min=2.5", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeMaking1, "capability=Parallelism", "min=(+ (* (** (/ ?arg1 ?arg2) 3) 0.0254) 0.0762)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeMaking1, "capability=DepthLimit", "min=0", "max=(* 12 ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//End drilling profile
			String holeMaking2 = graph.postFunction("HoleMaking", "http://www.ohio.edu/ontologies/capability-implanner#enddrilling0101");
			graph.postCapability("function="+holeMaking2, "capability=TruePosition", "min=0.2032", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeMaking2, "capability=ToolDiameter", "min=3.157", "max=25.4", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeMaking2, "capability=PositiveTolerance", "min=0.0254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeMaking2, "capability=NegativeTolerance", "min=0.0254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeMaking2, "capability=Straightness", "min=0.0635", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeMaking2, "capability=Roundness", "min=0.1016", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeMaking2, "capability=SurfaceFinish", "min=1.575", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeMaking2, "capability=Parallelism", "min=0.0889", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeMaking2, "capability=DepthLimit", "min=0", "max=(2 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Spade drilling profile
			String holeMaking3 = graph.postFunction("HoleMaking", "http://www.ohio.edu/ontologies/capability-implanner#spadedrilling0101");
			graph.postCapability("function="+holeMaking3, "capability=TruePosition", "min=0.2032", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeMaking3, "capability=ToolDiameter", "min=19.05", "max=101.6", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeMaking3, "capability=PositiveTolerance", "min=((0.025199 * (sqrt ?arg1)) + 0.0762)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeMaking3, "capability=NegativeTolerance", "min=((0.020159 * (sqrt ?arg1)) + 0.0635)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeMaking3, "capability=Straightness", "min=((0.00762 * (pow (?arg1 / ?arg2), 3)) + 0.0508)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeMaking3, "capability=Roundness", "min=0.1016", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeMaking3, "capability=SurfaceFinish", "min=2.5", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeMaking3, "capability=Parallelism", "min=((0.1524 * (pow (?arg1 / ?arg2), 3)) + 0.0762)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeMaking3, "capability=DepthLimit", "min=0", "max=(4 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Gun drilling profile
			String holeMaking4 = graph.postFunction("HoleMaking", "http://www.ohio.edu/ontologies/capability-implanner#gundrilling0101");
			graph.postCapability("function="+holeMaking4, "capability=TruePosition", "min=0.0508", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeMaking4, "capability=ToolDiameter", "min=1.905", "max=50.8", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeMaking4, "capability=PositiveTolerance", "min=0.07366", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeMaking4, "capability=NegativeTolerance", "min=0.06096", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeMaking4, "capability=Straightness", "min=((0.00762 * (pow (?arg1 / ?arg2), 3)) + 0.0254)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeMaking4, "capability=Roundness", "min=0.0508", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeMaking4, "capability=SurfaceFinish", "min=0.75", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeMaking4, "capability=Parallelism", "min=((0.0254 * (pow (?arg1 / ?arg2), 3)) + 0.0762)", "max=99999999999.99", "arg1=http://www.ohio.edu/ontologies/design#DepthSpecification", "arg2=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeMaking4, "capability=DepthLimit", "min=0", "max=(40 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//hole improving function
			//boring profile
			String holeImproving1 = graph.postFunction("HoleImproving", "http://www.ohio.edu/ontologies/capability-implanner#boring0101");
			graph.postCapability("function="+holeImproving1, "capability=TruePosition", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeImproving1, "capability=ToolDiameter", "min=9.525", "max=254", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeImproving1, "capability=PositiveTolerance", "min=0.00762", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeImproving1, "capability=NegativeTolerance", "min=0.00762", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeImproving1, "capability=Straightness", "min=0.0127", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeImproving1, "capability=Roundness", "min=0.0127", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeImproving1, "capability=SurfaceFinish", "min=0.2", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeImproving1, "capability=Parallelism", "min=0.0254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeImproving1, "capability=DepthLimit", "min=0", "max=(9 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Precision Boring profile
			String holeImproving2 = graph.postFunction("HoleImproving", "http://www.ohio.edu/ontologies/capability-implanner#precboring0101");
			graph.postCapability("function="+holeImproving2, "capability=TruePosition", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeImproving2, "capability=ToolDiameter", "min=9.525", "max=254", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeImproving2, "capability=PositiveTolerance", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeImproving2, "capability=NegativeTolerance", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeImproving2, "capability=Straightness", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeImproving2, "capability=Roundness", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeImproving2, "capability=SurfaceFinish", "min=0.2", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeImproving2, "capability=Parallelism", "min=0.0889", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeImproving2, "capability=DepthLimit", "min=0", "max=(9 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Reaming profile
			String holeImproving3 = graph.postFunction("HoleImproving", "http://www.ohio.edu/ontologies/capability-implanner#reaming0101");
			graph.postCapability("function="+holeImproving3, "capability=TruePosition", "min=0.4", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#TruePositionSpecification");
			graph.postCapability("function="+holeImproving3, "capability=ToolDiameter", "min=1.5875", "max=101.6", "reference=http://www.ohio.edu/ontologies/design#DiameterSpecification");
			graph.postCapability("function="+holeImproving3, "capability=PositiveTolerance", "min=0.01016", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeImproving3, "capability=NegativeTolerance", "min=0.01016", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeImproving3, "capability=Straightness", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#StraightnessSpecification");
			graph.postCapability("function="+holeImproving3, "capability=Roundness", "min=0.0127", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#RoundnessSpecification");
			graph.postCapability("function="+holeImproving3, "capability=SurfaceFinish", "min=0.4", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			graph.postCapability("function="+holeImproving3, "capability=Parallelism", "min=0.254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#ParallelismSpecification");
			graph.postCapability("function="+holeImproving3, "capability=DepthLimit", "min=0", "max=(16 * ?arg1)", "arg1=http://www.ohio.edu/ontologies/design#DiameterSpecification", "reference=http://www.ohio.edu/ontologies/design#DepthSpecification");
			
			//Hole Finishing
			//Honing profile
			String holeFinishing1 = graph.postFunction("HoleFinishing", "http://www.ohio.edu/ontologies/capability-implanner#honing0101");
			graph.postCapability("function="+holeFinishing1, "capability=PositiveTolerance", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeFinishing1, "capability=NegativeTolerance", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeFinishing1, "capability=SurfaceFinish", "min=0.05", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			
			//Hole grinding profile
			String holeFinishing2 = graph.postFunction("HoleFinishing", "http://www.ohio.edu/ontologies/capability-implanner#holegrinding0101");
			graph.postCapability("function="+holeFinishing2, "capability=PositiveTolerance", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#PositiveToleranceSpecification");
			graph.postCapability("function="+holeFinishing2, "capability=NegativeTolerance", "min=0.00254", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#NegativeToleranceSpecification");
			graph.postCapability("function="+holeFinishing2, "capability=SurfaceFinish", "min=0.1", "max=99999999999.99", "reference=http://www.ohio.edu/ontologies/design#SurfaceFinishSpecification");
			
			graph.writePartGraph(prop.getProperty("CAPABILITY_ABOX_MM"), "RDF/XML");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
