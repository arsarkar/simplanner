PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

SELECT ?f1 ?ft ?d
WHERE{
 ?f1	  rdf:type			?ft.
 ?d1	  cco:inheres_in	?f1;
		  cco:concretizes	?d.
  
 FILTER ( ?ft	NOT IN (design:FormFeature))
}