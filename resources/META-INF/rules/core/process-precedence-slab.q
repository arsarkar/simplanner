PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root rdf:type pp:RootProcess.
	
	?sm1	rdf:type	capa:SlabMilling.
	?ro1	rdf:type	 capa:SurfaceRoughing.
	?sm1 	cco:realizes ?ro1.
	
	?sm2	rdf:type	capa:SlabMilling.
	?fn1	rdf:type	 capa:SurfaceFinishing.
	?sm2	cco:realizes ?fn1.
	
	?fm1	rdf:type	 capa:FaceMilling.
	?ro2	rdf:type	 capa:SurfaceRoughing.
	?fm1 	cco:realizes ?ro2.
	
	?fm2	rdf:type	 capa:FaceMilling.
	?fn2	rdf:type	 capa:SurfaceFinishing.
	?fm2	cco:realizes ?fn2.
	
	?root 	pp:hasSucceedingProcess ?sm1.
	?root 	pp:hasSucceedingProcess ?fm1.
	?sm1 	pp:hasSucceedingProcess ?sm2. 
	?fm1 	pp:hasSucceedingProcess ?fm2. 
}
WHERE{
	?ro1	rdf:type	 capa:SurfaceRoughing.
	?sm1	rdf:type	 capa:SlabMilling.
	?sm1 	cco:realizes ?ro1.
	?fn1	rdf:type	 capa:SurfaceFinishing.
	?sm2	rdf:type	 capa:SlabMilling.
	?sm2	cco:realizes ?fn1.
	
	?ro2	rdf:type	 capa:SurfaceRoughing.
	?fm1	rdf:type	 capa:FaceMilling.
	?fm1 	cco:realizes ?ro2.
	?fn2	rdf:type	 capa:SurfaceFinishing.
	?fm2	rdf:type	 capa:FaceMilling.
	?fm2	cco:realizes ?fn2.
}