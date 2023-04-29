package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import org.apache.jena.rdf.model.Model;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;

public class SendOntologiesBehaviour extends SimpleBehaviour {

	List<String> agents;
	MapRepresentation myMap;
	MapaModel model;
	
	public SendOntologiesBehaviour(final AbstractDedaleAgent myAgent, MapaModel model, List<String> agents) {
	}
	
	@Override
	public void action() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
