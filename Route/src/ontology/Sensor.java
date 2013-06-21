package ontology;

import jade.content.Concept;


public class Sensor implements Concept{

	private Point2D pt = new Point2D();
	private int radius = 0;

	
	public Point2D getPt() {
		return pt;
	}
	
	public void setPt(Point2D pt) {
		this.pt = pt;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public void setRadius(int radius) {
		this.radius = radius;
	}
	
}
