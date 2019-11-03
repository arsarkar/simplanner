PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX cco:<http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	?root 	rdf:type pp:RootProcess.
	
	?pn		rdf:type 		capa:Plunging.
	?pm		rdf:type		capa:PlungeMilling.
	?pm		cco:realizes 	?pn.
	
	?sm1	rdf:type	capa:SideMilling.
	?sr2	rdf:type	 capa:SlotRoughing.
	?sm1	cco:realizes ?sr2.
	
	?sm2	rdf:type	capa:SideMilling.
	?sf2	rdf:type	 capa:SlotFinishing.
	?sm2 	cco:realizes ?sf2.	
	
	?root	pp:hasSucceedingProcess ?pm.
	?pm 	pp:hasSucceedingProcess ?sm1.
	?sm1 	pp:hasSucceedingProcess ?sm2. 
}
WHERE{

	?pn		rdf:type 		capa:Plunging.
	?pm		rdf:type		capa:PlungeMilling.
	?pm		cco:realizes 	?pn.
	
	?sr2	rdf:type	 capa:SlotRoughing.
	?sm1	rdf:type	 capa:SideMilling.
	?sm1	cco:realizes ?sr2.
	
	?sf2	rdf:type	 capa:SlotFinishing.
	?sm2	rdf:type	 capa:SideMilling.
	?sm2 	cco:realizes ?sf2.	
}