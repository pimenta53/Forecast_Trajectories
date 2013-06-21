package ontology;


import java.util.*;
import jade.content.Concept;


public class Way implements Concept{

	private ArrayList<Point2D> trajectory = new ArrayList<Point2D>();

	
	public ArrayList<Point2D> getTrajectory() {
		return trajectory;
	}

	public void setTrajectory(ArrayList<Point2D> trajectory) {
		this.trajectory = trajectory;
	}

	public void move(Point2D pt){
		trajectory.add(pt.clone());
	}

	public String toString(){
		StringBuilder s = new StringBuilder();
		s.append("\nCAMINHO:\n");
		for(Point2D pt : trajectory) 
			s.append("POSICAO - " + pt.toString() + "\n");
		s.append("FIM\n");
	return s.toString();
	}

}