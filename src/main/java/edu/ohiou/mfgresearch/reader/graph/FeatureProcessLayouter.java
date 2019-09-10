package edu.ohiou.mfgresearch.reader.graph;

import java.awt.Color;
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
	Map<ColoredNode, double[]> orbitRanges = new HashMap<ColoredNode, double[]>();
	Map<ColoredNode, Integer> numChildren = new HashMap<ColoredNode, Integer>();
	List<Arc> currentArcs = new LinkedList<Arc>();
	boolean satellite = true;

	/**
	 * Create a FeatureProcessLayouter
	 * @param graph
	 * @param deltaRadius Distance between parent and child process node
	 * @param beginAngle  Begin limit of the radial span of the tree expansion  
	 * @param endAngle	  End limit of the radial span of the tree expansion  		
	 * @param expansionFactor	increment of the expansion for every orbit
	 */
	public FeatureProcessLayouter(Graph graph, double deltaRadius, double beginAngle, double endAngle, double expansionFactor, boolean satellite) {
		super(graph, new Point2D.Double(0,0));
		this.deltaRadius = deltaRadius;
		this.beginAngle = beginAngle;
		this.endAngle = endAngle;
		this.expansionFactor = expansionFactor;
		this.satellite = satellite;
	}
	
	/**
	 * Jump to next orbit
	 */
	public void nextOrbit(){
		rankOfOrbit += 1;
		angle = Math.PI;
		numChildren.clear();
		currentArcs.clear();
		calculateNextOrbitPositions();
		beginAngle = beginAngle - beginAngle * (expansionFactor-1);
		endAngle = endAngle + endAngle * (expansionFactor-1);
	}

	public int getRank(){
		return rankOfOrbit;
	}
	
	public void setNumPlanets(int numPlanets){
		this.numPlanets = numPlanets;
	}
	
	@Override
	public void nodeAdded(Node n) {
		super.nodeAdded(n);
//		vertices.put(n, new Vertex (n, new Point2D.Double(0.0, 0.0)));
		if (rankOfOrbit==0) positions.add(new PlanetPosition((ColoredNode) n.getUserObject(), 0));
//		if (canvas != null) canvas.repaint();
	}

	@Override
	public void arcAdded(Arc a) {
		super.arcAdded(a);
		if(rankOfOrbit>0) {
			currentArcs.add(a);
			if(!a.getUserObject().equals(new ColoredArc("", Color.MAGENTA))){
				ColoredNode parent = (ColoredNode) a.getParentNode().getUserObject();
				if(numChildren.get(parent)!=null){
					numChildren.put(parent, numChildren.get(parent)+1);
				}
				else{
					numChildren.put(parent, 1);
				}
			}
		}
	}
	
	@Override
	public void repositionEdges() {
		
		//add children planents for each parent planet
		for(ColoredNode parent:numChildren.keySet()){			
			//create an orbit
			Point2D center = getVertexByObject(parent).geettPosition();
			Arc2d orbit = Uni.of(deltaRadius).map(r->new Arc2d(new Point2d(center.getX(), center.getY()), r)).get();
			//get the range of angle for this parent
			if(!orbitRanges.containsKey(parent)) continue;
			double[] posRange = orbitRanges.get(parent);
			double spacing = (posRange[1]-posRange[0]) / (numChildren.get(parent));
			angle = posRange[0]+spacing/2; //set the initial angle
			
			//for each children planet
			for(int i=0;i<numChildren.get(parent); i++){
				//calculate angle
				Point2d p = orbit.getPoint(angle);
				Node child = findChildByParentPlanet(parent, new ColoredArc("precedes", Color.GREEN));
				//draw the planet node
				getVertexByObject((ColoredNode) child.getUserObject()).settPosition(new Point2D.Double(p.x, p.y));
//				vertices.put(child, new Vertex (child, new Point2D.Double(p.x, p.y)));
				//save the angle in the positions
				positions.add(new PlanetPosition((ColoredNode) child.getUserObject(), angle));
//				System.out.println("DNS + ARKO> " + "P>" + child + ", angle>" + angle );
			
				//add the satellite node
				if(satellite){
					Arc2d satOrb = Uni.of(deltaRadius*0.7).map(r->new Arc2d(new Point2d(center.getX(), center.getY()), r)).get();
					Point2d p1 = satOrb.getPoint(angle+.2);
					Node sat = findChildByParentPlanet((ColoredNode) child.getUserObject(), new ColoredArc("has_output", Color.MAGENTA));
					//draw the satellite node
					getVertexByObject((ColoredNode) sat.getUserObject()).settPosition(new Point2D.Double(p1.x, p1.y));
				}
				//increment the angle
				angle = angle + spacing;
			}
			
		}
		
		super.repositionEdges();
	}
	
	/**
	 * get the vertex from the vertices by matching the user object of the node
	 * @param cn
	 * @return
	 */
	public Vertex getVertexByObject(ColoredNode cn){
		Vertex v = null;
		for(Node n:vertices.keySet()){
			if(n.getUserObject().equals(cn)){
				return vertices.get(n);
			}
		}
		return null;
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

	public Node findChildByParentPlanet(ColoredNode parent, ColoredArc arc){
		int i = 0;
		Node child = null;
		for(;i<currentArcs.size();i++){
			if(currentArcs.get(i).getUserObject().equals(arc)){
				if(currentArcs.get(i).getParentNode().getUserObject().equals(parent)){
					child = currentArcs.get(i).getChildNode();
					break;
				}
			}
		}
		currentArcs.remove(i);
		return child;
	}
	
	/**
	 * Calculate begin and end angle on the next orbit for each planet in the current orbit.
	 * current planets and their angles are in positions list
	 */
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
					if(i+1>=s){
						eAngle = endAngle;
					}
					else{
						eAngle = positions.get(i).getAngle() + (Math.abs(positions.get(i+1).getAngle() - positions.get(i).getAngle()) / 2);
					}
				}
				else if(i==s-1){
					bAngle = positions.get(i-1).getAngle() + (Math.abs(positions.get(i).getAngle() - positions.get(i-1).getAngle()) / 2);
					eAngle = endAngle;
				}
				else{
					bAngle = positions.get(i-1).getAngle() + (Math.abs(positions.get(i).getAngle() - positions.get(i-1).getAngle()) / 2);
					eAngle = positions.get(i).getAngle() + (Math.abs(positions.get(i+1).getAngle() - positions.get(i).getAngle()) / 2);
				}
				
				orbitRanges.put(positions.get(i).getN(), new double[]{bAngle, eAngle});
			}
		}
		positions.clear();
	}

	class PlanetPosition{
		ColoredNode n;
//		Node parent;
		double angle;		
		
		public PlanetPosition(ColoredNode n, double angle) {
			super();
			this.n = n;
//			this.parent = parent;
			this.angle = angle;
		}
				
		public ColoredNode getN() {
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
