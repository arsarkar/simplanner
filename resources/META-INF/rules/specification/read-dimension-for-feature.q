PREFIX  this: <edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph>
PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

CONSTRUCT 
  { 
    ?d rdf:type design:QualitySpecification .
    ?r rdf:type design:QualityRepresentation. 
    ?fd rdf:type design:FeatureQualityMap .
    ?i3 rdf:type design:TypeBearingEntity .
    design:describes_map_with rdf:type owl:ObjectProperty .
    cco:represents rdf:type owl:ObjectProperty .
    ?d cco:represents ?r. 
    cco:has_URI_value rdf:type owl:DatatypeProperty .
    ?i3 cco:has_URI_value ?dt .
    ?d cco:inheres_in ?i3 .
    ?fd design:describes_map_with ?f .
    ?fd design:describes_map_with ?d .
  }
WHERE
  { ?f   rdf:type            design:FeatureSpecification .
    ?i2  rdf:type            design:LabelBearingEntity .
    ?f   cco:inheres_in      ?i2 .
    ?i2  cco:has_text_value  ?fn
  }
FUNCTION{
	?dt <- this:getDimensions(?fn)
}
