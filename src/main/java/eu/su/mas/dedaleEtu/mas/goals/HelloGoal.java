package eu.su.mas.dedaleEtu.mas.goals;

import bdi4jade.goal.Goal;

public class HelloGoal implements Goal {
	private static final long serialVersionUID = 1L;
	private final String text;
	public HelloGoal(String text) {
	this.text = text;
	}
	public String getText() {
	return text;
	}
}