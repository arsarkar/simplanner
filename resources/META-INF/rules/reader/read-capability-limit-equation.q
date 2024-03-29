PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  cap: <http://www.ohio.edu/ontologies/manufacturing-capability#>

SELECT ?capa ?MaxLimit ?MaxEquation ?MinLimit ?MinEquation ?arg
WHERE{
?p	cco:realizes ?func.
?capa	rdf:type ?Capability;
			cap:demarcates	?func;
			cco:references	?ref.
?ref		rdf:type	?Reference.			
?capa	cco:is_measured_by	?maxICE;
			cco:is_measured_by	?minICE.

?maxICE	cco:inheres_in			?maxIBE;
			cco:is_measured_by	?maxOrd;
			rdf:type	cco:MeasurementInformationContentEntity.
?maxOrd	rdf:type	cco:MaximumOrdinalMeasurementInformation.
?maxIBE	rdf:type	cco:InformationBearingEntity;
			cco:uses_measurement_unit ?maxUnit;
			cco:has_decimal_value	?MaxLimit.	

?minICE	cco:inheres_in			?minIBE;
			cco:is_measured_by	?minOrd;
			rdf:type	cco:Equation;
			cco:expects	?arg.
?minOrd	rdf:type	cco:MinimumOrdinalMeasurementInformation.
?minIBE	rdf:type	cco:InformationBearingEntity;
			cco:has_string_value ?MinEquation;
			cco:uses_measurement_unit ?minUnit;
			cco:uses_equation_type	?eqType.
?arg		rdf:type	?Argument;
			cco:is_tokenized_by	?token.	
FILTER (?Capability NOT IN (owl:NamedIndividual))
FILTER (?Reference NOT IN (owl:NamedIndividual))
FILTER (?Argument NOT IN (owl:NamedIndividual))
}
