package terrain;

import java.util.Random;

import interaction.JFramePrincipal;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import ontology.*;


public class SensorAgent extends Agent {

	private boolean state = false;
	private Point2D pos = new Point2D(0,0);
	private int radius = Terrain.range;
	
	protected void setup (){

		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(TrajectoryOntology.getInstance());
				
		Object[] args = getArguments();
		if(args.length == 3){
			pos.setX(Integer.parseInt(args[0].toString()));
			pos.setY(Integer.parseInt(args[1].toString()));
			radius = Integer.parseInt(args[2].toString());
			//System.out.println("Antena: " + pos.toString());
		}
		else {
			Random rand = new Random();
			int x = rand.nextInt(80-(radius*2)) + radius;
			pos.setX(x);
			int y = rand.nextInt(80-(radius*2)) + radius;
			pos.setY(y);
		}
		
		// Register the sensor service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		sensorRecord(dfd);
		changeState();	
	}

	protected void sensorRecord (DFAgentDescription dfd){
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Sensoring");
		sd.setName(getLocalName()+" - Sensor");// atencao aqui ...
		
		sd.addProperties(new Property("radius",radius));
		sd.addProperties(new Property("Y", pos.getY()));
		sd.addProperties(new Property("X", pos.getY()));
		
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {fe.printStackTrace();}
	}
	
	protected void changeState(){
		
		addBehaviour(new CyclicBehaviour(this) {
		
			@Override
			public void action() {
				
				ACLMessage msg = receive();
				if(msg!=null){
					if (msg.getContent().equals("active")){ 
						changeS(true);
						//System.out.println(getAID().getName() + ": Estou a detectar qualquer coisa");
						JFramePrincipal.terreno.setSensor(pos.getX(), pos.getY(), radius, 1);
					}
					if (msg.getContent().equals("disable")){ 
						changeS(false);
						//System.out.println(getAID().getName() + ": Deixei de detectar alguma coisa");
						JFramePrincipal.terreno.setSensor(pos.getX(), pos.getY(), radius, 0);
					}
				}
				else block();
			}
			
		});
	}
	
	public void changeS(boolean st){
		this.setState(st);
	}
	
	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public Point2D getPos() {
		return pos;
	}

	public void setPos(Point2D pos) {
		this.pos = pos;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	protected void takeDown(){
		
		System.out.println("Antena a desligar-se!");
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {e.printStackTrace();}
	}
	
}
