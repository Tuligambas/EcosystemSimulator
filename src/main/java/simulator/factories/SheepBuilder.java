package simulator.factories;

import org.json.JSONObject;

import simulator.model.Animal;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends AnimalBuilder {

    @Override
    protected void fillInData(JSONObject o) {
        super.fillInData(o);
        o.put("danger_strategy", "JSON {type, data} (opcional, por defecto 'first')");
    }

    @Override
    protected Animal createInstance(JSONObject data) {
        SelectionStrategy mate = buildStrategy(data, "mate_strategy");
        SelectionStrategy danger = buildStrategy(data, "danger_strategy");
        return new Sheep(mate, danger, buildPos(data));
    }

    public SheepBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("sheep", "Crea una oveja con estrategias y posición", strategyFactory);
    }
}
