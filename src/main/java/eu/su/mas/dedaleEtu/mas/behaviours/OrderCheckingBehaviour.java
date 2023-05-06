package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CompositeBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.util.leap.Collection;


public class OrderCheckingBehaviour extends SimpleBehaviour {
	private static final long serialVersionUID = 1L;
	private ArrayList<Behaviour> subBehaviours = new ArrayList<Behaviour>();
	boolean finished = false;
	
	public OrderCheckingBehaviour(AbstractDedaleAgent a) {
		super(a);
	}
	
	public void addBehaviour(Behaviour subBehaviour) {
		this.subBehaviours.add(subBehaviour);
	}

	@Override
	public void action() {
		for (Behaviour b : subBehaviours) {
			if (!b.isRunnable()) continue;
			b.actionWrapper();
			if (b.done()) {
				finished = true;
				break;
			}
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}
}
