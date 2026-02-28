package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectClosest;
import simulator.model.SelectionStrategy;

public class SelectYoungestBuilder extends Builder<SelectionStrategy> {
    public SelectYoungestBuilder() {
        super("youngest", "Select de youngest candidate");
    }

    @Override
    protected SelectionStrategy createInstance(JSONObject data) {
        return new SelectClosest();
    }
}
