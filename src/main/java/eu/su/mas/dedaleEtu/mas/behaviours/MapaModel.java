package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.riot.rowset.QueryResults;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;

public class MapaModel {
	Model model;
	public enum NodeType {
		Open,
		Closed
	}

	public enum MineralType {
		Gold,
		Diamond,
	}
	
	public enum AgentType {
		Explorer,
		Storage,
		Recollector,
	}
	
	public MapaModel(Model model) {
		this.model = model;
	}
	
	private String mapa(String hastag) {
		return "http://mapa#" + hastag;
	}

	private Resource getMineral(String id) {
		return model.createResource(mapa("Instance_" + id + "_resource"));
	}

	private Resource getAgent(String id) {
		return model.createResource(mapa("Instance_" + id + "_agent"));
	}
	
	private Resource getCell(String id) {
		return model.createResource(mapa("Instance_" + id + "_cell"));
	}
	
	public void addMineral(String id, MineralType mineral) {
        model.add(new StatementImpl(
        		getMineral(id),
                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                mineral == MineralType.Gold ? 
                model.getResource(mapa("Gold")) : 
            	model.getResource(mapa("Diamond"))));
	}

	public void addMineralPos(String idMineral, String idNode) {
		model.add(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("LocatedAt")), getCell(idNode)));
	}
	
	public void addNode(String id, NodeType type) {
		Resource cell = getCell(id);
		boolean alreadyOpen = model.contains(new StatementImpl(cell, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Open"))));
		boolean alreadyClosed = model.contains(new StatementImpl(cell, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Closed"))));

		if (type == NodeType.Open && (alreadyClosed || alreadyOpen)) {
			return;
		}
		else if (type == NodeType.Closed && alreadyClosed) {
			return;
		} else if (type == NodeType.Closed && alreadyOpen) {
	        model.remove(new StatementImpl(
	        		cell,
	                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
	                model.getResource(mapa("Open"))));
		}
        model.add(new StatementImpl(
        		cell,
                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                type == NodeType.Open ? 
                model.getResource(mapa("Open")) : 
            	model.getResource(mapa("Closed"))));
	}
	
	public void addNodeWindy(String id) {
        model.add(new StatementImpl(
        		getCell(id),
                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            	model.getResource(mapa("Windy"))));
	}
	
	public void addAgent(String agentName, AgentType agent) {
		Resource classOfAgent = null;
		switch (agent) {
		case Explorer:
			classOfAgent = model.getResource(mapa("Explorer"));
			break;
		case Recollector:
			classOfAgent = model.getResource(mapa("Recollector"));
			break;
		case Storage:
			classOfAgent = model.getResource(mapa("Storage"));
			break;
		default:
			break;
		}
		model.add(new StatementImpl(
        		getAgent(agentName),
                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                classOfAgent));
	}

	public void addAgentPos(String agentName, String id) {
		model.add(new StatementImpl(getAgent(agentName), model.getProperty(mapa("LocatedAt")), getCell(id)));
	}
	
	public List<String> getAgentPositions() {
		Query query = QueryFactory.create
		(
			"PREFIX mapa: <http://mapa#> " +
            "SELECT ?Position where {" +
            " ?Agent a mapa:Agent ;" +
            "  mapa:LocatedAt ?Position ." +
            "}"
		);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet result = qe.execSelect();
        while (result.hasNext()) {
        	QuerySolution entry = result.next();
        	entry.get("Position");
        	System.out.println(entry.get("Position"));
        }
        qe.close();
		return new ArrayList<>();
	}
}
