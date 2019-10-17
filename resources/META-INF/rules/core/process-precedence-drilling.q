PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX capa:<http://www.ohio.edu/ontologies/manufacturing-capability#>
PREFIX pp:<http://www.ohio.edu/ontologies/manufacturing-plan#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>

CONSTRUCT{
	pp:hasSucceedingProcess rdf:type owl:ObjectProperty.
	pp:hasOptionallySucceedingProcess rdf:type owl:ObjectProperty.
	
	?root rdf:type pp:RootProcess.
	
	?sp rdf:type capa:SpotDrilling.
	?sp 	cco:realizes	?spfunc.
	?spfunc	rdf:type		?spfType.
	
	?td rdf:type capa:TwistDrilling.
	?td 	cco:realizes	?tdfunc.
	?tdfunc	rdf:type		?tdfType.
		
	?sd 	rdf:type 		capa:SpadeDrilling.
	?sd 	cco:realizes	?sdfunc.
	?sdfunc	rdf:type		?sdfType.
	
	?ed rdf:type capa:EndDrilling.
	?ed 	cco:realizes	?edfunc.
	?edfunc	rdf:type		?edfType.	
	
	?gd rdf:type capa:GunDrilling.
	?gd 	cco:realizes	?gdfunc.
	?gdfunc	rdf:type		?gdfType.
	
	?br rdf:type capa:Boring.
	?br 	cco:realizes	?brfunc.
	?brfunc	rdf:type		?brfType.	
	
	?pb rdf:type capa:PrecisionBoring.
	?pb 	cco:realizes	?pbfunc.
	?pbfunc	rdf:type		?pbfType.	
	
	?rm rdf:type capa:Reaming.
	?rm 	cco:realizes	?rmfunc.
	?rmfunc	rdf:type		?rmfType.	
		
	?hn rdf:type capa:Honing.
	?hn cco:realizes	?hnfunc.
	?hnfunc	rdf:type		?hnfType.		
	
	?hg rdf:type capa:HoleGrinding.
	?hg 	cco:realizes	?hgfunc.
	?hgfunc	rdf:type		?hgfType.
	
	?root pp:hasSucceedingProcess ?sp.	
	
	?sp pp:hasOptionallySucceedingProcess ?td.
	?sp pp:hasOptionallySucceedingProcess ?ed.
	?sp pp:hasOptionallySucceedingProcess ?sd.
	
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
	?sp rdf:type capa:SpotDrilling.
	?sp 	cco:realizes	?spfunc.
	?spfunc	rdf:type		?spfType.

	?td rdf:type capa:TwistDrilling.
	?td 	cco:realizes	?tdfunc.
	?tdfunc	rdf:type		?tdfType.
		
	?sd 	rdf:type 		capa:SpadeDrilling.
	?sd 	cco:realizes	?sdfunc.
	?sdfunc	rdf:type		?sdfType.
	
	?ed rdf:type capa:EndDrilling.
	?ed 	cco:realizes	?edfunc.
	?edfunc	rdf:type		?edfType.	
	
	?gd rdf:type capa:GunDrilling.
	?gd 	cco:realizes	?gdfunc.
	?gdfunc	rdf:type		?gdfType.
	
	?br rdf:type capa:Boring.
	?br 	cco:realizes	?brfunc.
	?brfunc	rdf:type		?brfType.	
	
	?pb rdf:type capa:PrecisionBoring.
	?pb 	cco:realizes	?pbfunc.
	?pbfunc	rdf:type		?pbfType.	
	
	?rm rdf:type capa:Reaming.
	?rm 	cco:realizes	?rmfunc.
	?rmfunc	rdf:type		?rmfType.	
		
	?hn rdf:type capa:Honing.
	?hn cco:realizes	?hnfunc.
	?hnfunc	rdf:type		?hnfType.		
	
	?hg rdf:type capa:HoleGrinding.
	?hg 	cco:realizes	?hgfunc.
	?hgfunc	rdf:type		?hgfType.
	
	FILTER (?spfType NOT IN (owl:NamedIndividual))
	FILTER (?tdfType NOT IN (owl:NamedIndividual))
	FILTER (?sdfType NOT IN (owl:NamedIndividual))
	FILTER (?edfType NOT IN (owl:NamedIndividual))
	FILTER (?gdfType NOT IN (owl:NamedIndividual))
	FILTER (?brfType NOT IN (owl:NamedIndividual))
	FILTER (?pbfType NOT IN (owl:NamedIndividual))
	FILTER (?rmfType NOT IN (owl:NamedIndividual))
	FILTER (?hnfType NOT IN (owl:NamedIndividual))
	FILTER (?hgfType NOT IN (owl:NamedIndividual))	
}