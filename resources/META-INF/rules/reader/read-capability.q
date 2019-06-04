PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  cap: <http://www.ohio.edu/ontologies/manufacturing-capability#>

SELECT ?Capability 
WHERE{
?p	cco:realizes ?func.
?capa	rdf:type ?Capability;
			cap:demarcates	?func.	
FILTER (?Capability NOT IN (owl:NamedIndividual))
}