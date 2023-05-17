package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

import bdi4jade.belief.Belief;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientPredicate;
import bdi4jade.core.GoalUpdateSet;
import bdi4jade.core.SingleCapabilityAgent;
import bdi4jade.event.GoalEvent;
import bdi4jade.event.GoalListener;
import bdi4jade.goal.*;
import bdi4jade.plan.DefaultPlan;
import bdi4jade.plan.Plan;
import bdi4jade.reasoning.*;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.OntologyAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.MapaModel;
import eu.su.mas.dedaleEtu.mas.goals.SPARQLGoal;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama;
import eu.su.mas.dedaleEtu.mas.planBody.FindSituatedPlanBody;
import eu.su.mas.dedaleEtu.mas.planBody.KeepMailboxEmptyPlanBody;
import eu.su.mas.dedaleEtu.mas.planBody.RegisterPlanBody;
import eu.su.mas.dedaleEtu.mas.planBody.RequestMovePlanBody;
import eu.su.mas.dedaleEtu.mas.planBody.StayInformedPlanBody;
import jade.lang.acl.MessageTemplate;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.StatementImpl;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

public class BDIAgentPolidama extends SingleCapabilityAgent {

	private MapRepresentationPolidama map = new MapRepresentationPolidama();
	private Set<String> agentNames = new HashSet<String>();
	private Set<String> msgsReceived = new HashSet<String>();
	
    public BDIAgentPolidama() {

		
        // Create initial beliefs
        Belief iAmRegistered = new TransientPredicate(I_AM_REGISTERED, false);
        Belief ontology = new TransientBelief(ONTOLOGY, new MapaModel(loadOntology()));

        // Add initial desires
        Goal registerGoal = new PredicateGoal(I_AM_REGISTERED, true);
        Goal findSituatedGoal = new SPARQLGoal(ONTOLOGY, QUERY_SITUATED_AGENT);
        Goal requestMoveGoal = new SPARQLGoal(ONTOLOGY, QUERY_OPEN_NODE);
        addGoal(registerGoal);
        addGoal(findSituatedGoal);
        addGoal(requestMoveGoal);

        // Declare goal templates
        GoalTemplate registerGoalTemplate = matchesGoal(registerGoal);
        GoalTemplate findSituatedTemplate = matchesGoal(findSituatedGoal);
        GoalTemplate requestMoveTemplate = matchesGoal(requestMoveGoal);

        // Assign plan bodies to goals
        Plan registerPlan = new DefaultPlan(
                registerGoalTemplate, RegisterPlanBody.class);
        Plan findSituatedPlan = new DefaultPlan(
                findSituatedTemplate, FindSituatedPlanBody.class);
        Plan StayInformedPlan = new DefaultPlan(MessageTemplate.MatchProtocol("Order"),
                StayInformedPlanBody.class);
        Plan requestMovePlan = new DefaultPlan(
                requestMoveTemplate, RequestMovePlanBody.class);

        // Init plan library
        getCapability().getPlanLibrary().addPlan(registerPlan);
        getCapability().getPlanLibrary().addPlan(findSituatedPlan);
        getCapability().getPlanLibrary().addPlan(StayInformedPlan);

        // Init belief base
        getCapability().getBeliefBase().addBelief(iAmRegistered);
        getCapability().getBeliefBase().addBelief(ontology);

        // Add a goal listener to track events
        enableGoalMonitoring();

        // Override BDI cycle meta-functions, if needed
        overrideBeliefRevisionStrategy();
        overrideOptionGenerationFunction();
        overrideDeliberationFunction();
        overridePlanSelectionStrategy();
    }

    private void overrideBeliefRevisionStrategy() {
        this.getCapability().setBeliefRevisionStrategy(new DefaultBeliefRevisionStrategy() {
            @Override
            public void reviewBeliefs() {
                // This method should check belief base consistency,
                // make new inferences, etc.
                // The default implementation does nothing
            }
        });
    }

    private void overrideOptionGenerationFunction() {
        this.getCapability().setOptionGenerationFunction(new DefaultOptionGenerationFunction() {
            @Override
            public void generateGoals(GoalUpdateSet agentGoalUpdateSet) {
                // A GoalUpdateSet contains the goal status for the agent:
                // - Current goals (.getCurrentGoals)
                // - Generated goals, existing but not adopted yet (.getGeneratedGoals)
                // - Dropped goals, discarded forever (.getDroppedGoals)
                // This method should update these three sets (current,
                // generated, dropped).
                // The default implementation does nothing
            }
        });
    }

    private void overrideDeliberationFunction() {
        this.getCapability().setDeliberationFunction(new DefaultDeliberationFunction() {
            @Override
            public Set<Goal> filter(Set<GoalUpdateSet.GoalDescription> agentGoals) {
                // This method should choose which of the current goal
                // of the agent should become intentions in this iteration
                // of the BDI cycle.
                // The default implementation chooses all goals with no
                // actual filtering.
                return super.filter(agentGoals);
            }
        });
    }

    private void overridePlanSelectionStrategy() {
        this.getCapability().setPlanSelectionStrategy(new DefaultPlanSelectionStrategy() {
            @Override
            public Plan selectPlan(Goal goal, Set<Plan> capabilityPlans) {
                // This method should return a plan from a list of
                // valid (ordered) plans for fulfilling a particular goal.
                // The default implementation just chooses
                // the first plan of the list.
                return super.selectPlan(goal, capabilityPlans);
            }
        });
    }

    private void enableGoalMonitoring() {
        this.addGoalListener(new GoalListener() {
            @Override
            public void goalPerformed(GoalEvent goalEvent) {
                if(goalEvent.getStatus() == GoalStatus.ACHIEVED) {
                    System.out.println("BDI: " + goalEvent.getGoal() + " " +
                            "fulfilled!");
                }
            }
        });
    }

    private GoalTemplate matchesGoal(Goal goalToMatch) {
        return new GoalTemplate() {
            @Override
            public boolean match(Goal goal) {
                return goal == goalToMatch;
            }
        };
    }

    public static Model loadOntology() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntDocumentManager dm = model.getDocumentManager();
        URL fileAsResource = OntologyAgent.class.getClassLoader().getResource("mapa.owl");
        System.out.println(fileAsResource);
        dm.addAltEntry("mapa", fileAsResource.toString());
        model.read("mapa", null, "RDF/XML");
        return model;
    }

    public void AddSituated(String agent) {
    	agentNames.add(agent);
    }

    public Set<String> GetSituated() {
    	return agentNames;
    }
}
