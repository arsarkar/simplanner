PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

CONSTRUCT 
  {   	
    ?d rdf:type design:ParallelismSpecification.
  }
WHERE
  { 
	 ?f   rdf:type              design:FeatureSpecification.
	 ?fq  rdf:type              design:FeatureQualityMap .
	 ?fq  design:describes_map_with ?f;
		  design:describes_map_with ?d.
	 ?d	  rdf:type 				design:ToleranceSpecification;		  
	 	  cco:inheres_in		?dt.
	 ?dt  rdf:type				design:TypeBearingEntity;
	 	  cco:has_URI_value 	"parallelism".
  } 