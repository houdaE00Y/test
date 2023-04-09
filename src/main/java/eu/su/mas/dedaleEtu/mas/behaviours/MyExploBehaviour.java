package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.TickerBehaviour;

public class MyExploBehaviour extends TickerBehaviour {
    private MapRepresentation myMap;
	private static final long serialVersionUID = 1L;
	MyExploBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap) {
        super(myAgent, 20);
        this.myMap = myMap;
	}
	
	@Override
	protected void onTick() {
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
                ((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNode));
            }
        }
	}

}
