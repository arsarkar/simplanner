PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

SELECT ?fn ?ft ?dtype ?dimType ?dim 
WHERE
  { ?f   rdf:type            design:FeatureSpecification .
    ?i1  rdf:type            design:LabelBearingEntity .
    ?f   cco:inheres_in      ?i1.
    ?i1  cco:has_text_value  ?fn.
    
    ?i2  rdf:type            design:TypeBearingEntity .    
    ?f   cco:inheres_in      ?i2 .
    ?i2  cco:has_URI_value   ?ft.
    
	?fq  rdf:type              design:FeatureQualityMap .
	?fq  design:describes_map_with ?f;
		 design:describes_map_with ?d.
	?d	 rdf:type 			?dimType;		  
	 	 cco:inheres_in		?dt;
	 	 cco:inheres_in		?dm.
	?dt rdf:type			design:TypeBearingEntity;
	 	 cco:has_URI_value 	?dtype.	   	
	?dm rdf:type			design:MeasurementBearingEntity;
	 	 cco:has_decimal_value ?dim.  
	 	 	   
  }