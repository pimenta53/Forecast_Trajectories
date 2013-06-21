package ontology;

import java.util.HashSet;
import java.util.LinkedHashSet;

import jade.content.Predicate;


public class SensorSet implements Predicate{

	private LinkedHashSet<Sensor> newsSensors = new LinkedHashSet<Sensor>();
	private int total = 0;

	
	public LinkedHashSet<Sensor> getNewsSensors() {
		return newsSensors;
	}
	
	public void addSensor(Sensor s){
		total++;
		newsSensors.add(s);
	}
	
	public void setNewsSensors(LinkedHashSet<Sensor> newsSensors) {
		this.newsSensors = newsSensors;
	}
	
	public int getTotal() {
		return total;
	}
	
	public void setTotal(int total) {
		this.total = total;
	}
	
	public void restart(){
		LinkedHashSet<Sensor> novo = new LinkedHashSet<Sensor>();
		int i = 0;
		for(Sensor s : newsSensors){
			if(i >= 2) 
				break;
			novo.add(s);
			i++;
		}
		newsSensors.clear();
		newsSensors.addAll(novo);
	}
	
}
