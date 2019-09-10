PREFIX  this: <edu.ohiou.mfgresearch.simplanner.PartSpecificationGraph>
PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
PREFIX  cco:  <http://www.ontologyrepository.com/CommonCoreOntologies/>
PREFIX  design: <http://www.ohio.edu/ontologies/design#>
PREFIX  plan: <http://www.ohio.edu/ontologies/manufacturing-plan#>

CONSTRUCT 
  { 
	plan:hasSucceedingFeature rdf:type owl:ObjectProperty .
	?r 		plan:hasSucceedingFeature    ?rNext.
  }
WHERE
  { 
	?p		rdf:type					design:PartSpecification.
	?pf		rdf:type					design:PartFeatureMap;
			design:describes_map_with	?p;
			design:describes_map_with	?fs.
	?fs  	rdf:type              		design:FeatureSpecification;
			cco:represents 				?r.
	?r		cco:has_text_value   		?fNext.
	?fsNext	rdf:type              		design:FeatureSpecification;
			cco:inheres_in       		?i1;
			cco:represents 				?rNext.
	?i1  	rdf:type              		design:LabelBearingEntity;
			cco:has_text_value   		?fNext.		
  }