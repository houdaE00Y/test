package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

public class Constants {
    public static String I_AM_REGISTERED = "IAmRegistered";
    public static String ONTOLOGY = "polydama-mapstate";
    public static String QUERY_SITUATED_AGENT =
            "PREFIX mapa: <http://mapa#> " +
            "SELECT ?Agent where {" +
            " ?Agent a mapa:Agent ."+
            "}";
    public static String QUERY_OPEN_NODE =
			"PREFIX mapa: <http://mapa#> " +
		    "SELECT ?Node where {" +
		    " ?Node a mapa:Open ." +
		    "}";
}
