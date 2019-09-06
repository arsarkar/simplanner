PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root 	rdf:type pp:RootProcess.
	?em1	rdf:type	capa:EndMilling.
	?em2	rdf:type	capa:EndMilling.
	?sm1	rdf:type	capa:SideMilling.
	?sm2	rdf:type	capa:SideMilling.
	?root 	pp:hasSucceedingProcess ?em1.
	?root 	pp:hasSucceedingProcess ?sm1.
	?em1 	pp:hasSucceedingProcess ?em2.
	?sm1 	pp:hasSucceedingProcess ?sm2. 
}
WHERE{
	?sr1	rdf:type	 capa:SlotRoughing.
	?em1	rdf:type	 capa:EndMilling.
	?em1	cco:realizes ?sr1.
	?sf1	rdf:type	 capa:SlotFinishing.
	?em2	rdf:type	 capa:EndMilling.
	?em2 	cco:realizes ?sf1.
		
	?sr2	rdf:type	 capa:SlotRoughing.
	?sm1	rdf:type	 capa:SideMilling.
	?sm1	cco:realizes ?sr2.
	?sf2	rdf:type	 capa:SlotFinishing.
	?sm2	rdf:type	 capa:SideMilling.
	?sm2 	cco:realizes ?sf2.	
}