package routes;

import interaction.JFramePrincipal;

import java.util.ArrayList;

import jade.content.ContentException;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import ontology.*;
import terrain.Terrain;


public class WalkBehaviour extends TickerBehaviour{
	
	private WalkerAgent walker;
	private long sp;
	private Point2D pt; // inicio do caminho
	private boolean end=false; 
	
	
	public WalkBehaviour(Agent a,long period){
		super(a, period); 
		walker = (WalkerAgent) a;
		pt = Terrain.getStart().clone();
		sp = period;
		walker.getAgentWay().move(pt.clone());
	}
	
	
	protected void onTick(){
		JFramePrincipal.end = false;
		int stop=walker.getMax();
		
		// se atinguio o maximo de passos permitidos ou atinguio o fim antes disso
		if (getTickCount()>stop || end){
			
			//walker.getAgentWay().move(new Point2D(0,0));// mudar para fim
			RealWay rWay = new RealWay();
			rWay.setRealWay(walker.getAgentWay());
			
			ACLMessage cfp = new ACLMessage ( ACLMessage.INFORM) ;
			cfp.setOntology(TrajectoryOntology.getInstance().getName());
			cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			try {
				myAgent.getContentManager().fillContent(cfp,rWay);
				cfp.addReceiver(new AID("SuperVisor",AID.ISLOCALNAME));
				myAgent.send(cfp); // envia a mensagem
			} catch (ContentException ce) {ce.printStackTrace();}
			JFramePrincipal.end = true;
			stop();
		}
		else{
			Point2D oldPoint = new Point2D(pt);
			// verifica se está no fim (especie de buraco negro)
			if(Terrain.inArea(pt.getX(), pt.getY(), Terrain.getEnd().getX(), Terrain.getEnd().getY(), 15) || getTickCount()-1>=walker.getMax()) {
				if(!pt.impossiBro( Terrain.getEnd().getX(),Terrain.getEnd().getY())){
					pt.setX(Terrain.getEnd().getX());
					pt.setY(Terrain.getEnd().getY());
				}	
			}
			else {
				pt.moveRnd();
			}
				if(!pt.equals(oldPoint)){ // verifica se foi realemte um passo ou ficou na mesma posição
					//walker.setMax(walker.getMax()+1);
					//System.out.print(pt.toString());
					
					ArrayList<Point2D> trajectory = walker.getAgentWay().getTrajectory();
					int pos = trajectory.size();
					if(!walker.getAgentWay().getTrajectory().isEmpty()){
						int x1 = trajectory.get(pos-1).getX();
						int y1 = trajectory.get(pos-1).getY();
						
						int x2 = pt.getX();
						int y2 = pt.getY();
						
						drawAgent(x1, y1, x2, y2, sp);
					}
					
					// caminho no agente
					walker.getAgentWay().move(pt.clone());
					
					Position ppp = new Position();
					ppp.setPos(pt);
					ppp.setTime(System.nanoTime());
						
					ACLMessage cfp = new ACLMessage (ACLMessage.INFORM);
					cfp.setOntology(TrajectoryOntology.getInstance().getName());
					cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
					cfp.setContent("move");
					try {
						myAgent.getContentManager().fillContent(cfp,ppp);
						cfp.addReceiver(new AID("SensorCommunication",AID.ISLOCALNAME));
						myAgent.send(cfp); // envia a mensagem
					} catch (ContentException ce) {ce.printStackTrace();}
				}
				else stop++;
			
			
			
		}
	}
	
	public void drawAgent(int x1, int y1, int x2, int y2, long sp){
		int x = x1, y = y1, D = 0, HX = x2 - x1, HY = y2 - y1, c, M, xInc = 1, yInc = 1;
		
		if (HX < 0){xInc = -1; HX = -HX;}
	    if (HY < 0){yInc = -1; HY = -HY;}
	    if (HY <= HX){  
	    	c = 2 * HX;
	    	M = 2 * HY;
	    	
	    	while(true){
	    		if(!JFramePrincipal.terreno.getCell(x,y).equals("s") && !JFramePrincipal.terreno.getCell(x,y).equals("p")
	    		   &&!JFramePrincipal.terreno.getCell(x,y).equals("i") && !JFramePrincipal.terreno.getCell(x,y).equals("f")) 
	    			JFramePrincipal.terreno.setCell(x, y,"w");

	    		Point2D p = new Point2D(x,y);
	    		walker.getAgentWay().move(p.clone());
	    		
	    		Position ppp = new Position();
	    		ppp.setPos(p);
	    		ppp.setTime(System.nanoTime());
	    			
	    		ACLMessage cfp = new ACLMessage (ACLMessage.INFORM);
	    		cfp.setOntology(TrajectoryOntology.getInstance().getName());
	    		cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
	    		cfp.setContent("move");
	    		try {
	    			myAgent.getContentManager().fillContent(cfp,ppp);
	    			cfp.addReceiver(new AID("SensorCommunication",AID.ISLOCALNAME));
	    			myAgent.send(cfp); // envia a mensagem
	    		} catch (ContentException ce) {ce.printStackTrace();}
	    		
	    		if (x == x2) 
	    			break;
	    		x += xInc; 
	    		D += M;
	    		if (D > HX){
	    			y += yInc; 
	    			D -= c;
	    		}
	    		try {
	    			Thread.sleep(sp);
	    		} catch (InterruptedException e) {e.printStackTrace();}
	     	}
	    }
	    else{  
	    	c = 2 * HY; 
	    	M = 2 * HX;
	    	
	    	while(true){
	    		if(!JFramePrincipal.terreno.getCell(x,y).equals("s") && !JFramePrincipal.terreno.getCell(x,y).equals("p")
	 	    	   &&!JFramePrincipal.terreno.getCell(x,y).equals("i") && !JFramePrincipal.terreno.getCell(x,y).equals("f")) 
	    			JFramePrincipal.terreno.setCell(x, y,"w");
	    		
	    		Point2D p = new Point2D(x,y);		    		
	    		walker.getAgentWay().move(p.clone());
	    		
	    		Position ppp = new Position();
	    		ppp.setPos(p);
	    		ppp.setTime(System.nanoTime());
	    			
	    		ACLMessage cfp = new ACLMessage (ACLMessage.INFORM);
	    		cfp.setOntology(TrajectoryOntology.getInstance().getName());
	    		cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
	    		cfp.setContent("move");
	    		try {
	    			myAgent.getContentManager().fillContent(cfp,ppp);
	    			cfp.addReceiver(new AID("SensorCommunication",AID.ISLOCALNAME));
	    			myAgent.send(cfp); // envia a mensagem
	    		} catch (ContentException ce) {ce.printStackTrace();}
	    		  		
	    		if (y == y2) 
	    			break;
	    		y += yInc; 
	    		D += M;
	    		if (D > HY){
	    			x += xInc; 
	    			D -= c;
	    		}
    			try {
    				Thread.sleep(sp);
    			} catch (InterruptedException e) {e.printStackTrace();}
	    	}
	    }
	}
	
}
