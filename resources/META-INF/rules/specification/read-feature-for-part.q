PREFIX  this: <edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph>
PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

CONSTRUCT 
  { 
    ?f rdf:type design:FeatureSpecification .
    ?r rdf:type design:FeatureRepresentation. 
    ?pf rdf:type design:PartFeatureMap .
    ?i2 rdf:type design:LabelBearingEntity .
    design:describes_map_with rdf:type owl:ObjectProperty .
    cco:represents rdf:type owl:ObjectProperty .
    ?f cco:represents ?r. 
    ?i2 cco:has_text_value ?fn .
    ?f cco:inheres_in ?i2 .
    ?pf design:describes_map_with ?f .
    ?pf design:describes_map_with ?p .
  }
WHERE
  { ?p   rdf:type            design:PartSpecification .
    ?i1  rdf:type            design:LabelBearingEntity .
    ?p   cco:inheres_in      ?i1 .
    ?i1  cco:has_text_value  ?pn
  }
FUNCTION{
	?fn <- this:getFeatures()
}
