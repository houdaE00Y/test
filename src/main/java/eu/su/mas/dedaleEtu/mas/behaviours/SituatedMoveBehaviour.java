package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.SituatedAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel.MineralType;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel.NodeType;
import jade.core.behaviours.SimpleBehaviour;

public class SituatedMoveBehaviour extends SimpleBehaviour {
    private MapRepresentationPolidama myMap;
    private MapaModel model;
	private static final long serialVersionUID = 1L;
	
	public SituatedMoveBehaviour(final AbstractDedaleAgent myAgent, MapRepresentationPolidama myMap, MapaModel model) {
        super(myAgent);
        this.myMap = myMap;
        this.model = model;
	}
	
	@Override
	public boolean done() {
		return false;
	}
	
	@Override
	public void action() {
        //0) Retrieve the current position
        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId();
        if (myPosition == null) {
        	return;
        }
        
        //1) remove the current node from openlist and add it to closedNodes.
        this.myMap.addNode(myPosition, MapAttribute.closed);
        model.addNode(myPosition, NodeType.Closed);
        model.addAgentPos(myAgent.getLocalName(), myPosition);

        model.getAgentPositions();
        
        //List of observable from the agent's current position
        List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition
        //System.out.println(" " + lobs);
        //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
        for (Couple<Location, List<Couple<Observation, Integer>>> lob : lobs) {
        	if (!lob.getRight().isEmpty()) {
        		System.out.println(getAgent().getLocalName() + " Found: " + lob.getRight().toString());
        	}
            String nodeId = lob.getLeft().getLocationId();
            if (!myPosition.equals(nodeId)) {
	            this.myMap.addNewNode(nodeId);
	            model.addNode(nodeId, NodeType.Open);
	            model.addAdjancency(nodeId, myPosition);
            }
            boolean isWindNode=false;
            // Check wind
            for (Couple<Observation,Integer> c : lob.getRight()) 	
            {
            	if (c.getLeft() == Observation.WIND) {
            		isWindNode = true;
                    model.addNodeWindy(nodeId);
            	}
            	else if (c.getLeft() == Observation.GOLD) {
                	model.addMineral(c.getRight().toString(), MineralType.Gold);
                	model.addMineralPos(c.getRight().toString(), nodeId);
            	}
            	else if (c.getLeft() == Observation.DIAMOND) {
                	model.addMineral(c.getRight().toString(), MineralType.Diamond);            		
                	model.addMineralPos(c.getRight().toString(), nodeId);
            	}
        	};
            //the node may exist, but not necessarily the edge

            // Si es un nodo viento no creamos arista
            // si spawn es en un nodo viento caca!
            if (isWindNode) {
                this.myMap.addNode(nodeId, MapAttribute.closed);
            }
            else if (!myPosition.equals(nodeId)) {
                this.myMap.addEdge(myPosition, nodeId);
            }
        }
        
        String objective = this.model.getObjectiveLocation(myAgent.getLocalName());
        if (objective == null) return;
        //System.out.println("Moving to: " + objective);
    	if (((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(objective))) {
    		((SituatedAgent) this.myAgent).successOrder = true;
    	}
    }
}
