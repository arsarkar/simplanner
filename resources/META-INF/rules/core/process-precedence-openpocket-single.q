PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root 	rdf:type pp:RootProcess.
	?pr2	rdf:type	 capa:PocketRoughing.
	?sm1	rdf:type	 capa:SideMilling.
	?sm1	cco:realizes ?pr2.
	?root 	pp:hasSucceedingProcess ?sm1.
}
WHERE{
	?pr2	rdf:type	 capa:PocketRoughing.
	?sm1	rdf:type	 capa:SideMilling.
	?sm1	cco:realizes ?pr2.
}