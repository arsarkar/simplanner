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
	
	?em1	rdf:type	capa:EndMilling.
	?sr1	rdf:type	 capa:SlotRoughing.
	?em1	cco:realizes ?sr1.
	
	?em2	rdf:type	capa:EndMilling.
	?sf1	rdf:type	 capa:SlotFinishing.
	?em2 	cco:realizes ?sf1.
	
	?root	pp:hasSucceedingProcess ?pm.
	?pm 	pp:hasSucceedingProcess ?em1.
	?em1 	pp:hasSucceedingProcess ?em2.
}
WHERE{

	?pn		rdf:type 		capa:Plunging.
	?pm		rdf:type		capa:PlungeMilling.
	?pm		cco:realizes 	?pn.
	
	?sr1	rdf:type	 capa:SlotRoughing.
	?em1	rdf:type	 capa:EndMilling.
	?em1	cco:realizes ?sr1.
	
	?sf1	rdf:type	 capa:SlotFinishing.
	?em2	rdf:type	 capa:EndMilling.
	?em2 	cco:realizes ?sf1.
}