PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root 	rdf:type pp:RootProcess.
	
	?pr1	rdf:type	 capa:PocketRoughing.
	?em1	rdf:type	 capa:EndMilling.
	?em1	cco:realizes ?pr1.
	
	?root	pp:hasSucceedingProcess ?em1.
}
WHERE{
	?pr1	rdf:type	 capa:PocketRoughing.
	?em1	rdf:type	 capa:EndMilling.
	?em1	cco:realizes ?pr1.
}