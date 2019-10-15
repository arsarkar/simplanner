PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  cap: <http://www.ohio.edu/ontologies/manufacturing-capability#>

SELECT ?capa ?maxICE ?minICE
WHERE{
?p	cco:realizes ?func.
?capa	cap:demarcates	?func.			
?capa	cco:is_measured_by	?maxICE.
?maxICE	cco:is_measured_by	?maxOrd.
?maxOrd	rdf:type	cco:MaximumOrdinalMeasurementInformation.	
?capa	cco:is_measured_by	?minICE.
?minICE	cco:is_measured_by	?minOrd.
?minOrd	rdf:type	cco:MinimumOrdinalMeasurementInformation.
}