package simulator.factories;

import org.json.JSONObject;

import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {
    public DynamicSupplyRegionBuilder() {
        super("dynamic", "Create a dinamic region with variable food");
    }

    @Override
    protected void fillInData(JSONObject o) {
        o.put("food", "Initial amount of food (double)");
        o.put("factor", "Food variation per second; >0 adds, <0 reduces (double)");
    }

    @Override
    protected Region createInstance(JSONObject data) {
        double food = data.getDouble("food");
        double factor = data.getDouble("factor");
        return new DynamicSupplyRegion(food, factor);
    }
}
