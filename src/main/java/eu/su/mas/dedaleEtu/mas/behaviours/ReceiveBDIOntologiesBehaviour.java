package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveBDIOntologiesBehaviour extends SimpleBehaviour {

	String agent;
	MapaModel model;
	
	public ReceiveBDIOntologiesBehaviour(final AbstractDedaleAgent myAgent, MapaModel model, String agent) {
		this.agent = agent;
		this.model = model;
		System.out.println("AGENTS SEND::: " + agent);
	}
	
	@Override
	public void action() {
		MessageTemplate msgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("SHARE-ONTO"), MessageTemplate.MatchSender(new AID(agent, false)));
        while (true) {
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
	        if (msgReceived != null) {
	        	MapaModel otherModel = MapaModel.importOntology(msgReceived.getContent());
	        	model.replaceModel(otherModel);
	        	model.getObjectiveLocation(getAgent().getLocalName());
	        	getAgent().send(msgReceived.createReply(ACLMessage.AGREE));
            } else break;
        }
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
