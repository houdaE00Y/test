package eu.su.mas.dedaleEtu.mas.planBody;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bdi4jade.annotation.Parameter;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;

public class RequestMovePlanBody extends BeliefGoalPlanBody {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void execute() {
    	String situatedAgent = (String) getBeliefBase().getBelief(Constants.SITUATED_AGENT).getValue();
    	MapaModel model = (MapaModel) getBeliefBase().getBelief(Constants.ONTOLOGY).getValue();
    	MapRepresentationPolidama map = (MapRepresentationPolidama) getBeliefBase().getBelief(Constants.MAP_REPRESENTATION).getValue();
    	String currentLocation = model.getAgentLocation(situatedAgent);
    	
    	String nextNode = null;
    	if (map.hasOpenNode()) {
    		List<String> route = map.getShortestPathToClosestOpenNode(currentLocation, model);
    		if (route != null && !route.isEmpty()) {
    			nextNode = route.get(0);
    		}
    	}
    	else if (model.hasClosedNodes()) {
    		List<String> route = map.getRouteThroughLeastVisitedNodes(currentLocation, model);
    		if (route != null && !route.isEmpty()) {
    			nextNode = route.get(0);
    		}
    	}
    	if (nextNode == null) {
    		return; // Can't do anything! maybe we haven't seen any nodes yet;
    	}

    	model.addObectiveLocation(situatedAgent, nextNode);
    	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    	msg.addReceiver(new AID((String) situatedAgent, AID.ISLOCALNAME));
		msg.setProtocol("Order");
        msg.setSender(this.myAgent.getAID());
		// We want to receive a reply in 1 sec
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
		msg.setContent(model.getOntology());
		getAgent().send(msg);
		System.out.println("Porposing to move to: " + nextNode);
		ACLMessage response = getAgent().blockingReceive(MessageTemplate.and(MessageTemplate.MatchProtocol("Order"), MessageTemplate.MatchSender(new AID(situatedAgent, AID.ISLOCALNAME))), 1000);
		if (response == null) { // timeout
			 // A timeout should not happen; In our design, the situated agent will try once, and if it fails, it sends a failure. This is an unexpected state.
			System.out.println("TIMEOUT FIRST MESSAGE");
			//setEndState(Plan.EndState.FAILED);
			return;
		}
		if (response.getPerformative() != ACLMessage.AGREE) {
			if (response.getPerformative() == ACLMessage.REFUSE)
				System.out.println("The situated agent refused!");
			else
				System.out.println("The situated agent could not understand the ontology!");
			return; // Try again; The situated agent can refuse normally
		}
		ACLMessage resultMessage = getAgent().blockingReceive(MessageTemplate.and(MessageTemplate.MatchProtocol("Order"), MessageTemplate.MatchSender(new AID(situatedAgent, AID.ISLOCALNAME))), 1000);
		if (resultMessage == null) {
			 // A timeout should not happen; In our design, the situated agent will try once, and if it fails, it sends a failure. This is an unexpected state.
			System.out.println("TIMEOUT SECOND MESSAGE");
			setEndState(Plan.EndState.FAILED);
			return;
		}
		if (resultMessage.getPerformative() == ACLMessage.INFORM) {
			map.markAsVisitedOnce(nextNode);
			return;
		} else {
			System.out.println("Situated agent failed with message: " + resultMessage);
		}
    }
}
