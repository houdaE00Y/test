package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveOntologiesBehaviour extends SimpleBehaviour {

	Set<String> agents;
	MapaModel model;
	
	public ReceiveOntologiesBehaviour(final AbstractDedaleAgent myAgent, MapaModel model, Set<String> agents) {
		super(myAgent);
		this.agents = agents;
		this.model = model;
		System.out.println("AGENTS SEND::: " + agents);
	}
	
	void filter(MapaModel otherModel, String receiver) {
		Set<String> closedNodes = model.getClosedNodes();
		
		Set<String> agents = model.getAgentPositions().keySet();
		agents.remove(receiver);
		otherModel.removeAllAgentPositionsInSet(agents);
		otherModel.removeClosedNodes(closedNodes);
		
		
	}
	
	@Override
	public void action() {
		MessageTemplate msgTemplate = MessageTemplate.MatchProtocol("SHARE-ONTO");
        while (true) {
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
	        if (msgReceived != null) {
	        	MapaModel otherModel = MapaModel.importOntology(msgReceived.getContent());
	            filter(otherModel, msgReceived.getSender().getLocalName());
	            Map<String, String> agents = otherModel.getAgentPositions();
	            for (Map.Entry<String, String> entry : agents.entrySet()) {
	            	model.addAgent(entry.getKey(), otherModel.getAgentType(entry.getKey()));
	            	model.addAgentPos(entry.getKey(), entry.getValue());
	            	//System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
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
