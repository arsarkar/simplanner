PREFIX  rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:<http://www.w3.org/2002/07/owl#>
PREFIX	graph:<http://www.ohio.edu/ontologies/graph5#>
PREFIX	g1:<http://www.ohio.edu/ontologies/graph/test1#>

SELECT?n3 ?n4 ?n2
WHERE{
?n3 ^graph:hasChild ?n2
  {
	SELECT ?n3 ?n4
	WHERE{
		?n3 rdf:type	graph:Node.
		?n4 rdf:type	graph:Node.
		?n3	graph:hasChild+ ?n4.
		FILTER NOT EXISTS {?n3 graph:hasChild ?n4}.
	}
  }
}