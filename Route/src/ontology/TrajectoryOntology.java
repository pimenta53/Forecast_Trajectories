package ontology;

import jade.content.onto.BeanOntology;
import jade.content.onto.Ontology;


public class TrajectoryOntology extends BeanOntology {
	
		public static final String NAME = "Trajectory-Ontology";		
		// The singleton instance of this ontology
		private final static Ontology theInstance = new TrajectoryOntology();
		
		public final static Ontology getInstance() {
			return theInstance;
		}
		
		/**
		 * Constructor
		 */
		private TrajectoryOntology() {
		
			super(NAME);
			try {
				add(getClass().getPackage().getName());
			} catch (Exception e) {e.printStackTrace();}
		}
	
	}