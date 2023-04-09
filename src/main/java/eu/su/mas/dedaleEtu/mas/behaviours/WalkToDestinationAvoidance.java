package eu.su.mas.dedaleEtu.mas.behaviours;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class WalkToDestinationAvoidance extends FSMBehaviour {

	private static final long serialVersionUID = 1L;
	private MapRepresentation myMap;
	String objective;
	public void setObjective(String objective) {
		this.objective = objective;
    	System.out.println("OBJECTIVE IS NULL? " + (objective == null));
	}

	String prevPos = "";
	String currentPos = "";
    Random rand = new Random();
	
	class WalkObjective extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;
		boolean failWalk = false;
		int exitCode = 0;

		WalkObjective(AbstractDedaleAgent a) {super(a);}
		
		@Override
		public void action() {
			prevPos = currentPos;
			currentPos = ((AbstractDedaleAgent) myAgent).getCurrentPosition().getLocationId();

			if (currentPos.equals(objective)) {
				exitCode = 1;
				failWalk = true;
				return;
			} 

			if (prevPos.equals(currentPos)) {
				exitCode = 0;
				failWalk = true;
				return;
			}
	    	System.out.println("OBJECTIVE IS NULL? " + (objective == null) + " " + (currentPos == null));

            for (Couple<Location, List<Couple<Observation, Integer>>> lob : ((AbstractDedaleAgent) this.myAgent).observe()) {
                String nodeId = lob.getLeft().getLocationId();
                myMap.addNewNode(nodeId);
                boolean isWindNode=false;
                // Check wind
                for (Couple<Observation,Integer> c : lob.getRight()) 	
                {
                	if (c.getLeft() == Observation.WIND) {isWindNode = true;}
            	};
                // Si es un nodo viento no creamos arista
                // si spawn es en un nodo viento caca!
                if (isWindNode)
                    myMap.addNode(nodeId, MapAttribute.closed);
                else if (!currentPos.equals(nodeId)) {
                    myMap.addEdge(currentPos, nodeId);
                }
            }
	    	
	    	String direction = myMap.getShortestPath(currentPos, objective).get(0);
            ((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(direction));
		}

		@Override
		public boolean done() {
			if (failWalk) {
				failWalk = false;
				return true;
			}
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public int onEnd() {
			return exitCode;
		}
	}
	
	class Avoid extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;
		boolean failWalk = false;
		int exitCode = 0;
		Avoid(AbstractDedaleAgent a) {super(a);}
		
		@Override
		public void action() {
			prevPos = currentPos;
			currentPos = ((AbstractDedaleAgent) myAgent).getCurrentPosition().getLocationId();
			if (currentPos.equals(objective)) {
				exitCode = 1;
				failWalk = true;
				return;
			} 

			if (!prevPos.equals(currentPos)) {
				exitCode = 0;
				failWalk = true;
				return;
			}
			
			String direction = myMap.getShortestPath(currentPos, objective).get(0);
            
            List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition
            ArrayList<String> nextCandidates = new ArrayList<String>();
            for (Couple<Location, List<Couple<Observation, Integer>>> lob : lobs) {
                String nodeId = lob.getLeft().getLocationId();
                myMap.addNewNode(nodeId);
                boolean isWindNode=false;
                // Check wind
                for (Couple<Observation,Integer> c : lob.getRight()) 	
                {
                	if (c.getLeft() == Observation.WIND) {isWindNode = true;}
            	};
                // Si es un nodo viento no creamos arista
                // si spawn es en un nodo viento caca!
                if (isWindNode)
                    myMap.addNode(nodeId, MapAttribute.closed);
                else if (!currentPos.equals(nodeId)) {
                    myMap.addEdge(currentPos, nodeId);
                    if (!direction.equals(nodeId)) {
                    	nextCandidates.add(nodeId);
                    }
                }
            }
            if (nextCandidates.isEmpty()) {
				exitCode = 0;
				failWalk = true;
				return;
            }
            int moveId = rand.nextInt(nextCandidates.size());
            ((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextCandidates.get(moveId)));
		}

		@Override
		public boolean done() {
			if (failWalk) {
				failWalk = false;
				return true;
			}
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public int onEnd() {
			return exitCode;
		}
	}
	
	WalkToDestinationAvoidance(AbstractDedaleAgent a, MapRepresentation myMap) {
		super(a);
		assertNotEquals(myMap, null);
		this.myMap = myMap;
		this.objective = objective;
		this.registerFirstState(new WalkObjective(a), "WalkObjective");
		this.registerState(new Avoid(a), "Avoid");
		this.registerLastState(new OneShotBehaviour(a) {public void action() {}}, "End");
		String[] resetersWalk = {"WalkObjective"};
		String[] resetersAvoid = {"Avoid"};
		this.registerTransition("WalkObjective", "End", 1, resetersWalk);
		this.registerTransition("WalkObjective", "Avoid", 0, resetersWalk);
		this.registerTransition("Avoid", "WalkObjective", 0, resetersAvoid);
		this.registerTransition("Avoid", "End", 1, resetersAvoid);

	}
}
