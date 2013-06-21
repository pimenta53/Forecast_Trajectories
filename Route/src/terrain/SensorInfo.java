package terrain;

import ontology.Point2D;


public class SensorInfo {

	private String sensor;
	private Point2D pt = new Point2D();
	private int radius;
	
	
	public SensorInfo(String name, Point2D pt, int rd){
		sensor = name;
		this.pt = pt;
		radius = rd;
	}

	
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

	public String getSensor() {
		return sensor;
	}

	public void setSensor(String sensor) {
		this.sensor = sensor;
	}

	@Override
	public String toString() {
		return "SensorInfo [sensor = " + sensor + ", pt = " + pt.toString() + ", radius = "
				+ radius + "]";
	}
	
}