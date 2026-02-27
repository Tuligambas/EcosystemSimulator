package simulator.control;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.Simulator;

public class Controller {

    private Simulator sim;

    // Unica constructora que recibe el simulador
    public Controller(Simulator sim) {
        if (sim != null) {
            this.sim = sim;
        }
    }

    public void loadData(JSONObject data) {
        if (data.has("regions")) {
            JSONArray arrayRegions = data.getJSONArray("regions");
            for (int k = 0; k < arrayRegions.length(); k++) {
                JSONObject objRegion = arrayRegions.getJSONObject(k);
                JSONArray rows = objRegion.getJSONArray("row");
                JSONArray cols = objRegion.getJSONArray("col");
                JSONObject regDescrip = objRegion.getJSONObject("spec");
                for (int i = rows.getInt(0); i <= rows.getInt(1); i++) {
                    for (int j = cols.getInt(0); j <= cols.getInt(1); j++) {
                        sim.setRegion(i, j, regDescrip);
                    }
                }
            }
        }
        if (data.has("animals")) {
            JSONArray animals = data.getJSONArray("animals");
            for (int t = 0; t < animals.length(); t++) {
                JSONObject objAnimal = animals.getJSONObject(t);
                int n = objAnimal.getInt("amount");
                JSONObject animalDescrip = objAnimal.getJSONObject("spec");
                for (int s = 0; s < n; s++) {
                    sim.addAnimal(animalDescrip);
                }
            }
        }
    }
}
