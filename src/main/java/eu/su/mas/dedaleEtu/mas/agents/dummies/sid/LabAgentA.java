package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.FastWalk;
import eu.su.mas.dedaleEtu.mas.behaviours.RandomWalkBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.WalkToB;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LabAgentA extends AbstractDedaleAgent {
	private int nA;
	private int nB;
	private boolean chaser = true;
	
	protected void setup() {
		super.setup();
		
		System.out.println("A AID is: " + getAID().getName());
		Object[] args = getArguments();
		if (args != null)
			for (int i = 0 ; i<args.length; ++i)
				System.out.println("- " + args[i]);
		
		// use them as parameters for your behaviours is you want
		List<Behaviour> lb = new ArrayList<>();
		List<String> agentNames = new ArrayList <String>();
		agentNames.add("B");

		lb.add(
        		new OneShotBehaviour(this) {
					@Override
					public void action() {
		        		myAgent.addBehaviour(new WalkToB((AbstractDedaleAgent) myAgent, new MapRepresentation(), agentNames));
					}
				}
        );

		// MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		addBehaviour(new startMyBehaviours(this, lb));
	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown() {
		System.out.println("A says: \"Fk you! You killed me!\"");
		super.takeDown();
	}

	/**
	 * This method is automatically called before migration. You can add here all
	 * the saving you need
	 */
	protected void beforeMove() {
		super.beforeMove();
	}

	/**
	 * This method is automatically called after migration to reload. You can add
	 * here all the info regarding the state you want your agent to restart from
	 */
	protected void afterMove() {
		super.afterMove();
	}
}
