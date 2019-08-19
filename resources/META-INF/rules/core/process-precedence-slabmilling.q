PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root rdf:type pp:RootProcess.
	?sm1	rdf:type	capa:SlabMilling.
	?sm2	rdf:type	capa:SlabMilling.
	?sm1 pp:hasSucceedingProcess ?sm2. 
}
WHERE{
	?ro		rdf:type	capa:Roughing.
	?sm1	rdf:type	capa:SlabMilling.
	?sm1 	cco:realizes ?ro.
	?fn		rdf:type	capa:Finishing.
	?sm2	rdf:type	capa:SlabMilling.
	?sm2	cco:realizes ?fn.
}