package evacuacao;

import evacuacao.onto.ServiceOntology;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;

public class ResultsCollector extends Agent {
	
	private int livesSaved = 0;

	private Codec codec;
	private Ontology serviceOntology;
	
	public ResultsCollector() {
	
	}
	
	@Override
	public void setup() {
		
		// register language and ontology
		codec = new SLCodec();
		serviceOntology = ServiceOntology.getInstance();
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(serviceOntology);

		// results listener
		addBehaviour(new ResultsListener());
	}
	
	private class ResultsListener extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		
		private MessageTemplate template = 
				MessageTemplate.and(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchOntology(ServiceOntology.ONTOLOGY_NAME));

		@Override
		public void action() {
			
			// when evacuation is complete
		}
			
	}
}
