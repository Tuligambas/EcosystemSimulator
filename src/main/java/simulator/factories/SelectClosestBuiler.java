package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectClosest;
import simulator.model.SelectionStrategy;

public class SelectClosestBuiler extends Builder<SelectionStrategy> {

    public SelectClosestBuiler() {
        super("closest", "Select the closest constructor");
    }

    @Override
    protected SelectionStrategy createInstance(JSONObject data) {
        return new SelectClosest();
    }

}
