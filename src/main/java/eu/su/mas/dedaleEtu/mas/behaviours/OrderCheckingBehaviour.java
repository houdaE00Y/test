package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CompositeBehaviour;
import jade.util.leap.Collection;
import jade.util.leap.ArrayList;


public class OrderCheckingBehaviour extends CompositeBehaviour {
	private static final long serialVersionUID = 1L;
	private ArrayList subBehaviours = new ArrayList();
	int current = 0;
	
	public OrderCheckingBehaviour(AbstractDedaleAgent a) {
		super(a);
	}
	
	void addBehaviour(Behaviour subBehaviour) {
		this.subBehaviours.add(subBehaviour);
	}
	
	@Override
	protected void scheduleFirst() {
		current = 0;
	}

	@Override
	protected void scheduleNext(boolean currentDone, int currentResult) {
		current++;
		if (current == subBehaviours.size()) current = 0;
	}

	@Override
	protected boolean checkTermination(boolean currentDone, int currentResult) {
		return currentDone;
	}

	@Override
	protected Behaviour getCurrent() {
		Behaviour b = (Behaviour) subBehaviours.get(current);
		int counter = 0;
		while (!b.isRunnable() && counter != subBehaviours.size()) {
			scheduleNext(false, 0);
			b = (Behaviour) subBehaviours.get(current);
			++counter;
		}		
		return (Behaviour) subBehaviours.get(current);
	}

	@Override
	public Collection getChildren() {
		return (Collection) subBehaviours;
	}
}
