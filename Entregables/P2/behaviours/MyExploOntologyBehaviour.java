package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.Model;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.MapaModel.MineralType;
import eu.su.mas.dedaleEtu.mas.behaviours.MapaModel.NodeType;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.SimpleBehaviour;

public class MyExploOntologyBehaviour extends SimpleBehaviour {
    private MapRepresentation myMap;
    private MapaModel model;
	private static final long serialVersionUID = 1L;

	private boolean allExplored = false;
	private boolean isDone = false;
	
	public MyExploOntologyBehaviour(final AbstractDedaleAgent myAgent, MapRepresentation myMap, MapaModel model) {
        super(myAgent);
        this.myMap = myMap;
        this.model = model;
	}
	
	@Override
	public boolean done() {
		return isDone;
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
        String nextNode = null;
        for (Couple<Location, List<Couple<Observation, Integer>>> lob : lobs) {
        	if (!lob.getRight().isEmpty()) {
        		System.out.println(getAgent().getLocalName() + " Found: " + lob.getRight().toString());
        	}
            String nodeId = lob.getLeft().getLocationId();
            this.myMap.addNewNode(nodeId);
            model.addNode(nodeId, NodeType.Open);
            model.addAdjancency(nodeId, myPosition);
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
        
        //3) while openNodes is not empty, continues.
        if (!this.myMap.hasOpenNode()) {
        	//System.out.println(this.myAgent.getLocalName() + " - Exploration successfully done.");
        	//Explo finished
        	//this.stop();
        	if (allExplored == false) {
        		allExplored = true;
            	System.out.println("AGENT " + this.getAgent().getLocalName() + " IS DONE. IT KNOWS:");
        		System.out.println(model.getOntology());
        		System.out.println("");
        		System.out.println("AGENT " + this.getAgent().getLocalName() + " KNOWLEDGE END");
        	}
            nextNode = this.myMap.getRandomNode(myPosition, model);         
        } else {
            //no directly accessible openNode
            //chose one, compute the path and take the first step.
        	List<String> path = this.myMap.getShortestPathToClosestOpenNode(myPosition, model);
        	if (path != null && !path.isEmpty()) {
                nextNode = path.get(0);
        	} else {
        		nextNode = this.myMap.getRandomNode(myPosition, model);
        	}
        }
        if (nextNode != null) {
        	/*System.out.println(getAgent().getLocalName() + " beliefs that:");
        	for (Entry<String, String> ag :  model.getAgentPositions().entrySet()) {
            	System.out.println(ag.getKey() + " is at " + ag.getValue());
        	}
        	System.out.println(getAgent().getLocalName() + " goes " + nextNode);*/
        	((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNode));
        }
    }
}
