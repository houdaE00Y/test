package eu.su.mas.dedaleEtu.mas.planBody;

import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.Plan;
import eu.su.mas.dedaleEtu.mas.goals.HelloGoal;

public class HelloPlanBody extends AbstractPlanBody {
	int attempt = 0;
	@Override
	public void action() {
		HelloGoal goal = (HelloGoal) getGoal();
		if (attempt >= 10) {
			setEndState(Plan.EndState.FAILED);			
		} else {
			System.out.println("Hello " + goal.getText() + ": " + attempt);			
			attempt++;
		}
	}
}