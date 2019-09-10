PREFIX  rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  plan: <http://www.ohio.edu/ontologies/manufacturing-plan#>

SELECT ?p0 ?pNext
WHERE{
?p0	rdf:type 			plan:RootProcess.
?p0	rdf:type 			plan:PlannedProcess.
?p0	plan:precedes 	?pNext.
}