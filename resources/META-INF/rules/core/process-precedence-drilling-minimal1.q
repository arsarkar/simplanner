PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	
	?root rdf:type pp:RootProcess.
	?td rdf:type capa:TwistDrilling.
	?ed rdf:type capa:EndDrilling.
	?rm rdf:type capa:Reaming.
	?hg rdf:type capa:HoleGrinding.
	
	?root pp:hasSucceedingProcess ?td.
	?root pp:hasSucceedingProcess ?ed.
	
	?td pp:hasSucceedingProcess ?rm.
	
	?ed pp:hasSucceedingProcess ?rm.
	
	?rm pp:hasSucceedingProcess ?hg.
}
WHERE{
	?td rdf:type capa:TwistDrilling.
	?ed rdf:type capa:EndDrilling.
	?rm rdf:type capa:Reaming.
	?hg rdf:type capa:HoleGrinding.
}