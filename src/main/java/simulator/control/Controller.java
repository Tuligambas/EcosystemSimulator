package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {

    private Simulator sim;

    // Unica constructora que recibe el simulador
    public Controller(Simulator sim) {
        if (sim != null) {
            this.sim = sim;
        }
    }

    public void loadData(JSONObject data) {
        if (data.has("regions")) { // Procesa los JSON de la regiones
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
        if (data.has("animals")) { // Procesa los JSON de los animales
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

    public void run(double t, double dt, boolean sv, OutputStream out){

        SimpleObjectViewer view = null;
        if (sv) {
        MapInfo m = sim.getMapInfo();
        view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows());
        view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
}
        // Escribo antes de la simulacion en el output la estructura JSON.
        JSONObject initState = sim.asJSON(); // Me guardo la simulacion antes dg,e runear

        while(sim.getTime() <= t){
            sim.advance(dt);
            if (sv) view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
        }

        JSONObject finalState = sim.asJSON(); // me guardo la simulacion despues de haber runeado
        JSONObject outputJSON = new JSONObject();
        outputJSON.put("in", initState);
        outputJSON.put("out", finalState);

        PrintStream p = new PrintStream(out);
        // Convertimos el JSON a texto y lo escribimos (el '4' es para que se imprima bonito e indentado)
        p.println(outputJSON.toString(4));

       if (sv) view.close();
    }
}
