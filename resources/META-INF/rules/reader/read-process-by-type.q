PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  cap: <http://www.ohio.edu/ontologies/manufacturing-capability#>

SELECT
?ProcessType ?Process ?FunctionType ?Function
WHERE{
?Process	rdf:type	?ProcessType.
?Process	cco:realizes ?Function.
?Function	rdf:type	?FunctionType.
FILTER(?ProcessType NOT IN(owl:NamedIndividual) )
FILTER(?FunctionType NOT IN(owl:NamedIndividual) )
}