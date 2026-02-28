package simulator.factories;

import org.json.JSONObject;

import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {
    public DynamicSupplyRegionBuilder() {
        super("dynamic", "Crea una región dinámica con comida variable");
    }

    @Override
    protected void fillInData(JSONObject o) {
        o.put("food", "Cantidad inicial de comida (double)");
        o.put("factor", "Variación de comida por segundo; >0 añade, <0 reduce (double)");
    }

    @Override
    protected Region createInstance(JSONObject data) {
        double food = data.getDouble("food");
        double factor = data.getDouble("factor");
        return new DynamicSupplyRegion(food, factor);
    }
}
