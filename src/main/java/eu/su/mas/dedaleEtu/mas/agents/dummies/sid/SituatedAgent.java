package eu.su.mas.dedaleEtu.mas.agents.dummies.sid;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.MapaModel;
import eu.su.mas.dedaleEtu.mas.behaviours.MapaModel.AgentType;
import eu.su.mas.dedaleEtu.mas.behaviours.MyExploOntologyBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveOntologiesBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendOntologiesBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SituatedAgent extends AbstractDedaleAgent {
	private static final long serialVersionUID = 1L;
	//private MapaModel model;
	protected void setup() {
		super.setup();
		
		System.out.println("Ontology AID is: " + getAID().getName());
		Object[] args = getArguments();
		if (args != null)
			for (int i = 0 ; i<args.length; ++i)
				System.out.println("- " + args[i]);
			
		
		EntityCharacteristics infoAgent = (EntityCharacteristics) args[0];
		final AgentType agentType;
		switch (infoAgent.getMyEntityType()) {
		case AGENT_COLLECTOR:
			agentType = AgentType.Recollector;
			break;
		case AGENT_EXPLORER:
			agentType = AgentType.Explorer;
			break;
		default:
			agentType = AgentType.Storage;
			break;			
		}
		// use them as parameters for your behaviours is you want
		List<Behaviour> lb = new ArrayList<>();

		lb.add(
        		new OneShotBehaviour(this) {
					@Override
					public void action() {
						Set<String> agentNames = new HashSet<String>();

						// Build the description used as template for the search
			        	DFAgentDescription template = new DFAgentDescription();
			        	ServiceDescription templateSd = new ServiceDescription();
			        	templateSd.addOntologies("polydama-mapstate");
			        	template.addServices(templateSd);
			        	SearchConstraints sc = new SearchConstraints();
			        	// We want to receive 10 results at most
			        	sc.setMaxResults(Long.valueOf(1000));
			        	
			        	DFAgentDescription[] results;
						try {
							results = DFService.search(getAgent(), template, sc);
				        	if (results.length > 0) {
				        		for (DFAgentDescription dfd : results) {
					        		agentNames.add(dfd.getName().getLocalName().toString());
	
					                System.out.println("Found pal: " + dfd.getName().getLocalName());
				        		}
				        	}
						} catch (FIPAException e) {
							e.printStackTrace();
						}
						agentNames.remove(getAgent().getLocalName());
						
						
						MapRepresentation map = new MapRepresentation();
						MapaModel model = new MapaModel(loadOntology());
						model.addAgent(getLocalName(), agentType);
						myAgent.addBehaviour(new ReceiveOntologiesBehaviour((AbstractDedaleAgent) myAgent, model, agentNames));
						myAgent.addBehaviour(new MyExploOntologyBehaviour((AbstractDedaleAgent) myAgent, map, model));
						myAgent.addBehaviour(new SendOntologiesBehaviour((AbstractDedaleAgent) myAgent, model, agentNames));
					}
				}
        );

		// MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		addBehaviour(new startMyBehaviours(this, lb));
		
		DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName("polydama-Ontology");
        sd.setType("polydama-Ontology");
        sd.addOntologies("polydama-mapstate");
        sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
        dfd.addServices(sd);
        try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
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
	
    public static Model loadOntology() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntDocumentManager dm = model.getDocumentManager();
        URL fileAsResource = OntologyAgent.class.getClassLoader().getResource("mapa.owl");
        System.out.println(fileAsResource);
        dm.addAltEntry("mapa", fileAsResource.toString());
        model.read("mapa", null, "RDF/XML");
        return model;
    }
}