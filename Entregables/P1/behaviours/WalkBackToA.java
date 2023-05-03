package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class WalkBackToA extends FSMBehaviour {
    private static final long serialVersionUID = 8567689731496787661L;

    /**
     * Current knowledge of the agent regarding the environment
     */
    private MapRepresentation myMap = new MapRepresentation();
    private boolean mapReceived = false;
    private String goalLoc = null;

    private List<String> agentNames;
        
    /**
     * @param myAgent    the agent using this behaviour
     * @param agentNames name of the agents to share the map with
     */
    public WalkBackToA(final AbstractDedaleAgent myAgent, List<String> agentNames) {
        super(myAgent);
        System.out.println("Agent " + myAgent.getAID().getLocalName() + " becomes B");
        this.agentNames = agentNames;
        this.registerFirstState(new OneShotBehaviour(myAgent) {
			@Override
			public void action() {
				MessageTemplate filter = MessageTemplate.MatchAll();
				while (((AbstractDedaleAgent) this.myAgent).getCurQueueSize() > 0) {
	        		ACLMessage msg = myAgent.receive(filter);
				}
			}}, "InitVariables");

        
        WalkToDestinationAvoidance walkToDestination = new WalkToDestinationAvoidance(myAgent, myMap);
        OrderCheckingBehaviour findingBehaivours = new OrderCheckingBehaviour(myAgent);
        findingBehaivours.addBehaviour(new ShareLocation(myAgent, agentNames));
        findingBehaivours.addBehaviour(new SimpleBehaviour(myAgent) {
			@Override
			public boolean done() {
				return false;
			}
			
			@Override
			public void action() {
				MessageTemplate msgTemplate = MessageTemplate.and(
                        MessageTemplate.MatchProtocol("SHARE-TOPO"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
                if (msgReceived != null) {
                    try {
                        SerializableSimpleGraph<String, MapAttribute> sgreceived =
                                (SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();
                        mapReceived = true;
                        myMap.mergeMap(sgreceived);
                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
			}
		});
        findingBehaivours.addBehaviour(new SimpleBehaviour(myAgent) {
			@Override
			public boolean done() {
				return false;
			}
			
			@Override
			public void action() {
				MessageTemplate filter = MessageTemplate.and(
			            MessageTemplate.MatchProtocol("SHARE-ORIGINAL-LOC"),
			            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			
				MessageTemplate msgTemplate = null;
				for(String agentName : agentNames) {
					if (msgTemplate == null) {
						msgTemplate = MessageTemplate.and(MessageTemplate.MatchSender(new AID(agentName, AID.ISLOCALNAME)), filter);
					} else {
						msgTemplate = MessageTemplate.or(msgTemplate, 
							MessageTemplate.and(MessageTemplate.MatchSender(new AID(agentName, AID.ISLOCALNAME)), filter));
					}
				}
				ACLMessage msg = myAgent.receive(msgTemplate);
				if (msg != null) {
					try {
						goalLoc = (String) msg.getContentObject();
						walkToDestination.setObjective(goalLoc);
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
			}
		});
        findingBehaivours.addBehaviour(new CheckingBehaviour(myAgent) {
			@Override
			public boolean done() {
				//System.out.println("goalloc " + goalLoc + " " + mapReceived);
	        	return  goalLoc != null && mapReceived;
			}
		});
        this.registerState(findingBehaivours, "BroadcastRecv");

        this.registerState(new WakerBehaviour(myAgent, 1000) {}, "WaitForOtherToGetOut");
        
        OrderCheckingBehaviour walkToDestinationWhileScreaming = new OrderCheckingBehaviour(myAgent);
        walkToDestinationWhileScreaming.addBehaviour(walkToDestination);
        walkToDestinationWhileScreaming.addBehaviour(new SimpleBehaviour() {			
			@Override
			public boolean done() {
				return false;
			}
			
			@Override
			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		        msg.setProtocol("SCREAM-PASSING");
		        msg.setSender(this.myAgent.getAID());
		        for (String agentName : agentNames) {
		            msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		        }

	            msg.setContent("AHHHH!");
		        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
			}
		});
        this.registerState(walkToDestinationWhileScreaming, "Found");
        this.registerLastState(new OneShotBehaviour(myAgent) {
			@Override
			public void action() {
				myAgent.addBehaviour(new WalkToB((AbstractDedaleAgent)myAgent, new MapRepresentation(), agentNames));
			}
		}, "ChangeRole");
        
        this.registerTransition("InitVariables", "BroadcastRecv", 0);
        this.registerTransition("BroadcastRecv", "WaitForOtherToGetOut", 0);
        this.registerTransition("WaitForOtherToGetOut", "Found", 0);
        this.registerTransition("Found", "ChangeRole", 0);
    }
}