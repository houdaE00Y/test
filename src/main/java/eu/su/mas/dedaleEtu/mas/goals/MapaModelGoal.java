package eu.su.mas.dedaleEtu.mas.goals;

import bdi4jade.belief.Belief;
import bdi4jade.belief.BeliefBase;
import bdi4jade.goal.AbstractBeliefGoal;
import eu.su.mas.dedaleEtu.mas.agents.dummies.sid.bdi.Constants;
import eu.su.mas.dedaleEtu.mas.knowledge.MapaModel;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class MapaModelGoal extends AbstractBeliefGoal<String> {
    private final Function<MapaModel, Boolean> func;

    public interface MyInterface {
        String doSomething(int param1, String param2);
    }
    
    public MapaModelGoal() {
    	func = null;
    }

    public MapaModelGoal(String beliefName, Function<MapaModel, Boolean> func) {
        super(beliefName);
        this.func = func;
    }

    public boolean isAchieved(BeliefBase beliefBase) {
        Belief<?, ?> belief = beliefBase.getBelief(Constants.ONTOLOGY);
        if (belief == null) {
            return false;
        } else {
        	MapaModel model = (MapaModel) belief.getValue();
            return func.apply(model);
        }
    }

    public String toString() {
        return this.getClass().getName() + ": " + this.getBeliefName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MapaModelGoal that = (MapaModelGoal) o;
        return Objects.equals(func, that.func);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), func);
    }
}
