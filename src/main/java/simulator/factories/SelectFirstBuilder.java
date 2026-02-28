package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {

    public SelectFirstBuilder() {
        super("first", "Select the first candidate");
    }

    @Override
    protected SelectionStrategy createInstance(JSONObject data) {
        return new SelectFirst();
    }
}