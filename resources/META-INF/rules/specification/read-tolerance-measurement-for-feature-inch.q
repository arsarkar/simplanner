PREFIX  this: <edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph>
PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

CONSTRUCT 
  { 
    ?i3 rdf:type design:MeasurementBearingEntity .
    cco:has_decimal_value rdf:type owl:DatatypeProperty .
    ?i3 cco:has_decimal_value ?tm .
    cco:uses_measurement_unit rdf:type owl:ObjectProperty .
    cco:InchMeasurementUnit rdf:type cco:MeasurementUnitOfLength.
    ?i3 cco:uses_measurement_unit cco:InchMeasurementUnit.
    ?t cco:inheres_in ?i3 .
  }
WHERE
  { ?f   rdf:type              design:FeatureSpecification .
    ?i1  rdf:type              design:LabelBearingEntity .
    ?t   rdf:type              design:ToleranceSpecification .
    ?ft  rdf:type              design:FeatureQualityMap .
    ?i2  rdf:type              design:TypeBearingEntity .
    ?f   cco:inheres_in        ?i1 .
    ?i1  cco:has_text_value    ?fn .
    ?i2  cco:has_URI_value     ?tt .
    ?t   cco:inheres_in        ?i2 .
    ?ft  design:describes_map_with  ?f ;
         design:describes_map_with  ?t}
FUNCTION{
	?tm <- this:getToleranceMeasure(?fn,?tt)
}
