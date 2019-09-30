PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root 	rdf:type pp:RootProcess.
	
	?em1	rdf:type	capa:EndMilling.
	?pr1	rdf:type	 capa:PocketRoughing.
	?em1	cco:realizes ?pr1.
	
	?em2	rdf:type	capa:EndMilling.
	?pf1	rdf:type	 capa:PocketFinishing.
	?em2 	cco:realizes ?pf1.
	
	?sm1	rdf:type	capa:SideMilling.
	?pr2	rdf:type	 capa:PocketRoughing.
	?sm1	cco:realizes ?pr2.
	
	?sm2	rdf:type	 capa:SideMilling.
	?pf2	rdf:type	 capa:PocketFinishing.
	?sm2 	cco:realizes ?pf2.	
	
	?root 	pp:hasSucceedingProcess ?em1.
	?root 	pp:hasSucceedingProcess ?sm1.
	?em1 	pp:hasSucceedingProcess ?em2.
	?sm1 	pp:hasSucceedingProcess ?sm2. 
}
WHERE{
	?pr1	rdf:type	 capa:PocketRoughing.
	?em1	rdf:type	 capa:EndMilling.
	?em1	cco:realizes ?pr1.
	?pf1	rdf:type	 capa:PocketFinishing.
	?em2	rdf:type	 capa:EndMilling.
	?em2 	cco:realizes ?pf1.
		
	?pr2	rdf:type	 capa:PocketRoughing.
	?sm1	rdf:type	 capa:SideMilling.
	?sm1	cco:realizes ?pr2.
	?pf2	rdf:type	 capa:PocketFinishing.
	?sm2	rdf:type	 capa:SideMilling.
	?sm2 	cco:realizes ?pf2.	
}