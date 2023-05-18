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
import eu.su.mas.dedaleEtu.mas.goals.MapaModelGoal;
import eu.su.mas.dedaleEtu.mas.goals.SPARQLGoal;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;
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

	private Set<String> agentNames = new HashSet<String>();
	
    public BDIAgentPolidama() {

        // Create initial beliefs
        Belief iAmRegistered = new TransientPredicate<String>(I_AM_REGISTERED, false);
        
        Belief ontology = new TransientBelief<String, MapaModel>(ONTOLOGY, new MapaModel(loadOntology()));
        Belief mapRepresentation = new TransientBelief<String, MapRepresentationPolidama>(MAP_REPRESENTATION, new MapRepresentationPolidama());
        Belief situatedAgent = new TransientBelief<String, String>(SITUATED_AGENT, null);
        // Add initial desires
        Goal registerGoal = new PredicateGoal<String>(I_AM_REGISTERED, true);
        Goal findSituatedGoal = new BeliefNotNullValueGoal<String,String>(SITUATED_AGENT);        
        Goal stayInformedGoal = new Goal() {
		};
        Goal exploreGoal = new MapaModelGoal("Explore goal", (MapaModel model) -> {
        	return model.hasClosedNodes() && !model.hasOpenNodes();
        });
        
        Goal[] plans = {exploreGoal, stayInformedGoal};
        ParallelGoal plansGoal = new ParallelGoal(plans);
        
        Goal[] seq = {
        	registerGoal,
        	findSituatedGoal,
        	plansGoal
        };
        SequentialGoal seqGoal = new SequentialGoal(seq);
        
        addGoal(seqGoal);

        // Declare goal templates
        GoalTemplate registerGoalTemplate = matchesGoal(registerGoal);
        GoalTemplate findSituatedGoalTemplate = matchesGoal(findSituatedGoal);
        GoalTemplate exploreGoalTemplate = matchesGoal(exploreGoal);
        GoalTemplate stayInformedGoalTemplate = matchesGoal(stayInformedGoal);

        
        // Assign plan bodies to goals
        Plan registerPlan = new DefaultPlan(
                registerGoalTemplate, RegisterPlanBody.class);
        Plan findSituatedPlan = new DefaultPlan(
        		findSituatedGoalTemplate, FindSituatedPlanBody.class);
        // We are fully aware that we can use MessageTemplate.MatchProtocol("Inform")
        // Unfortunately this mechanism is slow for our needs
        Plan StayInformedPlan = new DefaultPlan(stayInformedGoalTemplate,
                StayInformedPlanBody.class);
        Plan requestMovePlan = new DefaultPlan(
        		exploreGoalTemplate, RequestMovePlanBody.class);

        // Init plan library
        getCapability().getPlanLibrary().addPlan(registerPlan);
        getCapability().getPlanLibrary().addPlan(findSituatedPlan);
        getCapability().getPlanLibrary().addPlan(StayInformedPlan);
        getCapability().getPlanLibrary().addPlan(requestMovePlan);

        // Init belief base
        getCapability().getBeliefBase().addBelief(iAmRegistered);
        getCapability().getBeliefBase().addBelief(ontology);
        getCapability().getBeliefBase().addBelief(mapRepresentation);
        getCapability().getBeliefBase().addBelief(situatedAgent);

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
            	if (getCurQueueSize() == 0) {
            		return super.filter(agentGoals);
            	}
            	return new HashSet<Goal>(); // First empty the queue please
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
                    /*System.out.println("BDI: " + goalEvent.getGoal() + " " +
                            "fulfilled!");*/
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
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
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
