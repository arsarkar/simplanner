PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

SELECT ?FeatureName ?ToleranceType ?Tolerance
WHERE
  { 
	?f   rdf:type            design:FeatureSpecification .
    ?i1  rdf:type            design:LabelBearingEntity .
    ?f   cco:inheres_in      ?i1.
    ?i1  cco:has_text_value  ?FeatureName.
    
    ?fq  rdf:type              design:FeatureQualityMap .
	?fq  design:describes_map_with ?f;
		 design:describes_map_with ?t.
	?t	 rdf:type 				design:ToleranceSpecification;		  
	 	 cco:inheres_in		?tt;
	 	 cco:inheres_in		?tm.
	?tt rdf:type				design:TypeBearingEntity;
	 	 cco:has_URI_value 	?ToleranceType.	   	
	?tm rdf:type			design:MeasurementBearingEntity;
	 	 cco:has_decimal_value ?Tolerance.     
  }