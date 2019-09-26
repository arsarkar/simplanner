PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root 	rdf:type pp:RootProcess.
	?td rdf:type capa:TwistDrilling.	
	?root pp:hasSucceedingProcess ?td. 
}
WHERE{
	?td rdf:type capa:TwistDrilling.
}