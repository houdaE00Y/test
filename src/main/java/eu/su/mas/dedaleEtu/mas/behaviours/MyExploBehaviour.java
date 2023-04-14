package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;

public class MyExploBehaviour extends SimpleBehaviour {
    private MapRepresentation myMap;
	private static final long serialVersionUID = 1L;
	
	private String ObjectiveLoc = null;
	private boolean isDone = false;
	
	protected long ciclesUntilUpdate = 20;
	protected long wakeupTime = 0;

	MyExploBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap) {
        super(myAgent);
        this.myMap = myMap;
	}
	
	@Override
	public boolean done() {
		return isDone;
	}
	
	@Override
	public void action() {
		if (ciclesUntilUpdate != 0) {
			long blockTime = wakeupTime - System.currentTimeMillis();
			if (blockTime <= 0) {
				wakeupTime = ciclesUntilUpdate + System.currentTimeMillis();
			} else {
				return;
			}
		}
		
		if (this.myMap  == null) {
            this.myMap  = new MapRepresentation();
            //this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent, 500, WalkToB.this.myMap , list_agentNames));
        }
        
        //0) Retrieve the current position
        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();

        //System.out.print(this.myAgent.getLocalName() + " POS: " + myPosition);
        if (myPosition != null) {
            // Just added here to let you see what the agent is doing, otherwise it will be too quick
            try {
                this.myAgent.doWait(100);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //1) remove the current node from openlist and add it to closedNodes.
            this.myMap .addNode(myPosition, MapAttribute.closed);

            //List of observable from the agent's current position
            List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition
            //System.out.println(" " + lobs);
            //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
            String nextNode = null;
            for (Couple<Location, List<Couple<Observation, Integer>>> lob : lobs) {
            	if (!lob.getRight().isEmpty()) {
            		System.out.println("A Found: " + lob.getRight().toString());
            	}
                String nodeId = lob.getLeft().getLocationId();
                boolean isNewNode = this.myMap.addNewNode(nodeId);
                boolean isWindNode=false;
                // Check wind
                for (Couple<Observation,Integer> c : lob.getRight()) 	
                {
                	if (c.getLeft() == Observation.WIND) {isWindNode = true;}
            	};
                //the node may exist, but not necessarily the edge

                // Si es un nodo viento no creamos arista
                // si spawn es en un nodo viento caca!
                if (isWindNode)
                    this.myMap .addNode(nodeId, MapAttribute.closed);
                else if (!myPosition.equals(nodeId)) {
                    this.myMap .addEdge(myPosition, nodeId);
                    if (nextNode == null && isNewNode && !isWindNode) nextNode = nodeId;
                }
            }
            
            //3) while openNodes is not empty, continues.
            if (!this.myMap.hasOpenNode()) {
                //Explo finished
            	//this.stop();
            	if (ObjectiveLoc != null) {
            		isDone = true;
            		return;
            	}
                System.out.println(this.myAgent.getLocalName() + " - Exploration successfully done. No agent found");
            } else {
                //4) select next move.
                //4.1 If there exist one open node directly reachable, go for it,
                //	 otherwise choose one from the openNode list, compute the shortestPath and go for it
                if (nextNode == null) {
                    //no directly accessible openNode
                    //chose one, compute the path and take the first step.
                    nextNode = this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);
                }
                if (nextNode.equals(ObjectiveLoc)) { // Oh no, we are trying to go through B! That's bad...
                	isDone = true;
                	return;
                }
                ((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNode));
            }
        }
	}
	
	void AddKnownObjective(String loc) {
		ObjectiveLoc = loc;
		this.myMap.addNode(loc, MapAttribute.closed);
		ciclesUntilUpdate = 0;
	}
}
