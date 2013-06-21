package routes;

import interaction.JFramePrincipal;
import jade.core.Agent;
import ontology.*;
import jade.content.lang.sl.SLCodec;


public class WalkerAgent extends Agent {

	private Way AgentWay = new Way();
	private int max = 53;
	private int speed = 5;
	
	
	protected void setup(){
		
		
		JFramePrincipal.end = false;
		// registar ontologia
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(TrajectoryOntology.getInstance());
		
		// leitura dos argumentos
		Object[] args = getArguments();
		if(args != null){
			max = Integer.parseInt(args[0].toString());
			speed = Integer.parseInt(args[1].toString());
		}				
		
		//System.out.println("Walker "+getAID().getName());
		addBehaviour(new WalkBehaviour(this,speed));		
	}
	
	public Way getAgentWay() {
		return AgentWay;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setAgentWay(Way agentWay) {
		AgentWay = agentWay;
	}

	public void setMax(int max) {
		this.max = max;
	}
	
	public int getMax(){
		return max;
	}

	protected void takeDown(){
		System.out.println("Xau, ja estou cansado de andar a passear!");
	}
	
}
