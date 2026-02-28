package simulator.factories;

import org.json.JSONObject;

import simulator.model.Animal;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends AnimalBuilder {

    @Override
    protected void fillInData(JSONObject o) {
        super.fillInData(o);
        o.put("hunt_strategy", "JSON {type, data} (opcional, por defecto 'first')");
    }

    @Override
    protected Animal createInstance(JSONObject data) {
        SelectionStrategy mate = buildStrategy(data, "mate_strategy");
        SelectionStrategy hunt = buildStrategy(data, "hunt_strategy");
        return new Wolf(mate, hunt, buildPos(data));
    }

    public WolfBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("wolf", "Crea un lobo con estrategias y posición", strategyFactory);
    }
}
