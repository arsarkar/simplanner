PREFIX  this: <edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph>
PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

CONSTRUCT 
  { 
    ?i3 rdf:type design:MeasurementBearingEntity .
    ?i3 cco:has_text_value ?dm .
    ?d cco:inheres_in ?i3 .
  }
WHERE
  { ?f   rdf:type              design:FeatureSpecification .
    ?i1  rdf:type              design:LabelBearingEntity .
    ?d   rdf:type              design:QualitySpecification .
    ?fd  rdf:type              design:FeatureQualityMap .
    ?i2  rdf:type              design:TypeBearingEntity .
    ?f   cco:inheres_in        ?i1 .
    ?i1  cco:has_text_value    ?fn .
    ?i2  cco:has_URI_value     ?dt .
    ?d   cco:inheres_in        ?i2 .
    ?fd  design:describes_map_with  ?f ;
         design:describes_map_with  ?d
  }
FUNCTION{
	?dm <- this:getDimensionMeasure(?fn,?dt)
}