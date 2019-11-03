PREFIX  rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  plan: <http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>

SELECT ?pCurrent ?f ?q ?s ?st
WHERE{
?pCurrent cco:has_output ?f.
?q	cco:inheres_in ?f.
?q  cco:concretizes ?s.	
?s  rdf:type	?st.
FILTER (?st NOT IN (owl:NamedIndividual))
}