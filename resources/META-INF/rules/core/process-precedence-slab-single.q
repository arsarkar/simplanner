PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root rdf:type pp:RootProcess.
	?fm1	rdf:type	 capa:FaceMilling.
	?root 	pp:hasSucceedingProcess ?fm1.
}
WHERE{
	?ro2	rdf:type	 capa:SurfaceRoughing.
	?fm1	rdf:type	 capa:FaceMilling.
	?fm1 	cco:realizes ?ro2.
}