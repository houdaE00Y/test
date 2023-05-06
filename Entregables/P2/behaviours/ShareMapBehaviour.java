package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.List;

/**
 * The agent periodically share its map.
 * It tries to send all its graph to its friend(s)
 *
 * @author hc
 */
public class ShareMapBehaviour extends OneShotBehaviour {
    private MapRepresentation myMap;
    private final List<String> receivers;

    /**
     * Send the map once
     * @param a         the agent
     * @param myMap     (the map to share)
     * @param receivers the list of agents to send the map to
     */
    public ShareMapBehaviour(Agent a, List<String> receivers, MapRepresentation myMap) {
        super(a);
        this.receivers = receivers;
    	this.myMap = myMap;
    }
    
    /**
     *
     */
    private static final long serialVersionUID = -568863390879327961L;

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-TOPO");
        msg.setSender(this.myAgent.getAID());
        for (String agentName : receivers) {
            msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
        }

        SerializableSimpleGraph<String, MapAttribute> sg = this.myMap.getSerializableGraph();
        try {
            msg.setContentObject(sg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
    }
}
