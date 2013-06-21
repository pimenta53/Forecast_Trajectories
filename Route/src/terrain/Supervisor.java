package terrain;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;

import javax.swing.JOptionPane;

import ontology.*;
import interaction.*;
import jade.content.ContentException;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;


public class Supervisor extends Agent {
	
	private Way realWay = new Way(); //caminho do walker
	private Way sensorsWay = new Way(); //caminho entre as antenas
	private SensorSet sensors = new SensorSet();
	private int numSensors = 0; //numero inicial de sensores posicionados
	private int maxSensors = 100; //numero maximo de sensores que podem ser criados
	public int inputSensors = 2; //numero inicial de sensores posicionados
	
	protected void setup(){
		
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(TrajectoryOntology.getInstance());	
		
		Object[] args = getArguments();
		if(args != null){
			inputSensors = Integer.parseInt(args[0].toString());
			maxSensors = Integer.parseInt(args[1].toString());
		}
		
		initSensors();
				
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				
				ACLMessage msg = receive();
				if(msg != null){
					if(msg.getSender().getLocalName().equals("walker")){	
						try{
							RealWay rWay = (RealWay) myAgent.getContentManager().extractContent(msg);
							System.out.println(rWay.getRealWay().toString());
							realWay = rWay.getRealWay();
							requestSupposedWay();
						}catch(Exception e) {e.printStackTrace();}
					}
					if(msg.getSender().getLocalName().equals("SensorCommunication")){	
						try{
							SupposedWay sWay = (SupposedWay) myAgent.getContentManager().extractContent(msg);
							sensorsWay = sWay.getSupposedlWay();
							Thread.sleep(2000);
							System.out.println(sWay.getSupposedlWay().toString());
							JFramePrincipal.terreno.setWay(sWay.getSupposedlWay(), 1);
							Thread.sleep(2000);
							requestRatio();
							sensors.restart();
							numSensors++;
							requestNewSensor();
						}catch(Exception e) {e.printStackTrace();}
					}
				}
				else block();
			}
		});
	}
	
	public void initSensors(){
		
		addBehaviour (new OneShotBehaviour(this) {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
		
		
		
		Random random = new Random();
		int i = 1, x = 0, y = 0;
	 	int range = Terrain.range;
		boolean parede = false;
	 	
		while (i <= inputSensors){
			if(i == 1){
				if(Terrain.getStart().getX() + 1 <= 79 && !parede)
					x = Terrain.getStart().getX() + 1;
				else if (Terrain.getStart().getX() - 1 >= 0)
					x = Terrain.getStart().getX() - 1;
				if(Terrain.getStart().getY() + 1 <= 79 && !parede)
					y = Terrain.getStart().getY() + 1;
				else if (Terrain.getStart().getY() - 1 >= 0)
					y = Terrain.getStart().getY() - 1;
			}
			else if(i == 2){
				if(Terrain.getEnd().getX() + 1 <= 79 && !parede)
					x = Terrain.getEnd().getX() + 1;
				else if (Terrain.getEnd().getX() - 1 >= 0)
					x = Terrain.getEnd().getX() - 1;
				if(Terrain.getEnd().getY() + 1 <= 79 && !parede)
					y = Terrain.getEnd().getY() + 1;
				else if (Terrain.getEnd().getY() - 1 >= 0)
					y = Terrain.getEnd().getY() - 1;
			}
			else{
				x = random.nextInt(80-(2*range)) + range;
				y = random.nextInt(80-(2*range)) + range;
			}
				if(!JFramePrincipal.terreno.getCell(x, y).equals("p")){
					Point2D pt = new Point2D();
				    pt.setX(x);
				    pt.setY(y);
				    parede = false;
				        
				    Sensor sensor = new Sensor();
				    sensor.setPt(pt.clone());
			        sensor.setRadius(Terrain.range);
			            
			        sensors.addSensor(sensor); 
			        numSensors++;
			        i++;
			}
				else parede = true;
		}
	    try {
	    	ACLMessage msg = new ACLMessage(ACLMessage.INFORM) ;
	    	msg.setOntology(TrajectoryOntology.getInstance().getName());
	    	msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
	    	try {
	    		getContentManager().fillContent(msg,sensors);
				msg.addReceiver(new AID("SensorCommunication",AID.ISLOCALNAME));
				send(msg); // envia a mensagem
			} catch (ContentException ce) {ce.printStackTrace();}
	    	} catch(Exception e) {e.printStackTrace();}
			}
			});
	}
	
	public void requestRatio(){

		int i,j;
		double percentage = 0.0;
		int range = Terrain.range;
		
		ArrayList<Point2D> auxSup = new ArrayList<Point2D>();
		ArrayList<Point2D> aux = new ArrayList<Point2D>();
		Way suposedWay = new Way();
		
		ArrayList<Point2D> trajectory = sensorsWay.getTrajectory();
		for(int u = 0; u < trajectory.size()-1; u++){
				
				int x1 = trajectory.get(u).getX();
				int y1 = trajectory.get(u).getY();
				
				int x2 = trajectory.get(u+1).getX();
				int y2 = trajectory.get(u+1).getY();
				supposedWay(x1, y1, x2, y2, auxSup);
		}
		suposedWay.setTrajectory(auxSup);
		
		for(Point2D pt : suposedWay.getTrajectory()){
			
			i = pt.getX();
			j = pt.getY();

			for (int x = i-(range/2); x <= i+(range/2); x++){
				for (int y = j-(range/2); y <= j+(range/2); y++){
					Point2D ptWalker = new Point2D(x,y);
					if((x >= 0 && x < 80 && y >= 0 && y < 80) && (JFramePrincipal.terreno.getCell(x,y).equals("w") || 
						JFramePrincipal.terreno.getCell(x,y).equals("c") || JFramePrincipal.terreno.getCell(x, y).equals("i")
						|| JFramePrincipal.terreno.getCell(x, y).equals("f")) && !aux.contains(ptWalker)){
						aux.add(ptWalker);
					}
				}
			}
		}
		
		for(Sensor s : sensors.getNewsSensors()){
			System.out.println(s.getPt().toString());
			Point2D pt = s.getPt();
			i = pt.getX();
			j = pt.getY();
				
			for (int x = i-range; x <= i+range; x++){
				for (int y = j-range; y <= j+range; y++){
					Point2D ptWalker = new Point2D(x,y);
					if((x >= 0 && x < 80 && y >= 0 && y < 80) && (JFramePrincipal.terreno.getCell(x,y).equals("w") || 
						JFramePrincipal.terreno.getCell(x,y).equals("c") || JFramePrincipal.terreno.getCell(x, y).equals("i")
						|| JFramePrincipal.terreno.getCell(x, y).equals("f")) && !aux.contains(ptWalker)){
						aux.add(ptWalker);
					}
				}
			}
		}
		
		ArrayList<Point2D> rWay = new ArrayList<Point2D>();
		for(Point2D pt : realWay.getTrajectory()){
			if(!rWay.contains(pt)) rWay.add(pt);
		}
		
		System.out.println("N�mero de passos do walker: "+ rWay.size());
		System.out.println("N�mero de passos do walker adivinhados pelas antenas: "+ aux.size());
		
		if(realWay.getTrajectory().size() != 0) percentage = (double) (aux.size())/(rWay.size()); 
		
		System.out.printf("Pecentagem de caminho detetado %.2f\n",(percentage*100));
		DecimalFormat twoDigits = new DecimalFormat("0.00");    
		JOptionPane.showMessageDialog(null, "Detected path percentage: " + twoDigits.format(percentage*100) + "%");
	}
	
	public void supposedWay(int x1, int y1, int x2, int y2, ArrayList<Point2D> aux){
		int x = x1, y = y1, D = 0, HX, HY, c, M, xInc = 1, yInc = 1;
				
		HX = x2 - x1;
		HY = y2 - y1;
		if (HX < 0){xInc = -1; HX = -HX;}
		if (HY < 0){yInc = -1; HY = -HY;}
		if (HY <= HX){  
		   	c = 2 * HX; 
		   	M = 2 * HY;
		   	while(true){
		   		Point2D pt = new Point2D(x,y);
		   		if(!aux.contains(pt))
		   			aux.add(pt);
		   		
		   		if (x == x2) 
		   			break;
		   		x += xInc; 
		   		D += M;
		   		if (D > HX){
		   			y += yInc; 
		   			D -= c;
		   		}
		   	}
		}
		else{  
		   	c = 2 * HY; 
		   	M = 2 * HX;
		   	while(true){
		   		Point2D pt = new Point2D(x,y);
		   		if(!aux.contains(pt))
		   			aux.add(pt);
		   		
		   		if (y == y2) 
		   			break;
		   		y += yInc; 
		   		D += M;
		   		if (D > HY){
		   			x += xInc; 
		   			D -= c;
		   		}
		   	}
		}
	}
	
	public void requestNewSensor(){
		
		Spot[] bestSpots = new Spot[maxSensors-2];
		bestSpots = pickSpots();
		
		for(int i = 0; i < maxSensors-2 && i+2 < numSensors; i++){
			Sensor s = new Sensor();
			int x = bestSpots[i].getSpot().getX();
			int y = bestSpots[i].getSpot().getY();
			if(x != 0 && y != 0){
				s.setPt(new Point2D(x, y));
				s.setRadius(Terrain.range);
				sensors.addSensor(s);
			}
		}

		ACLMessage cfp = new ACLMessage ( ACLMessage.INFORM) ;
		cfp.setOntology(TrajectoryOntology.getInstance().getName());
		cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
		try {
			this.getContentManager().fillContent(cfp,sensors);
			cfp.addReceiver(new AID("SensorCommunication",AID.ISLOCALNAME));
			this.send(cfp); // envia a mensagem
		} catch (ContentException ce) {ce.printStackTrace();}
	}
	
	public Spot[] pickSpots(){ //variavel para verificar se a area tem parede (optar por spots k nao tem parede)
		Spot[] result = new Spot[maxSensors-2];
		
		for (int i = 0; i < maxSensors-2; i++)
			result[i] = new Spot();
		
		int range = Terrain.range;
		
		for (int i = range; i < JFramePrincipal.terreno.lengthH()-range; i++){
			for (int j = range; j < JFramePrincipal.terreno.lengthW()-range; j++){
				
				boolean primeiros = false;
				primeiros = compareSensors(new Point2D(i,j));
				
				if(!primeiros){
					int count = 0;
					int countParede = 0;
					boolean dentro = false;
					boolean segundo = false;
					int sensor = 0;
					
					if(!JFramePrincipal.terreno.getCell(i, j).equals("p")){
			
						for (int x = i-range; x <= i+range; x++){
							for (int y = j-range; y <= j+range; y++){
						
								if(JFramePrincipal.terreno.getCell(x, y).equals("w") || JFramePrincipal.terreno.getCell(x, y).equals("c")) 
									count++;
								if(JFramePrincipal.terreno.getCell(x, y).equals("p")) countParede++;										
							}
						}
					}

					for(int z = 0; z < maxSensors-2; z++){
						if(((Math.abs(result[z].getSpot().getX()-i) < (range*2)+1) && 
							(Math.abs(result[z].getSpot().getY()-j) < (range*2)+1)) && result[z].getSpot().getX() != 0){
							if(dentro){ 
								segundo = true;
								break;
							}
							dentro = true;
							sensor = z;						
						}
					}
					
					if(dentro && !segundo){
						if(result[sensor].getPerc() < count || (result[sensor].getPerc() == count && 
						   result[sensor].getParede() > countParede)){
							
							result[sensor].setParede(countParede);
							result[sensor].setPerc(count);
							result[sensor].setSpot(new Point2D(i,j));
							sortSpot(0, result.length-1, result);						
						}
					}
					else if(!segundo){
						if(result[maxSensors-3].getPerc() < count || (result[maxSensors-3].getPerc() == count && 
						   result[maxSensors-3].getParede() > countParede)){
							result[maxSensors-3].setParede(countParede);
							result[maxSensors-3].setPerc(count);
							result[maxSensors-3].setSpot(new Point2D(i,j));
							sortSpot(0, result.length-1, result);
						}
					}
				}
			}
		}
		return result;
	}
	
	public void sortSpot(int first, int last, Spot[] result){
		int i = first;
		int j = last;
		float mid = result[first + (last-first)/2].getPerc();
		
		while(i <= j){
			while(result[i].getPerc() > mid) i++;
			
			while(result[j].getPerc() < mid) j--;
			
			if(i <= j){
				swap(i, j, result);
				i++;
				j--;
			}
		}
		if (first < j)
			sortSpot(first, j, result);
		if (i < last)
			sortSpot(i, last, result);
	}
	
	public void swap(int i, int j, Spot[] result){
		Spot temp = result[i];
		result[i] = result[j];
		result[j] = temp;
	}
	
	// Comparar se a nova posi��o gerada j� est� a ser ocupada por alguma antena
	public boolean compareSensors(Point2D pt){
		int xS, yS;
		int x = pt.getX();
		int y = pt.getY();
		int i = 0;
		int rS = Terrain.range;
		
		for (Sensor s : sensors.getNewsSensors()){
			if (i >= 2) 
				break;
			Point2D ptS = s.getPt();
			xS = ptS.getX();
			yS = ptS.getY();
			
			if((Math.abs(xS-x) < (rS*2)+1) && (Math.abs(yS-y) < (rS*2)+1)) {		
				return true;
			}
			/*if(inArea(x-rS, y-rS, xS, yS, rS) || inArea(x-rS, y+rS, xS, yS, rS)
			   || inArea(x+rS, y-rS, xS, yS, rS) || inArea(x+rS, y+rS, xS, yS, rS)) 
				return true;*/
			i++;
		}	
		return false;
	}
	
	public boolean inArea(int x,int y,int a,int b, int rd){
		
		if ((x <= a+rd && x >= a-rd) && (y <= b+rd && y >= b-rd)) return true;
		else return false;
	}
	
	public void requestSupposedWay(){
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("supposedWay");
		msg.addReceiver(new AID("SensorCommunication",AID.ISLOCALNAME));
		this.send(msg);
	}

	@Override
	public String toString() {
		return "Supervisor [realWay = " + realWay + ", sensorsWay = " + sensorsWay + "]";
	}
	
	protected void takeDown(){
		System.out.println("Supervisor suicida-se!");
	}
	
}