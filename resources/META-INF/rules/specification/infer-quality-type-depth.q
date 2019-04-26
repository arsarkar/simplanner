PREFIX  this: <edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph>
PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

CONSTRUCT 
  {   	
    ?d1 rdf:type design:DepthSpecification .
    ?fd rdf:type design:FeatureQualityMap .
    ?fd design:describes_map_with ?f;
    	design:describes_map_with ?d1.
    ?i1 rdf:type design:MeasurementBearingEntity .	
    ?d1 cco:inheres_in ?i1;
	 	cco:represents ?r.
    ?i1 cco:has_decimal_value ?depth.
    cco:uses_measurement_unit rdf:type owl:ObjectProperty .
    cco:InchMeasurementUnit rdf:type cco:MeasurementUnitOfLength.
    ?i1 cco:uses_measurement_unit cco:InchMeasurementUnit. 
  }
WHERE
  { 
	 ?f   rdf:type              design:HoleSpecification.
	 ?fq  rdf:type              design:FeatureQualityMap .
	 ?fq  design:describes_map_with ?f;
		  design:describes_map_with ?d.
	 ?d	  rdf:type 				design:QualitySpecification;		  
	 	  cco:inheres_in		?dt;
	 	  cco:inheres_in		?dm;
	 	  cco:represents 		?r.
	 ?dt  rdf:type				design:TypeBearingEntity;
	 	  cco:has_URI_value 	"depth".	   	
	 ?dm  rdf:type				design:MeasurementBearingEntity;
	 	  cco:has_text_value 	?dep.
  }
FUNCTION{
	?depth <- this:calculateDepth(?dep)
}  