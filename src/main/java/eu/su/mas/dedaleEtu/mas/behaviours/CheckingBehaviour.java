package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;

public abstract class CheckingBehaviour extends Behaviour {
	private static final long serialVersionUID = 1L;
	
	CheckingBehaviour(AbstractDedaleAgent a) {
		super(a);
	}
	
	@Override
	public void action() {
	}
}
