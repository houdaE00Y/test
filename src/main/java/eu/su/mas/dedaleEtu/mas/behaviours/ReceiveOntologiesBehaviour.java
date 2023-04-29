package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import org.apache.jena.rdf.model.Model;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
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
	
	@Override
	public void action() {

		MessageTemplate msgTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-ONTO"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if (msgReceived != null) {
            try {
                model.importOntology((String) msgReceived.getContentObject());
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
