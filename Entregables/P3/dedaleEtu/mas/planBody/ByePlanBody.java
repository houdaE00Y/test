package eu.su.mas.dedaleEtu.mas.planBody;

import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import eu.su.mas.dedaleEtu.mas.goals.HelloGoal;

public class ByePlanBody extends AbstractPlanBody {
	int attempt = 0;
	@Override
	public void action() {
		HelloGoal goal = (HelloGoal) getGoal();
		if (attempt >= 10) {
			setEndState(Plan.EndState.SUCCESSFUL);			
		} else {
			System.out.println("Bye " + goal.getText() + ": " + attempt);			
			attempt++;
		}
	}
}