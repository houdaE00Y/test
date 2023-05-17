package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;


public class PerformBDIOrders extends SimpleAchieveREResponder {
	private static final long serialVersionUID = 1L;
	
	OrderCheckingBehaviour orderExecutors;
	
	MapaModel model;
	MapRepresentationPolidama map;


	public PerformBDIOrders(final AbstractDedaleAgent myAgent, OrderCheckingBehaviour orderExecutors, MapRepresentationPolidama map, MapaModel model, String agent) {
		super(myAgent, MessageTemplate.and(MessageTemplate.MatchProtocol("Order"), MessageTemplate.MatchSender(new AID(agent, AID.ISLOCALNAME))));
		this.orderExecutors = orderExecutors;
		this.model = model;
		this.map = map;
		System.out.println("BDI TO RECEIVE/SEND: " + agent);
	}
	
	@Override
	protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		SituatedAgent myAgent = ((SituatedAgent) getAgent());
    	try {
    		MapaModel otherModel = MapaModel.importOntology(request.getContent());
        	model.replaceModel(otherModel);
    	} catch (Exception e) {
    		throw new NotUnderstoodException("Something went wrong while importing the ontology");
    	}
    	if (map.getShortestPath(model.getAgentLocation(getAgent().getLocalName()), model.getObjectiveLocation(getAgent().getLocalName())) == null) {
    		System.out.println("Agent "+myAgent.getLocalName()+": Refuse");
			throw new RefuseException("check-failed");
    	} else {
    		System.out.println("Agent "+myAgent.getLocalName()+": Agree");
    		return request.createReply(ACLMessage.AGREE);
    	}
	}
	
	@Override
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		SituatedAgent myAgent = ((SituatedAgent) getAgent());
		myAgent.successOrder = false;
		orderExecutors.actionWrapper();
		if (myAgent.successOrder) {
			System.out.println("Agent "+myAgent.getLocalName()+": Action successfully performed");
			return request.createReply(ACLMessage.INFORM);
		}
		else {
			System.out.println("Agent "+myAgent.getLocalName()+": Action failed");
			throw new FailureException("Could not perform the requested action");
		}	
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
