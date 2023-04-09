package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ShareLocation extends TickerBehaviour {
	private static final long serialVersionUID = 1L;
	
	private List<String> receivers;
	
	public ShareLocation(AbstractDedaleAgent agent, List<String> receivers) {
		super(agent, 10);
		this.receivers = receivers;
	}

	@Override
	public void onTick() {
		String location = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-CURRLOC");
        msg.setSender(this.myAgent.getAID());
        for (String agentName : receivers) {
            msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
        }

        try {
            msg.setContentObject(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
	}
}
