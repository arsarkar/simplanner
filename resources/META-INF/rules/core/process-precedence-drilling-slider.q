PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	pp:hasOptionallySucceedingProcess rdf:type owl:ObjectProperty.
	
	?root rdf:type pp:RootProcess.	

	?td rdf:type capa:TwistDrilling.
	?td 	cco:realizes	?tdfunc.
	?tdfunc	rdf:type		?tdfType.
	
	?br rdf:type capa:Boring.
	?br 	cco:realizes	?brfunc.
	?brfunc	rdf:type		?brfType.	
	
	?rm rdf:type capa:Reaming.
	?rm 	cco:realizes	?rmfunc.
	?rmfunc	rdf:type		?rmfType.
	
	?root pp:hasSucceedingProcess ?td.	
	
	?td pp:hasSucceedingProcess ?br.
	?td pp:hasSucceedingProcess ?rm. 
}
WHERE{

	?td rdf:type capa:TwistDrilling.
	?td 	cco:realizes	?tdfunc.
	?tdfunc	rdf:type		?tdfType.
	
	?br rdf:type capa:Boring.
	?br 	cco:realizes	?brfunc.
	?brfunc	rdf:type		?brfType.	
	
	?rm rdf:type capa:Reaming.
	?rm 	cco:realizes	?rmfunc.
	?rmfunc	rdf:type		?rmfType.	
	
	FILTER (?tdfType NOT IN (owl:NamedIndividual))
	FILTER (?brfType NOT IN (owl:NamedIndividual))
	FILTER (?rmfType NOT IN (owl:NamedIndividual))
}