PREFIX  rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  plan: <http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

SELECT DISTINCT ?p1 ?fName ?pBefore
WHERE{
?p0	rdf:type 		plan:RootProcess.
?p0	rdf:type 		plan:PlannedProcess.
?p0	plan:precedes* 	?pCurrent.
?pCurrent	cco:has_output	?f1.
?f1	rdf:type		?fType.

?pCurrent	^plan:precedes*	?p1.
?p1	cco:has_output	?f2.
?f2	cco:designated_by	?fi.
?fi	cco:inheres_in	?ibe.
?ibe	cco:has_text_value	?fName.

OPTIONAL{?p1	^plan:precedes	?pBefore}

FILTER NOT EXISTS {?pCurrent plan:precedes ?p2}.
FILTER (?fType NOT IN (owl:NamedIndividual))
FILTER NOT EXISTS  {?f1	rdf:type design:UnsatisfiedFeature.
	                 ?f1	rdf:type design:FormFeature}
}
ORDER BY ?pCurrent ?fName