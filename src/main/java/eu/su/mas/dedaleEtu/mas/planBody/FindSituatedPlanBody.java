package eu.su.mas.dedaleEtu.mas.planBody;

import bdi4jade.annotation.TransientBelief;
import bdi4jade.belief.Belief;
import bdi4jade.core.SingleCapabilityAgent;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import bdi4jade.plan.planbody.BeliefGoalPlanBody;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.StatementImpl;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.ONTOLOGY;

public class FindSituatedPlanBody extends BeliefGoalPlanBody {
    @Override
    protected void execute() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType("polydama-BDIagent-situated");
        templateSd.setName("polydama-situated");
        template.addServices(templateSd);
        DFAgentDescription[] results;
        try {
            results = DFService.search(this.myAgent, template);
            if (results.length > 0)
            	for (DFAgentDescription dfd : results) {
	        		AID provider = dfd.getName();
	                if (!provider.getLocalName().equals(this.myAgent.getLocalName())) {
	                	Belief<String,String> belief = (Belief<String,String>)getBeliefBase().getBelief(Constants.SITUATED_AGENT);
	                	belief.setValue(provider.getLocalName()); 
	                }
                }
            // if results.length == 0, no endState is set,
            // so the plan body will run again (if the goal still holds)
        } catch (FIPAException e) {
            setEndState(Plan.EndState.FAILED);
            e.printStackTrace();
        }
    }
}
