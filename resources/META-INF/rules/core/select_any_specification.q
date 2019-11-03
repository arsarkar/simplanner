PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

SELECT	?fs	?d
WHERE{
 ?fs  	rdf:type              				design:FeatureSpecification.
 ?fq  	rdf:type              				design:FeatureQualityMap;
		design:describes_map_with 			?fs;
		design:describes_map_with 	?d.
 ?d		rdf:type 							?dimType.	
 
 FILTER(?dimType IN (design:DiameterSpecification, design:DepthSpecification, design:RoundnessSpecification, design:SurfaceFinishSpecification, design:PositiveToleranceSpecification, design:FlatnessSpecification))	 	
}