PREFIX  this: <edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph>
PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

CONSTRUCT 
  { 
    ?t rdf:type design:ToleranceSpecification .
    ?r rdf:type design:ToleranceRepresentation.     
    ?ft rdf:type design:FeatureQualityMap .
    ?i3 rdf:type design:TypeBearingEntity .
    design:describes_map_with rdf:type owl:ObjectProperty .
    cco:represents rdf:type owl:ObjectProperty .
    ?t cco:represents ?r. 
    cco:has_URI_value rdf:type owl:DatatypeProperty .
    ?i3 cco:has_URI_value ?tt .
    ?t cco:inheres_in ?i3 .
    ?ft design:describes_map_with ?f .
    ?ft design:describes_map_with ?t .
  }
WHERE
  { ?f   rdf:type            design:FeatureSpecification .
    ?i2  rdf:type            design:LabelBearingEntity .
    ?f   cco:inheres_in      ?i2 .
    ?i2  cco:has_text_value  ?fn
  }
FUNCTION{
	?tt <- this:getTolerance(?fn)
}
