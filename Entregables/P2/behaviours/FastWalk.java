package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.TickerBehaviour;

import java.io.IOException;
import java.util.List;
import java.util.Random;

// BEHAVIOUR RandomWalk : Illustrates how an agent can interact with, and move in, the environment
public class FastWalk extends TickerBehaviour {
    /**
     * When an agent choose to move
     */
    private static final long serialVersionUID = 9088209402507795289L;
    public FastWalk(final AbstractDedaleAgent myagent) {
        super(myagent, 6);
    }

    @Override
    public void onTick() {
        //Example to retrieve the current position
        Location myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        //System.out.print(this.myAgent.getLocalName() + " POS: " + myPosition);
        if (myPosition != null) {
            //List of observable from the agent's current position
            List<Couple<Location, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition
            //System.out.println(" " + lobs);
            //Little pause to allow you to follow what is going on
            /*try {
                System.out.println("Press enter in the console to allow the agent " + this.myAgent.getLocalName() + " to execute its next move");
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            //list of observations associated to the currentPosition
            List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();

            //example related to the use of the backpack for the treasure hunt
            boolean b = false;
            for (Couple<Observation, Integer> o : lObservations) {
                switch (o.getLeft()) {
                    case DIAMOND:
                    case GOLD:
//                        System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
//                        System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
//                        System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
//                        System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + ((AbstractDedaleAgent) this.myAgent).pick());
//                        System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
//                        b = true;
                        break;
                    default:
                        break;
                }
            }

            //If the agent picked (part of) the treasure
            if (b) {
                List<Couple<Location, List<Couple<Observation, Integer>>>> lobs2 = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition
                System.out.println(this.myAgent.getLocalName() + " - State of the observations after trying to pick something " + lobs2);
            }

            //removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
            //lobs= lobs.stream().filter(x -> !x.getLeft().equals(myPosition)).toList();
            //System.out.println(lobs);
            
            //Random move from the current position
            Random r = new Random();
            int moveId = 1 + r.nextInt(lobs.size() - 1);

            // Si nos podemos tirar al pozo, vamos de cabeza
            for (int i = 1; i< lobs.size(); ++i) {
            	if(!lobs.get(i).getRight().isEmpty() && lobs.get(i).getRight().get(0).getLeft() == Observation.WIND) {
            		System.out.println(this.myAgent.getLocalName() + " - is about to commit sudoku");
            		moveId = i;
            		i= lobs.size();
            	}
            }
            
            //The move action (if any) should be the last action of your behaviour
            ((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(moveId).getLeft());
        }
    }
}
