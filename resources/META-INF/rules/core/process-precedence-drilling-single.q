PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	pp:hasOptionallySucceedingProcess rdf:type owl:ObjectProperty.
	cco:realizes rdf:type owl:ObjectProperty.
	?root 	rdf:type pp:RootProcess.
		
	?sp rdf:type capa:SpotDrilling.
	?sp 	cco:realizes	?spfunc.
	?spfunc	rdf:type		?spfType.
	
	?td rdf:type capa:TwistDrilling.
	?td 	cco:realizes	?tdfunc.
	?tdfunc	rdf:type		?tdfType.
		
	?root pp:hasSucceedingProcess ?sp.
	?sp pp:hasOptionallySucceedingProcess ?td. 
}
WHERE{
	?sp rdf:type capa:SpotDrilling.
	?sp 	cco:realizes	?spfunc.
	?spfunc	rdf:type		?spfType.

	?td 	rdf:type 		capa:TwistDrilling.
	?td 	cco:realizes	?tdfunc.
	?tdfunc	rdf:type		?tdfType.
	
	FILTER (?spfType NOT IN (owl:NamedIndividual))
	FILTER (?tdfType NOT IN (owl:NamedIndividual))
}