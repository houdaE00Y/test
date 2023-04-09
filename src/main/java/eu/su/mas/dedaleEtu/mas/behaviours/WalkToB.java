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

/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 *
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.
 * This (non optimal) behaviour is done until all nodes are explored.
 *
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 *
 * @author hc
 */
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
        WalkToDestinationAvoidance walkToDestination = new WalkToDestinationAvoidance(myAgent, myMap);
        this.registerFirstState(new OneShotBehaviour(myAgent) {
			@Override
			public void action() {
				originalLoc = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
			}}, "InitVariables");

        OrderCheckingBehaviour findingBehaivours = new OrderCheckingBehaviour(myAgent);
        findingBehaivours.addBehaviour(new ShareLocationReceiver(myAgent, agentNames, locationOtherAgents));
        findingBehaivours.addBehaviour(new CheckingBehaviour(myAgent) {
        	public boolean done() {
	        	for (String agent : agentNames) {
	        		String loc = locationOtherAgents.get(agent);
	        		if (loc != null) {
	        			goalLoc = loc;
	        			walkToDestination.setObjective(goalLoc);
	        			return true;
	        		}
	        	}
	        	return false;
        }});
        findingBehaivours.addBehaviour(new MyExploBehaviour(myAgent, myMap));
        this.registerState(findingBehaivours, "Finding");
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
        this.registerLastState(walkToDestination, "Found");
        this.registerTransition("InitVariables", "Finding", 0);
        this.registerTransition("Finding", "Sharing", 0);
        this.registerTransition("Sharing", "Found", 0);

        this.myMap  = myMap;
        this.agentNames = agentNames;
    }
}