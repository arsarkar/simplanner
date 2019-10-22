PREFIX  rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  plan: <http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

SELECT ?p1 ?fName1 ?p2 ?fName2
WHERE{
?pCurrent	^plan:precedes*	?p1.
?p1	cco:has_output / cco:designated_by  / cco:inheres_in / cco:has_text_value  ?fName1.
OPTIONAL{
?p1	^plan:precedes	?p2.
?p2	cco:has_output / cco:designated_by  / cco:inheres_in / cco:has_text_value  ?fName2.
}
}