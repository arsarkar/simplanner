package edu.ohiou.mfgresearch.reader.graph;

import java.awt.Shape;
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
	double beginAngle, endAngle, expansionFactor;
	List<PlanetPosition> positions = new LinkedList<PlanetPosition>(); 
	Map<Node, double[]> orbitRanges = new HashMap<Node, double[]>();
	Map<Node, Integer> numChildren = new HashMap<Node, Integer>();
	List<Arc> currentArcs = new LinkedList<Arc>();
	

	/**
	 * @param graph
	 * @param point
	 */
	public FeatureProcessLayouter(Graph graph, double deltaRadius, double beginAngle, double endAngle, double expansionFactor) {
		super(graph, new Point2D.Double(0,0));
		this.deltaRadius = deltaRadius;
		this.beginAngle = beginAngle;
		this.endAngle = endAngle;
		this.expansionFactor = expansionFactor;
	}
	
	public void nextOrbit(){
		rankOfOrbit += 1;
		angle = Math.PI;
		numChildren.clear();
		currentArcs.clear();
		calculateNextOrbitPositions();
//		beginAngle = beginAngle * (expansionFactor - 1);
//		endAngle = endAngle * (1 + expansionFactor);
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
		
		//add children planents for each parent planet
		for(Node parent:numChildren.keySet()){			
			//create an orbit
			Point2D center = vertices.get(parent).geettPosition();
			Arc2d arc = Uni.of(deltaRadius).map(r->new Arc2d(new Point2d(center.getX(), center.getY()), r)).get();
			//get the range of angle for this parent
			double[] posRange = orbitRanges.get(parent);
			double spacing = (posRange[1]-posRange[0]) / (numChildren.get(parent));
			angle = posRange[0]+spacing/2; //set the initial angle
			
			//for each children planet
			for(int i=0;i<numChildren.get(parent); i++){
				//calculate angle
				Point2d p = arc.getPoint(angle);
				Node child = extractPosition(parent);
				vertices.put(child, new Vertex (child, new Point2D.Double(p.x, p.y)));
				positions.add(new PlanetPosition(child, angle));
				System.out.println("DNS + ARKO> " + "P>" + child + ", angle>" + angle );
				angle = angle + spacing;
			}
		}
		
		super.repositionEdges();
	}
	
	
	@Override
	public LinkedList<Shape> geetDrawList() {
		// TODO Auto-generated method stub
		LinkedList<Shape> returnList = super.geetDrawList();
		try {
			if(rankOfOrbit>0) returnList.addAll(new Arc2d(new Point2d(0.0, 0.0), deltaRadius*rankOfOrbit).geetDrawList());
		} catch (InvalidArcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnList;
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
		
		if(rankOfOrbit==1){
			//when parent is only a center
			orbitRanges.put(positions.get(0).getN(), new double[]{beginAngle, endAngle});
		}
		else{
			int s = positions.size();
			double bAngle = 0, eAngle = 0;
			for(int i=0;i<s;i++){
				if(i==0){
					bAngle = beginAngle;
					eAngle = positions.get(i).getAngle() + (Math.abs(positions.get(i+1).getAngle() - positions.get(i).getAngle()) / 2);
				}
				else if(i==s-1){
					bAngle = positions.get(i-1).getAngle() + (Math.abs(positions.get(i).getAngle() - positions.get(i-1).getAngle()) / 2);
					eAngle = endAngle;
				}
				else{
					bAngle = positions.get(i-1).getAngle() + (Math.abs(positions.get(i).getAngle() - positions.get(i-1).getAngle()) / 2);
					eAngle = positions.get(i).getAngle() + (Math.abs(positions.get(i+1).getAngle() - positions.get(i).getAngle()) / 2);
				}
				
//				int previ = i-1<0?s-1:i-1; double ba = 0.0;double ea= 0.0;
//				if(positions.get(i).getAngle() > positions.get(previ).getAngle()){
//					ba = positions.get(previ).getAngle() + (Math.abs(positions.get(i).getAngle() - positions.get(previ).getAngle()) / 2);
//				}
//				else if(positions.get(i).getAngle() < positions.get(previ).getAngle()){
//					ba = positions.get(previ).getAngle() + (Math.abs(positions.get(i).getAngle() + (Math.PI*2 - positions.get(previ).getAngle())) / 2);
//				}
//				ba = ba>Math.PI*2?ba-Math.PI*2:ba;
//				ba = ba==Math.PI*2?0:ba;
//				
//				int nexti = i+1<s?i+1:0;
//				if(positions.get(nexti).getAngle() > positions.get(i).getAngle()){
//					ea = positions.get(i).getAngle() + (Math.abs(positions.get(nexti).getAngle() - positions.get(i).getAngle()) / 2);
//				}
//				else if(positions.get(nexti).getAngle() < positions.get(i).getAngle()){
//					ea = positions.get(i).getAngle() + (Math.abs(positions.get(nexti).getAngle() + (Math.PI*2 - positions.get(i).getAngle())) / 2);
//				}
//				ea = ea>Math.PI*2?ea-Math.PI*2:ea;
//				ea = ea==Math.PI*2?0:ea;
				
				orbitRanges.put(positions.get(i).getN(), new double[]{bAngle, eAngle});
			}
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
