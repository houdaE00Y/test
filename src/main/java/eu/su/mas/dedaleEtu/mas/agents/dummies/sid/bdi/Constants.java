package eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi;

public class Constants {
    public static String I_AM_REGISTERED = "IAmRegistered";
    public static String ONTOLOGY = "polydama-mapstate";
    public static String SITUATED_AGENT = "situated_agent";
    public static String MAP_REPRESENTATION = "map_representation";
    public static String QUERY_SITUATED_AGENT =
            "PREFIX mapa: <http://mapa#> " +
            "SELECT ?Agent where {" +
            " ?Agent a mapa:Agent ."+
            "}";
}
