PREFIX  rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX  owl:<http://www.w3.org/2002/07/owl#>
PREFIX	graph:<http://www.ohio.edu/ontologies/graph5#>
PREFIX	g1:<http://www.ohio.edu/ontologies/graph/test1#>

SELECT ?n1 ?n2
WHERE{
?n1 rdf:type	graph:RootNode.
?n1 graph:hasChild* ?n2.
FILTER NOT EXISTS {?n2 graph:hasChild ?n3}
}