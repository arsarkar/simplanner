PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

SELECT ?FeatureName ?FeatureType
WHERE
  { 
	?f   rdf:type            design:FeatureSpecification .
    ?i1  rdf:type            design:LabelBearingEntity .
    ?f   cco:inheres_in      ?i1.
    ?i1  cco:has_text_value  ?FeatureName.
    
    ?i2  rdf:type            design:TypeBearingEntity .    
    ?f   cco:inheres_in      ?i2 .
    ?i2  cco:has_URI_value   ?FeatureType.    
  }