package edu.ohiou.mfgresearch.reader.graph;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.LinkedList;

import edu.ohiou.mfgresearch.labimp.basis.DrawString;
import edu.ohiou.mfgresearch.labimp.graph.DrawObject;

public class PropertyArc extends DrawObject {
	
	String label = "";
	
	public PropertyArc(String label, Color c) {
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
	public String toString() {
		return label;
	}
}
