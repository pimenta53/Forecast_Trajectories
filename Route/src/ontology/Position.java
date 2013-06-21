package ontology;

import jade.content.Predicate;


public class Position implements Predicate{

	private Point2D pos;
	private long time ;
	
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Point2D getPos() {
		return pos;
	}

	public void setPos(Point2D pos) {
		this.pos = pos;
	}
	
}
