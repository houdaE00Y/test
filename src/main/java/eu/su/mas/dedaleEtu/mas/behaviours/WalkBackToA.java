package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class WalkBackToA extends FSMBehaviour {
    private static final long serialVersionUID = 8567689731496787661L;

    /**
     * Current knowledge of the agent regarding the environment
     */
    private MapRepresentation myMap = null;

    private List<String> agentNames;
    
    private Map<String, String> locationOtherAgents = new HashMap<String, String>();
    private String originalLoc;
    private String goalLoc;
    
    /**
     * @param myAgent    the agent using this behaviour
     * @param agentNames name of the agents to share the map with
     */
    public WalkBackToA(final AbstractDedaleAgent myAgent, List<String> agentNames) {
        super(myAgent);
        this.agentNames = agentNames;
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