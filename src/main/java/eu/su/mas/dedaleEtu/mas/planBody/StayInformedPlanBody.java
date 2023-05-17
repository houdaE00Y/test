package eu.su.mas.dedaleEtu.mas.planBody;

import static eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants.ONTOLOGY;

import java.util.Map;

import org.apache.jena.rdf.model.Model;

import bdi4jade.annotation.Parameter;
import bdi4jade.belief.Belief;
import bdi4jade.plan.Plan;
import bdi4jade.plan.planbody.AbstractPlanBody;
import eu.su.mas.dedaleEtu.mas.behaviours.MapaModel;
import jade.lang.acl.ACLMessage;

public class StayInformedPlanBody extends AbstractPlanBody {
    @Override
    public void action() {
        System.out.println("Received information from situated agent!");
        setEndState(Plan.EndState.SUCCESSFUL);
    }

    @Parameter(direction = Parameter.Direction.IN)
    public void setMessage(ACLMessage msgReceived) {
    	Belief b = getBeliefBase().getBelief(ONTOLOGY);
    	MapaModel model = (MapaModel) b.getValue();
		MapaModel otherModel = MapaModel.importOntology(msgReceived.getContent());
	    model.replaceModel(otherModel);
	    System.out.println("Informed!");
    }
}
