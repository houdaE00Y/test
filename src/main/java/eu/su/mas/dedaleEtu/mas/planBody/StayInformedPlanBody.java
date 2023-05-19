package eu.su.mas.dedaleEtu.mas.planBody;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.*;

import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

import bdi4jade.annotation.Parameter;
import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentationPolidama;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StayInformedPlanBody extends AbstractPlanBody {
    @Override
    public void action() {
    	MessageTemplate msgTemplate = MessageTemplate.MatchProtocol("Inform");
    	List<ACLMessage> msgsReceived = null;
    	while (true) {
    		List<ACLMessage> candidate = this.myAgent.receive(msgTemplate, 1024*1024);
    		if (candidate == null || candidate.isEmpty()) break;
    		msgsReceived = candidate;
    	}    	
    	if (msgsReceived == null || msgsReceived.isEmpty()) return;
    	Belief b = getBeliefBase().getBelief(ONTOLOGY);
    	MapaModel model = (MapaModel) b.getValue();
		MapaModel otherModel = MapaModel.importOntology(msgsReceived.get(msgsReceived.size()-1).getContent());
	    model.replaceModel(otherModel);
	    MapRepresentationPolidama mapRepresentation = (MapRepresentationPolidama) getBeliefBase().getBelief(MAP_REPRESENTATION).getValue();
	    mapRepresentation.updateFromOntology(model);
	    //System.out.println("BDI informed! " + model.getAgentLocation("SituatedAgent"));
    }
}
