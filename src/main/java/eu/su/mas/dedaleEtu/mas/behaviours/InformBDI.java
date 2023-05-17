package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;


public class InformBDI extends SimpleBehaviour {
	private static final long serialVersionUID = 1L;
		
	String agent;

	MapaModel model;

	public InformBDI(final AbstractDedaleAgent myAgent, MapaModel model, String agent) {
		super(myAgent);
		this.model = model;
		this.agent = agent;
		System.out.println("BDI TO RECEIVE/SEND: " + agent);
	}
	
	@Override
	public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("Inform");
        msg.setSender(this.myAgent.getAID());
        msg.addReceiver(new AID(agent, AID.ISLOCALNAME));
        msg.setContent(model.getOntology());
        ((AbstractDedaleAgent) this.myAgent).send(msg);
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
