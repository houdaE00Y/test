package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.core.SingleCapabilityAgent;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import eu.su.mas.dedaleEtu.mas.goals.HelloGoal;
import eu.su.mas.dedaleEtu.mas.planBody.ByePlanBody;
import eu.su.mas.dedaleEtu.mas.planBody.HelloPlanBody;

public class TestBDIAgent extends SingleCapabilityAgent {
public 
	TestBDIAgent() {
		Plan plan1 = new DefaultPlan(HelloGoal.class, HelloPlanBody.class);
		Plan plan2 = new DefaultPlan(HelloGoal.class, ByePlanBody.class);
		addGoal(new HelloGoal("world"));
		getCapability().getPlanLibrary().addPlan(plan1);
		getCapability().getPlanLibrary().addPlan(plan2);
}
}
