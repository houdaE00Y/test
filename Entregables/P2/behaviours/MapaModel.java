package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.rowset.QueryResults;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.OntologyAgent;

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
	
	static Pattern patternIdCell = Pattern.compile("^http://mapa#Instance_(.+?)_cell$");
	static Pattern patternIdRecource = Pattern.compile("^http://mapa#Instance_(.+?)_recource$");
	static Pattern patternIdAgent = Pattern.compile("^http://mapa#Instance_(.+?)_agent$");
	
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
	
	private void addCheckStmt(StatementImpl stmt) {
		if (!model.contains(stmt)) model.add(stmt);
	}
	
	public void addMineral(String id, MineralType mineral) {
			addCheckStmt(new StatementImpl(
	        		getMineral(id),
	                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
	                mineral == MineralType.Gold ? 
	                model.getResource(mapa("Gold")) : 
	            	model.getResource(mapa("Diamond"))));
		
	}

	public void addMineralPos(String idMineral, String idNode) {
			Query query = QueryFactory.create
			(
				"PREFIX mapa: <http://mapa#> " +
	            "SELECT ?Position where {" +
	            "  mapa:Instance_" + idMineral + "_resource a mapa:Resource ;" +
	            "  mapa:LocatedAt ?Position ." +
	            "}"
			);
	
			QueryExecution qe = QueryExecutionFactory.create(query, model);
	        ResultSet result = qe.execSelect();
	        if (result.hasNext()) {
	        	QuerySolution entry = result.next();
	        	Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
	        	if (matcher.find()) {
	    			model.remove(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("LocatedAt")), getCell(matcher.group(1))));
	        	}
	        }
	        qe.close();
	        addCheckStmt(new StatementImpl(getMineral(idMineral), model.getProperty(mapa("LocatedAt")), getCell(idNode)));
		
	}
	
	public void addAdjancency(String node1id,String node2id) {
		Resource node1 = getCell(node1id);
		Resource node2 = getCell(node2id);
		addCheckStmt(new StatementImpl(node1, model.getProperty(mapa("Adjacent")), node2));
		addCheckStmt(new StatementImpl(node2, model.getProperty(mapa("Adjacent")), node1));
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
		addCheckStmt(new StatementImpl(
        		cell,
                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                type == NodeType.Open ? 
                model.getResource(mapa("Open")) : 
            	model.getResource(mapa("Closed"))));
	}
	
	public void addNodeWindy(String id) {
		addCheckStmt(new StatementImpl(
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
		StatementImpl stmt = new StatementImpl(
        		getAgent(agentName),
                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                classOfAgent);
		if (!model.contains(stmt)) {
			model.add(stmt);
		}
	}

	public AgentType getAgentType(String agentName) {
		Resource agent = getAgent(agentName);
		if (model.contains(new StatementImpl(agent, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Explorer"))))) {
			return AgentType.Explorer;
		}
		if (model.contains(new StatementImpl(agent, model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), model.getResource(mapa("Recollector"))))) {
			return AgentType.Recollector;
		}
		return AgentType.Storage;
	}
	
	public void addAgentPos(String agentName, String id) {
		{
		Query query = QueryFactory.create
		(
			"PREFIX mapa: <http://mapa#> " +
            "SELECT ?Position where {" +
            "  mapa:Instance_" + agentName + "_agent mapa:LocatedAt ?Position ." +
            "}"
		);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet result = qe.execSelect();
        if (result.hasNext()) {
        	QuerySolution entry = result.next();
        	Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
        	if (matcher.find()) {
    			model.remove(new StatementImpl(getAgent(agentName), model.getProperty(mapa("LocatedAt")), getCell(matcher.group(1))));
        	}
        }
        qe.close();
		}
		{
		Query query = QueryFactory.create
		(
			"PREFIX mapa: <http://mapa#> " +
            "SELECT ?Agent where {" +
            "  ?Agent mapa:LocatedAt mapa:Instance_" + id + "_cell ;" +
            "  a ?Type ." +
            "  FILTER(?Type IN (mapa:Explorer, mapa:Recollector, mapa:Storage) )" +
            "}"
		);
		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet result = qe.execSelect();
        if (result.hasNext()) {
        	QuerySolution entry = result.next();
        	Matcher matcher = patternIdAgent.matcher(entry.get("Agent").toString());
        	if (matcher.find()) {
    			model.remove(new StatementImpl(getAgent(matcher.group(1)), model.getProperty(mapa("LocatedAt")), getCell(id)));
        	}
        }
        qe.close();
		}
        addCheckStmt(new StatementImpl(getAgent(agentName), model.getProperty(mapa("LocatedAt")), getCell(id)));	
	}
	
	public Map<String, String> getAgentPositions() {
		Query query = QueryFactory.create
		(
			"PREFIX mapa: <http://mapa#> " +
            "SELECT ?Agent ?Position where {" +
            " ?Agent a ?Type ;" +
            "  mapa:LocatedAt ?Position ." +
            "  FILTER(?Type IN (mapa:Explorer, mapa:Recollector, mapa:Storage) )" +
            "}"
		);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet result = qe.execSelect();
        HashMap<String, String> returnedList = new HashMap<String, String>();
        while (result.hasNext()) {
        	QuerySolution entry = result.next();
        	Matcher matcher1 = patternIdAgent.matcher(entry.get("Agent").toString());
        	Matcher matcher2 = patternIdCell.matcher(entry.get("Position").toString());
        	if (matcher1.find() && matcher2.find()) returnedList.put(matcher1.group(1), matcher2.group(1));
        }
        qe.close();
		return returnedList;
	}
	
	public void removeAllAgentPositionsInSet(Set<String> agentNames) {
		Query query = QueryFactory.create
		(
			"PREFIX mapa: <http://mapa#> " +
            "SELECT ?Agent ?Position where {" +
            " ?Agent a ?Type ;" +
            "  mapa:LocatedAt ?Position ." +
            "  FILTER(?Type IN (mapa:Explorer, mapa:Recollector, mapa:Storage) )" +
            "}"
		);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet result = qe.execSelect();
        HashMap<String, String> removals = new HashMap<String, String>();
        while (result.hasNext()) {
        	QuerySolution entry = result.next();
        	
        	String position = null;
        	String agent = null;
        	Matcher matcher = patternIdCell.matcher(entry.get("Position").toString());
        	if (matcher.find()) position = matcher.group(1);
        	matcher = patternIdAgent.matcher(entry.get("Agent").toString());
        	if (matcher.find()) agent = matcher.group(1);
        	if (agent == null || position == null || !agentNames.contains(agent))
        		continue;
        	removals.put(agent, position);
        }
        qe.close();
        for (Entry<String, String> agentPosition : removals.entrySet()) {
        	model.remove(new StatementImpl(getAgent(agentPosition.getKey()), model.getProperty(mapa("LocatedAt")), getCell(agentPosition.getValue())));
        }
	}
	
	public String getOntology() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		model.write(bytes,"N-TRIPLE",null);
		return bytes.toString(StandardCharsets.UTF_8);
	}
	
	public static MapaModel importOntology(String onto) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		InputStream stream = new ByteArrayInputStream(onto.getBytes(StandardCharsets.UTF_8));
		model.read(stream, null, "N-TRIPLE");
        return new MapaModel(model);
	}

	public Set<String> getClosedNodes() {
		Query query = QueryFactory.create
		(
			"PREFIX mapa: <http://mapa#> " +
            "SELECT ?Node where {" +
            " ?Node a mapa:Closed ." +
            "}"
		);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet result = qe.execSelect();
        Set<String> returnedList = new HashSet<String>();
        while (result.hasNext()) {
        	QuerySolution entry = result.next();
        	Matcher matcher = patternIdCell.matcher(entry.get("Node").toString());
        	if (matcher.find()) returnedList.add(matcher.group(1));
        }
        qe.close();
		return returnedList;
	}

	public void removeClosedNodes(Set<String> closedNodes) {
		Query query = QueryFactory.create
		(
			"PREFIX mapa: <http://mapa#> " +
            "SELECT ?Node where {" +
            " ?Node a mapa:Open ." +
            "}"
		);
		
		QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet result = qe.execSelect();
        ArrayList<String> positions = new ArrayList<String>();
        while (result.hasNext()) {
        	QuerySolution entry = result.next();
        	
        	String position = null;
        	Matcher matcher = patternIdCell.matcher(entry.get("Node").toString());
        	if (matcher.find()) position = matcher.group(1);
        	if (position == null || !closedNodes.contains(position))
        		continue;
        	positions.add(position);
        }
        qe.close();
        for (String position : positions) {
	        model.remove(new StatementImpl(
        		getCell(position),
                model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                model.getResource(mapa("Open"))));
        }
	}
}
