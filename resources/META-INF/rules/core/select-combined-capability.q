PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>
PREFIX  cap: <http://www.ohio.edu/ontologies/manufacturing-capability#>

SELECT ?capa ?capaType ?refType	?max ?min
WHERE{
?capa	rdf:type ?capaType;
			cco:references	?ref.
?ref		rdf:type	?refType.		
	
?capa	cco:is_measured_by	?maxICE.
?maxICE	cco:inheres_in			?maxIBE;
			cco:is_measured_by	?maxOrd;
			rdf:type	cco:MeasurementInformationContentEntity.
?maxOrd	rdf:type	cco:MaximumOrdinalMeasurementInformation.
?maxIBE	rdf:type				cco:InformationBearingEntity;
			cco:has_decimal_value	?max.
	
?capa	cco:is_measured_by	?minICE.
?minICE	cco:inheres_in			?minIBE;
			cco:is_measured_by	?minOrd;
			rdf:type	cco:MeasurementInformationContentEntity.
?minOrd	rdf:type	cco:MinimumOrdinalMeasurementInformation.
?minIBE	rdf:type				cco:InformationBearingEntity;
			cco:has_decimal_value	?min.	 
}