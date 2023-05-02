package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Model;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class SendOntologiesBehaviour extends SimpleBehaviour {

	List<String> agents;
	MapRepresentation myMap;
	MapaModel model;
	
	public SendOntologiesBehaviour(final AbstractDedaleAgent myAgent, MapaModel model, List<String> agents) {
		this.agents = agents;
		this.model = model;
	}
	
	@Override
	public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-ONTO");
        msg.setSender(this.myAgent.getAID());
        for (String agentName : agents) {
            msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
        }
        msg.setContent(model.getOntology());
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
