PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	
	?root rdf:type pp:RootProcess.
	?td rdf:type capa:TwistDrilling.
	?sd rdf:type capa:SpadeDrilling.
	?ed rdf:type capa:EndDrilling.
	?gd rdf:type capa:GunDrilling.
	?br rdf:type capa:Boring.
	?pb rdf:type capa:PrecisionBoring.
	?rm rdf:type capa:Reaming.
	?hn rdf:type capa:Honing.
	?hg rdf:type capa:HoleGrinding.
	
	?root pp:hasSucceedingProcess ?td.
	?root pp:hasSucceedingProcess ?ed.
	?root pp:hasSucceedingProcess ?sd.
	
	?td pp:hasSucceedingProcess ?br.
	?td pp:hasSucceedingProcess ?pb.
	?td pp:hasSucceedingProcess ?rm.
	
	?ed pp:hasSucceedingProcess ?br.
	?ed pp:hasSucceedingProcess ?pb.
	?ed pp:hasSucceedingProcess ?rm.
	
	?sd pp:hasSucceedingProcess ?br.
	?sd pp:hasSucceedingProcess ?pb.
	?sd pp:hasSucceedingProcess ?rm. 	
		
	?br pp:hasSucceedingProcess ?hg.
	?br pp:hasSucceedingProcess ?hn.
	?br pp:hasSucceedingProcess ?rm.
		
	?pb pp:hasSucceedingProcess ?hg.
	?pb pp:hasSucceedingProcess ?hn.
	?pb pp:hasSucceedingProcess ?rm.
		
	?rm pp:hasSucceedingProcess ?hg.
	?rm pp:hasSucceedingProcess ?hn   
}
WHERE{
	?td rdf:type capa:TwistDrilling.
	?sd rdf:type capa:SpadeDrilling.
	?ed rdf:type capa:EndDrilling.
	?gd rdf:type capa:GunDrilling.
	?br rdf:type capa:Boring.
	?pb rdf:type capa:PrecisionBoring.
	?rm rdf:type capa:Reaming.
	?hn rdf:type capa:Honing.
	?hg rdf:type capa:HoleGrinding.
}