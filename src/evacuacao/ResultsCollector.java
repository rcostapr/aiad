package evacuacao;

import java.util.HashSet;

import evacuacao.onto.ServiceOntology;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public class ResultsCollector extends Agent {
	
	private int RESULTSCOLLECTOR_SLEEP = 200;
	private int counter = 0;

	/* Relevant metrics */
	private int nCtriticalInjuries = 0;
	private int nDead = 0;
	private int nPushes = 0;
	private int nHelpRequests = 0;
	private int nDirectionsRequests = 0;
	private long mediumEvacuationTime = 0;
	private long maximumEvacuationTime = 0;
	private long minimumEvacuationTime = Long.MAX_VALUE;
	
	private int nEvacuees;
	private int nEvacuated;

	private Codec codec;
	private Ontology serviceOntology;
	
	//private HashSet<EvacueeStats> evacuationResults = new HashSet<EvacueeStats>();
	
	public ResultsCollector() {
		this.nEvacuees = 0;
		this.nEvacuated = 0;
	}
	
	/**
	 * @return the nEvacuees
	 */
	public int getnEvacuees() {
		return nEvacuees;
	}

	/**
	 * @param nEvacuees the nEvacuees to set
	 */
	public void setnEvacuees(int nEvacuees) {
		this.nEvacuees = nEvacuees;
	}
	
	@Override
	public void setup() {
		
		// register language and ontology
		codec = new SLCodec();
		serviceOntology = ServiceOntology.getInstance();
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(serviceOntology);

		// results listener
		//addBehaviour(new ResultsListener());
	}
	
	/*private void calculateResults() {		
		for(EvacueeStats stat: evacuationResults){			
			nPushes += stat.getPushes();
			nDirectionsRequests += stat.getDirectionsRequests();
			nHelpRequests += stat.getHelpRequests();
			
			if(stat.getPhysicalCondition() < Person.DEATH_LEVEL){
				nDead++;
				continue;
			}
			
			mediumEvacuationTime += stat.getEvacuationTime();
			
			if(stat.getEvacuationTime() < minimumEvacuationTime){
				minimumEvacuationTime = stat.getEvacuationTime(); 
			}
			
			if(stat.getEvacuationTime() > maximumEvacuationTime){
				maximumEvacuationTime = stat.getEvacuationTime(); 
			}
			
			if(stat.getPhysicalCondition() < Person.DEATH_LEVEL * 2){
				nCtriticalInjuries++;
			}
		}
		
		if(!evacuationResults.isEmpty()) {
			mediumEvacuationTime /= (evacuationResults.size() - nDead); 
		}
	}*/
	
	/*private void printResults() {
		System.out.println("Evacuation statistics:");
		System.out.println(nEvacuated - nDead +(nEvacuated - nDead == 1 ? " was" : " were") +" evacuated in " + maximumEvacuationTime);
		System.out.println("Each person took an average of " + mediumEvacuationTime + " to reach an exit.");
		System.out.println("Some took only " + minimumEvacuationTime);
		System.out.println(nCtriticalInjuries + (nCtriticalInjuries == 1 ? " was" : " were") + " critically injuried. ");
		System.out.println(nDead + " died. ");
		System.out.println(nDirectionsRequests + " directions requests answered.");
		System.out.println(nPushes + " pushes occurred.");
		System.out.println(nHelpRequests + " help requests answered.");
		
		System.out.println();
		System.out.println("Detailed results:");
		for(EvacueeStats stats : evacuationResults){
			System.out.println(stats.getId() + ": Helped->" + stats.getHelpee() + "; evacTime->" + stats.getEvacuationTime() + "; Age->" + stats.getAge() + "; areaKnowledge->" + 
					stats.getAreaKnowledge() + "; altruism->" + stats.getAltruism() + "; independence->" + stats.getIndependence() +
					"; mobility->" + stats.getPhysicalCondition() + "; panic->" + stats.getPanic());
		}
	}*/

	
	/*private class ResultsListener extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		
		private MessageTemplate template = 
				MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchOntology(ServiceOntology.ONTOLOGY_NAME));

		@Override
		public void action() {
			
			// when evacuation is complete
			if(nEvacuated == nEvacuees) {
				if(++counter == RESULTSCOLLECTOR_SLEEP){
					// calculate and output results
					calculateResults();			
					printResults();
					
					// Allow for some time to pass before terminating the simulation
					// This prevents the graph in the Repast GUI from displaying incorrect final informations
					try {
						myAgent.getContainerController().getPlatformController().kill();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			} else {
			
				ACLMessage inform = myAgent.receive(template);
				boolean flag = false;
				
				if(inform != null) {
					EvacueeStats result = null;
					try {
						result = (EvacueeStats) getContentManager().extractContent(inform);
						flag = evacuationResults.add(result);
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
					
					if(flag){
						nEvacuated++;
					}
					
				} else {
					block();
				}
			}
		}
	}*/
}
