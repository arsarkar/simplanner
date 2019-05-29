/**
 * 
 */
package edu.ohiou.mfgresearch.reader.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import edu.ohiou.mfgresearch.labimp.graph.*;
import edu.ohiou.mfgresearch.labimp.gtk2d.InvalidArcException;
import edu.ohiou.mfgresearch.labimp.gtk3d.Arc2d;
import edu.ohiou.mfgresearch.lambda.Uni;

/**
 * @author sormaz
 *
 */
public class FeatureProcessLayouter extends GraphLayouter {
	
	double deltaRadius = 0.0;
	int rankOfOrbit = 0;
	int numPlanets = 0;
	double angle = Math.PI;
	List<PlanetPosition> positions = new LinkedList<PlanetPosition>(); 
	Map<Node, double[]> orbitRanges = new HashMap<Node, double[]>();
	Map<Node, Integer> numChildren = new HashMap<Node, Integer>();
	List<Arc> currentArcs = new LinkedList<Arc>();
	

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
		angle = Math.PI;
		numChildren.clear();
		currentArcs.clear();
		calculateNextOrbitPositions();
	}

	public int getRank(){
		return rankOfOrbit;
	}
	
	public void setNumPlanets(int numPlanets){
		this.numPlanets = numPlanets;
	}
	
//	public void clearPositions(){
//		positions.clear();
//	}
	
	public void nodeAdded(Node n) {
		vertices.put(n, new Vertex (n, new Point2D.Double(0.0, 0.0)));
		if (rankOfOrbit==0) positions.add(new PlanetPosition(n, 0));
		if (canvas != null) canvas.repaint();
	}

	@Override
	public void arcAdded(Arc a) {
		super.arcAdded(a);
//		try {				
//			Arc2d arc = new Arc2d(new Point2d(0.0, 0.0), deltaRadius*rankOfOrbit);
//			
//			if(orbitRanges.size()>0){
//				Node parent = a.getParentNode();
//				if(numChildren.get(parent)!=null){
//					numChildren.put(parent, numChildren.get(parent)+1);
//				}
//				else{
//					numChildren.put(parent, 1);
//				}
//				double[] posRange = orbitRanges.get(parent);
//				
//				angle = angle + posRange[0] + (posRange[1]-posRange[0]) / numChildren.get(parent);
//			}
//			else{
//				angle = angle + (360/numPlanets)*(Math.PI/180);
//			}
//			
////			positions.add(new PlanetPosition(a.getChildNode(), angle));
//			Point2d p = arc.getPoint(angle);
//			vertices.put(a.getChildNode(), new Vertex (a.getChildNode(), new Point2D.Double(p.x, p.y)));
//		} catch (InvalidArcException e) {
//			e.printStackTrace();
//		}
		if(rankOfOrbit>0) {
			currentArcs.add(a);
			Node parent = a.getParentNode();
			if(numChildren.get(parent)!=null){
				numChildren.put(parent, numChildren.get(parent)+1);
			}
			else{
				numChildren.put(parent, 1);
			}
		}
	}
	
	@Override
	public void repositionEdges() {
		//sort by parents
//		Collections.sort(positions, (p1, p2)->{
//			if(p1.getParent()==null) return 0;
//			if(p1.getParent().equals(p2.getParent())){
//				return 0;
//			}
//			return 1;
//		});
		
		//create an orbit
		Arc2d arc = Uni.of(deltaRadius*rankOfOrbit).map(r->new Arc2d(new Point2d(0.0, 0.0), r)).get();
		
		//add children planents for each parent planet
		for(Node parent:numChildren.keySet()){
			//get the range of angle for this parent
			double[] posRange = orbitRanges.get(parent);
			angle = posRange[0]; //set the initial angle
			//for each children planet
			for(int i=0;i<numChildren.get(parent); i++){
				//calculate angle
				angle = angle + (posRange[1]-posRange[0]) / numChildren.get(parent);
				Point2d p = arc.getPoint(angle);
				Node child = extractPosition(parent);
				vertices.put(child, new Vertex (child, new Point2D.Double(p.x, p.y)));
				positions.add(new PlanetPosition(child, angle));
			}
		}
		
		super.repositionEdges();
	}
	
	public Node extractPosition(Node parent){
		int i = 0;
		Node child = null;
		for(;i<currentArcs.size();i++){
			if(currentArcs.get(i).getParentNode().equals(parent)){
				child = currentArcs.get(i).getChildNode();
				break;
			}
		}
		currentArcs.remove(i);
		return child;
	}
	
	private void calculateNextOrbitPositions() {
		if(positions.size()==0) return;
		Collections.sort(positions, (p1, p2)->{
			return p1.angle>p2.angle?1:(p1.angle==p2.angle?0:-1);
		});
		int s = positions.size();
		orbitRanges.clear();
		for(int i=0;i<s;i++){
			int previ = i-1<0?s-1:i-1;
			double ba = positions.get(previ).getAngle() + (Math.abs(positions.get(i).getAngle() - positions.get(previ).getAngle()) / 2);
			int nexti = i+1<s?i+1:0;
			double ea = positions.get(i).getAngle() + (Math.abs(positions.get(nexti).getAngle() - positions.get(i).getAngle()) / 2);
			orbitRanges.put(positions.get(i).getN(), new double[]{ba, ea==0?Math.PI*2:ea});
		}
		positions.clear();
	}

	class PlanetPosition{
		Node n;
//		Node parent;
		double angle;		
		
		public PlanetPosition(Node n, double angle) {
			super();
			this.n = n;
//			this.parent = parent;
			this.angle = angle;
		}
				
		public Node getN() {
			return n;
		}
		
//		public Node getParent() {
//			return parent;
//		}

		public double getAngle() {
			return angle;
		}
	}

}
