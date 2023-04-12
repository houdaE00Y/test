package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CompositeBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;

public class WalkToB extends FSMBehaviour {
    private static final long serialVersionUID = 8567689731496787661L;

    /**
     * Current knowledge of the agent regarding the environment
     */
    private MapRepresentation myMap;

    private List<String> agentNames;
    
    private Map<String, String> locationOtherAgents = new HashMap<String, String>();
    private String originalLoc;
    private String goalLoc;
    
    /**
     * @param myAgent    the agent using this behaviour
     * @param myMap      known map of the world the agent is living in
     * @param agentNames name of the agents to share the map with
     */
    public WalkToB(final AbstractDedaleAgent myAgent, MapRepresentation myMap, List<String> agentNames) {
        super(myAgent);
        System.out.println("Agent " + myAgent.getAID().getLocalName() + " becomes A");
        WalkToDestinationAvoidance walkToDestination = new WalkToDestinationAvoidance(myAgent, myMap);
        WalkToDestinationAvoidance walkToLateralOfDestination = new WalkToDestinationAvoidance(myAgent, myMap);
        WalkToDestinationAvoidance walkOutOfTheWay = new WalkToDestinationAvoidance(myAgent, myMap);
        
        this.registerFirstState(new OneShotBehaviour(myAgent) {
			@Override
			public void action() {
				originalLoc = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
				MessageTemplate filter = MessageTemplate.MatchAll();
				while (((AbstractDedaleAgent) this.myAgent).getCurQueueSize() > 0) {
	        		ACLMessage msg = myAgent.receive(filter);
				}
			}}, "InitVariables");

        MyExploBehaviour explorer = new MyExploBehaviour(myAgent, myMap);
        OrderCheckingBehaviour findingBehaivours = new OrderCheckingBehaviour(myAgent);
        findingBehaivours.addBehaviour(new ShareLocationReceiver(myAgent, agentNames, locationOtherAgents));
        findingBehaivours.addBehaviour(new SimpleBehaviour(myAgent) {
        	public boolean done() {return false;}
        	public void action() {
	        	for (String agent : agentNames) {
	        		String loc = locationOtherAgents.get(agent);
	        		if (loc != null) {
	        			goalLoc = loc;
	        			walkToDestination.setObjective(goalLoc);
	        			explorer.AddKnownObjective(goalLoc);
	        		}
	        	}
        }});
        findingBehaivours.addBehaviour(explorer);
        this.registerState(findingBehaivours, "Finding");
        
        // A might have walked away from B...
        OrderCheckingBehaviour makeSureToBeBack = new OrderCheckingBehaviour(myAgent);

        makeSureToBeBack.addBehaviour(new CheckingBehaviour(myAgent) {
			@Override
			public boolean done() {
				String currLoc = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
				for (Couple<Location, List<Couple<Observation, Integer>>> lob : ((AbstractDedaleAgent) this.myAgent)
						.observe()) {
					String nodeId = lob.getLeft().getLocationId();
					myMap.addNewNode(nodeId);
					boolean isWindNode = false;
					// Check wind
					for (Couple<Observation, Integer> c : lob.getRight()) {
						if (c.getLeft() == Observation.WIND) {
							isWindNode = true;
						}
					}
					
					// Si es un nodo viento no creamos arista
					// si spawn es en un nodo viento caca!
					if (isWindNode)
						myMap.addNode(nodeId, MapAttribute.closed);
					if (!currLoc.equals(nodeId))
						myMap.addEdge(currLoc, nodeId);
				}
				List<String> route = myMap.getShortestPath(currLoc, goalLoc);
				System.out.println("Curr. Pos: " + currLoc + " route: " + goalLoc);
				if (route.size() <= 1) {
					return true;
				}
				walkToLateralOfDestination.setObjective(route.get(route.size()-2));
				return false;
			}
		});
        makeSureToBeBack.addBehaviour(walkToLateralOfDestination);
        this.registerState(makeSureToBeBack, "GoBack");
        
        // A might have walked away from B...
        OrderCheckingBehaviour getOutOfTheWay = new OrderCheckingBehaviour(myAgent);
        getOutOfTheWay.addBehaviour(new CheckingBehaviour(myAgent) {
        	List<String> optimalOtherPath = null;
			@Override
			public boolean done() {
				String currLoc = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
    			if (optimalOtherPath == null) optimalOtherPath = myMap.getShortestPath(goalLoc, originalLoc);
    			
				for (Couple<Location, List<Couple<Observation, Integer>>> lob : ((AbstractDedaleAgent) this.myAgent)
						.observe()) {
					String nodeId = lob.getLeft().getLocationId();
					myMap.addNewNode(nodeId);
					boolean isWindNode = false;
					// Check wind
					for (Couple<Observation, Integer> c : lob.getRight()) {
						if (c.getLeft() == Observation.WIND) {
							isWindNode = true;
						}
					}
					
					// Si es un nodo viento no creamos arista
					// si spawn es en un nodo viento caca!
					if (isWindNode)
						myMap.addNode(nodeId, MapAttribute.closed);
					if (!currLoc.equals(nodeId))
						myMap.addEdge(currLoc, nodeId);
				}
    			
    			List<String> getPathOutOfTheWay = myMap.getOutOfPathByOne(optimalOtherPath, goalLoc, currLoc);
				if (getPathOutOfTheWay.isEmpty()) {
					return true;
				}
    			System.out.println("Other Will do: " + optimalOtherPath);
    			System.out.println("We'll get out with path: " + currLoc + " " + getPathOutOfTheWay);
    			walkOutOfTheWay.setObjective(getPathOutOfTheWay.get(getPathOutOfTheWay.size()-1));
				return false;
			}
		});
        getOutOfTheWay.addBehaviour(walkOutOfTheWay);
        this.registerState(getOutOfTheWay, "GetOutOfTheWay");
        
        this.registerState(new TickerBehaviour(myAgent, 10) {
			boolean first = true;
			@Override
			protected void onTick() {
				if (first) {first = false; return;}
				MessageTemplate filter = MessageTemplate.and(
			            MessageTemplate.MatchProtocol("SCREAM-PASSING"),
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
					this.stop();
				}
			}
		}, "WaitForOtherToPass");
        
        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour(new ShareMapBehaviour(myAgent, agentNames, myMap));
        seq.addSubBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		        msg.setProtocol("SHARE-ORIGINAL-LOC");
		        msg.setSender(this.myAgent.getAID());
		        for (String agentName : agentNames) {
		            msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		        }

		        try {
		            msg.setContentObject(originalLoc);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
			}
		});
        this.registerState(seq, "Sharing");
        this.registerState(walkToDestination, "Found");
        this.registerLastState(new OneShotBehaviour(myAgent) {
			
			@Override
			public void action() {
				myAgent.addBehaviour(new WalkBackToA((AbstractDedaleAgent)myAgent, agentNames));
			}
		}, "ChangeRole");

        this.registerTransition("InitVariables", "Finding", 0);
        this.registerTransition("Finding", "GoBack", 0);
        this.registerTransition("GoBack", "Sharing", 0);
        this.registerTransition("Sharing", "GetOutOfTheWay", 0);
        this.registerTransition("GetOutOfTheWay", "WaitForOtherToPass", 0);
        this.registerTransition("WaitForOtherToPass", "Found", 0);
        this.registerTransition("Found", "ChangeRole", 0);

        this.myMap  = myMap;
        this.agentNames = agentNames;
    }
}
