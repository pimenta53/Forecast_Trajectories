package terrain;

import interaction.JFramePrincipal;
import ontology.Point2D;
import ontology.Position;
import ontology.Sensor;
import ontology.SensorSet;
import ontology.SupposedWay;
import ontology.TrajectoryOntology;
import ontology.Way;

import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.util.*;


public class Sensor_Communication extends Agent {
	
	private ArrayList<SensorInfo> records = new ArrayList<SensorInfo>(); //historico dos sensores que o walker passou
	private ArrayList<SensorInfo> sensors = new ArrayList<SensorInfo>(); //sensores colocados
	private TreeSet<String> cache = new TreeSet<String>(); //sensores ativados
	public int totalSensors = 0; //sensores colocados
	public int inputSensors = 2; //numero inicial de sensores posicionados
	public int maxSensors = 100; //numero maximo de sensores que podem ser criados
	
	
	protected void setup(){
			
		Object[] args = getArguments();
		if(args != null){
			inputSensors = Integer.parseInt(args[0].toString())-1;
			maxSensors = Integer.parseInt(args[1].toString());
		}
		
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(TrajectoryOntology.getInstance());
		updateSensors();
		scan();			
	}		

	protected void scan(){
		
		addBehaviour(new CyclicBehaviour(this) {
			
			@Override
			public void action() {
				
				ACLMessage msg = receive();
				if( msg!=null ){ 
					
					if(msg.getSender().getLocalName().equals("walker")){	
						try{		
							Position pt = (Position) myAgent.getContentManager().extractContent(msg);
							chooseSensor(pt);
						}catch(Exception e) {e.printStackTrace();}
					}
					if(msg.getSender().getLocalName().equals("SuperVisor")){
						if(msg.getContent().equals("supposedWay")){	
							sendSWay(traceWay());
							records = new ArrayList<SensorInfo>(); //com esta linha apaga-se o supposed way para fazer next
						}
						else{
							try {
								SensorSet newSensors = (SensorSet) myAgent.getContentManager().extractContent(msg);
								inputSensors++;
								newSensors(newSensors); 
							} catch (UngroundedException e) {e.printStackTrace();} 
							  catch (CodecException e) {e.printStackTrace();} 
							  catch (OntologyException e) {e.printStackTrace();}
							}
						}
					}
				else block();
			}
		});	
	}
	
	//inicia e desenha os sensores que sao passados pelo supervisor
	public void newSensors(SensorSet ssNew){
		
		for (SensorInfo si : sensors){
			String aux = new String(si.getSensor());
			try {
				AgentController ac = getContainerController().getAgent(aux);
				ac.kill();
			} catch(ControllerException e) {}
		}
		sensors.clear();
		totalSensors = 0;
		JFramePrincipal.sensores.clear();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {e1.printStackTrace();}
		
		for(Sensor s : ssNew.getNewsSensors()){
			int x, y, r;
			Object[] args = new Object[3];
			x = s.getPt().getX();
			y = s.getPt().getY();
			r = s.getRadius();
			args[0] = x;
	        args[1] = y;
	        args[2] = r;
			
			String name = "antena"+totalSensors+"-"+x+"-"+y+"-"+r;
	        JFramePrincipal.sensores.add(s.getPt());
			
	        AgentContainer c = getContainerController();
	        try {
		        AgentController a = c.createNewAgent(name, "terrain.SensorAgent", args);
	            sensors.add(new SensorInfo(name, s.getPt().clone(), r));
	            totalSensors++;
	            a.start();
	        } catch(Exception e) {e.printStackTrace();totalSensors++;}
		}
	}
	
	public SupposedWay traceWay(){
		
		SupposedWay sWay = new SupposedWay();
		Way temp = new Way();
		int i = 0;
		
		for(SensorInfo si: records){
			if(temp.getTrajectory().size() == 0) temp.move(si.getPt());
			if(!si.getPt().equals(temp.getTrajectory().get(i))){
				temp.move(si.getPt());
				i++;
			}
		}
		sWay.setSupposedlWay(temp);
		return sWay;
	}
	
	public void sendSWay(SupposedWay w) {
		
		try{	
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology(TrajectoryOntology.getInstance().getName());
			msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			this.getContentManager().fillContent(msg,w);// calcular o caminho
			msg.addReceiver(new AID("SuperVisor",AID.ISLOCALNAME));
			this.send(msg);
		} catch(Exception e) {e.printStackTrace();}
	}
	
	public void chooseSensor(Position pt){
		
		int a = pt.getPos().getX();
		int b = pt.getPos().getY();
		int x, y;
		float rd;
		boolean send = false;
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("active");
		
		for(SensorInfo sf : sensors){
			x = sf.getPt().getX();
			y = sf.getPt().getY();
			rd = sf.getRadius();
			
			if(Terrain.inArea(x,y,a,b,rd)) {
				if(!cache.contains(sf.getSensor())){
					records.add(sf);
					cache.add(sf.getSensor());
					msg.addReceiver(new AID(sf.getSensor(), AID.ISLOCALNAME));
					send = true;
				}
			}
			else if(cache.contains(sf.getSensor())) cacheRefresh(sf.getSensor());
			
			if(send) this.send(msg);
		}
	}
	
	public void cacheRefresh(String agent){
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("disable");
		msg.addReceiver(new AID(agent, AID.ISLOCALNAME));
		this.send(msg);
		cache.remove(agent);
	}

	public SensorInfo parseSensor(String name){
		
		Scanner lineScan = new Scanner(name);
		
		lineScan.useDelimiter("-");
		String lixo= lineScan.next();
		int x = Integer.parseInt(lineScan.next());
		int y = Integer.parseInt(lineScan.next());
		int rd = Integer.parseInt(lineScan.next());
		
		SensorInfo sf = new SensorInfo(name, new Point2D(x,y), rd);
		return sf;
	}
	
	protected void updateSensors() {
		
		// Update the list of sensor agents every minute
		addBehaviour(new TickerBehaviour(this, 600) {
			
			protected void onTick() {
				
				// Update the list of sensor agents
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("Sensoring");
				template.addServices(sd);
				
				try {
					DFAgentDescription[] result = DFService.search(myAgent,template);
					ArrayList<SensorInfo> sf = new ArrayList<SensorInfo>();
					for (int i = 0; i < result.length; i++)
						sf.add(parseSensor(result[i].getName().getLocalName()));
					sensors.clear();
					sensors.addAll(sf);
				} catch(FIPAException fe) {fe.printStackTrace();}
			}
		});
	}
	
	// desligar todas os sensores de movimento
	protected void takeDown(){
		for (SensorInfo si : sensors){
			String aux = new String(si.getSensor());
			try {
				AgentController ac = getContainerController().getAgent(aux);
				ac.kill();
			} catch(ControllerException e) {}
		}
	}
	
}