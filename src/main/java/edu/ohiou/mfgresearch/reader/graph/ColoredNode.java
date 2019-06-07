package edu.ohiou.mfgresearch.reader.graph;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.LinkedList;

import edu.ohiou.mfgresearch.labimp.basis.DrawString;
import edu.ohiou.mfgresearch.labimp.graph.DrawObject;

public class ColoredNode extends DrawObject {
	
	String label = "";
	String tooltip = "";
	
	public ColoredNode(String label, Color c) {
		super(new Point2D.Double(0, 0));
		setColor(c);
		this.label = label;
	}

	@Override
	public LinkedList<Shape> geetDrawList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<Shape> geetFillList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<DrawString> geetStringList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateImageList() {
		// TODO Auto-generated method stub

	}

	@Override
	public void makeDrawSets() {
		canvas.addDrawShapes(getColor(), geetDrawList());

	}

	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(toString());
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return label.hashCode();
	}

	@Override
	public String toToolTipString() {
		// TODO Auto-generated method stub
		return "tooltip of form feature";
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	
}
