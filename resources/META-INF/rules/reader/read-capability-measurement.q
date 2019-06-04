PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  cap: <http://www.ohio.edu/ontologies/manufacturing-capability#>

SELECT ?Capability ?Reference ?MaxLimit ?MaxEquation ?MinLimit ?MinEquation ?Argument
WHERE{
?p	cco:realizes ?func.
?capa	rdf:type ?Capability;
			cap:demarcates	?func;
			cco:references	?ref.
?ref		rdf:type	?Reference.			
?capa	cco:is_measured_by	?maxICE.
?maxICE	cco:inheres_in			?maxIBE;
			cco:is_measured_by	?maxOrd;
			rdf:type	cco:MeasurementInformationContentEntity.
?maxOrd	rdf:type	cco:MaximumOrdinalMeasurementInformation.
?maxIBE	rdf:type	cco:InformationBearingEntity;
			cco:has_decimal_value	?MaxLimit.	
?capa	cco:is_measured_by	?minICE.
?minICE	cco:inheres_in			?minIBE;
			cco:is_measured_by	?minOrd;
			rdf:type	cco:MeasurementInformationContentEntity.
?minOrd	rdf:type	cco:MinimumOrdinalMeasurementInformation.
?minIBE	rdf:type	cco:InformationBearingEntity;
			cco:has_decimal_value	?MinLimit.	
FILTER (?Capability NOT IN (owl:NamedIndividual))
FILTER (?Reference NOT IN (owl:NamedIndividual))
}