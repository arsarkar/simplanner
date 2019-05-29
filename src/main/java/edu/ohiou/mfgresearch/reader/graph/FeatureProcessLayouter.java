/**
 * 
 */
package edu.ohiou.mfgresearch.reader.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

import edu.ohiou.mfgresearch.labimp.graph.*;
import edu.ohiou.mfgresearch.labimp.gtk2d.InvalidArcException;
import edu.ohiou.mfgresearch.labimp.gtk3d.Arc2d;

/**
 * @author sormaz
 *
 */
public class FeatureProcessLayouter extends GraphLayouter {
	
	double deltaRadius = 0.0;
	int rankOfOrbit = 0;
	int numPlanets = 0;
	double angle = 0;
	Map<Node, java.lang.Double> positions = new HashMap<Node, java.lang.Double>(); 

	/**
	 * @param graph
	 * @param point
	 */
	public FeatureProcessLayouter(Graph graph, double deltaRadius) {
		super(graph, new Point2D.Double(0,0));
		this.deltaRadius = deltaRadius;
	}
	
	public void nextOrbit(){
		rankOfOrbit += 1;
		angle = 0;
		positions.clear();
	}
	
	public int getRank(){
		return rankOfOrbit;
	}
	
	public void setNumPlanets(int numPlanets){
		this.numPlanets = numPlanets;
	}
	
	public void nodeAdded(Node n) {
		if(rankOfOrbit>0){
			try {				
				Arc2d arc = new Arc2d(new Point2d(0.0, 0.0), deltaRadius*rankOfOrbit);
				Node parent = graph.findNode(n).getNeighbors().iterator().next();
				if(positions.get(parent)!=null){
					
				}
				angle = angle + (360/numPlanets)*(Math.PI/180);
				Point2d p = arc.getPoint(angle);
				positions.put(n, angle);
				vertices.put(n, new Vertex (n, new Point2D.Double(p.x, p.y)));
			} catch (InvalidArcException | NotMemberException e) {
				e.printStackTrace();
			}
		}
		else{
			vertices.put(n, new Vertex (n, new Point2D.Double(0.0, 0.0)));			
		}	
		if (canvas != null) canvas.repaint();
	}

}