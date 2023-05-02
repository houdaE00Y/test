package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import com.github.andrewoma.dexx.collection.HashSet;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.OntologyAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveOntologiesBehaviour extends SimpleBehaviour {

	List<String> agents;
	MapaModel model;
	
	public ReceiveOntologiesBehaviour(final AbstractDedaleAgent myAgent, MapaModel model, List<String> agents) {
		this.agents = agents;
		this.model = model;
	}
	
	void filter(MapaModel otherModel, String receiver) {
		Set<String> agents = model.getAgentPositions().keySet();
		agents.remove(receiver);
		otherModel.removeAllAgentPositionsInSet(agents);
	}
	
	@Override
	public void action() {
		MessageTemplate msgTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-ONTO"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if (msgReceived != null) {
        	MapaModel model = new MapaModel(OntologyAgent.loadOntology());
            model.importOntology(msgReceived.getContent());
            filter(model, msgReceived.getSender().getLocalName());
            this.model.absorb(model);
        }

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
