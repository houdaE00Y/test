package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveBDIOntologiesBehaviour extends SimpleBehaviour {

	String agent;
	MapaModel model;
	MapRepresentation map;
	
	public ReceiveBDIOntologiesBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation map, MapaModel model, String agent) {
		this.agent = agent;
		this.model = model;
		this.map = map;
		System.out.println("AGENTS SEND::: " + agent);
	}
	
	@Override
	public void action() {
		MessageTemplate msgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("SHARE-ONTO"), MessageTemplate.MatchSender(new AID(agent, false)));
        while (true) {
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
	        if (msgReceived != null) {
	        	SituatedAgent myAgent = ((SituatedAgent) getAgent());
	        	myAgent.messageBDI = msgReceived;
	        	MapaModel otherModel = MapaModel.importOntology(msgReceived.getContent());
	        	model.replaceModel(otherModel);
	        	if (map.getShortestPath(model.getAgentLocation(getAgent().getLocalName()), model.getObjectiveLocation(getAgent().getLocalName())) == null) {
		        	getAgent().send(msgReceived.createReply(ACLMessage.REFUSE));	        		
	        	} else {
	        		getAgent().send(msgReceived.createReply(ACLMessage.AGREE));
	        	}
            } else break;
        }
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
