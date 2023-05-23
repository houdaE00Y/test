package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareLocationReceiver extends SimpleBehaviour {
	private static final long serialVersionUID = 1L;
	
	private List<String> senders;
	private Map<String, String> locations;
	MessageTemplate msgTemplate = null;

	ShareLocationReceiver(AbstractDedaleAgent agent, List<String> senders, Map<String, String> locations) {
		super(agent);
		this.locations = locations;
		MessageTemplate filter = MessageTemplate.and(
	            MessageTemplate.MatchProtocol("SHARE-CURRLOC"),
	            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				MessageTemplate.MatchAll();
		
		for(String agentName : senders) {
			if (this.msgTemplate == null) {
				this.msgTemplate = MessageTemplate.and(MessageTemplate.MatchSender(new AID(agentName, AID.ISLOCALNAME)), filter);
			} else {
				this.msgTemplate = MessageTemplate.or(this.msgTemplate, 
					MessageTemplate.and(MessageTemplate.MatchSender(new AID(agentName, AID.ISLOCALNAME)), filter));
			}
		}
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(msgTemplate);
		if (msg != null) {
			try {
				locations.put(msg.getSender().getLocalName(), (String) msg.getContentObject());
			} catch (UnreadableException e) {
				e.printStackTrace();
			}			
		}
	}

	@Override
	public boolean done() {
		return false;
	}
}
