/**
 * 
 */
package edu.ohiou.mfgresearch.reader.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import edu.ohiou.mfgresearch.labimp.graph.*;

/**
 * @author sormaz
 *
 */
public class FeatureProcessLayouter extends GraphLayouter {
	
	double currentX = 0.0;
	public double currentY = 0.0;

	/**
	 * @param graph
	 * @param point
	 */
	public FeatureProcessLayouter(Graph graph, Double point) {
		super(graph, point);
		// TODO Auto-generated constructor stub
	}
	
	public void nodeAdded(Node n) {
		vertices.put(n, new Vertex (n, new Point2D.Double(currentX, currentY)));
		currentX += 1.0;
//		
		if (canvas != null) canvas.repaint();
	}

}
