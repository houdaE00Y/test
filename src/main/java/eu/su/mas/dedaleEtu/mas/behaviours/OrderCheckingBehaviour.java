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
	
	public OrderCheckingBehaviour(AbstractDedaleAgent a, List<Behaviour> subBehaviours) {
		super(a);
		for (Behaviour subBehaviour : subBehaviours)
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
		return (Behaviour) subBehaviours.get(current);
	}

	@Override
	public Collection getChildren() {
		return (Collection) subBehaviours;
	}
}
