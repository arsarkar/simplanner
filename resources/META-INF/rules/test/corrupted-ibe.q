PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  cap: <http://www.ohio.edu/ontologies/manufacturing-capability#>

SELECT ?p ?func ?capa ?ICE ?IBE ?unit
WHERE{
?p cco:realizes ?func.
?capa	rdf:type ?capaType;
		cap:demarcates	?func.			
?capa	cco:is_measured_by	?ICE.
?ICE	cco:inheres_in			?IBE.
?IBE	cco:uses_measurement_unit ?unit.
{
SELECT (COUNT(?ICE) AS ?count) ?IBE
WHERE{
?ICE	cco:inheres_in			?IBE;
		rdf:type	cco:MeasurementInformationContentEntity.		
}	
GROUP BY ?IBE
}
FILTER(?count >= 2)
}