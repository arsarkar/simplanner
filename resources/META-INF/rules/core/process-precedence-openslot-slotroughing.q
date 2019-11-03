PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root 	rdf:type pp:RootProcess.
	
	?em1	rdf:type	capa:EndMilling.
	?sr1	rdf:type	 capa:SlotRoughing.
	?em1	cco:realizes ?sr1.
	
	?sm1	rdf:type	capa:SideMilling.
	?sr2	rdf:type	 capa:SlotRoughing.
	?sm1	cco:realizes ?sr2.
	
	?root 	pp:hasSucceedingProcess ?em1.
	?root 	pp:hasSucceedingProcess ?sm1. 
}
WHERE{
	?sr1	rdf:type	 capa:SlotRoughing.
	?em1	rdf:type	 capa:EndMilling.
	?em1	cco:realizes ?sr1.
		
	?sr2	rdf:type	 capa:SlotRoughing.
	?sm1	rdf:type	 capa:SideMilling.
	?sm1	cco:realizes ?sr2.	
}