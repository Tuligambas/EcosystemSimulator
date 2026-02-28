package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {
    private Map<String, Builder<T>> builders;
    private List<JSONObject> buildersInfo;

    public BuilderBasedFactory() {
        builders = new HashMap<>();
        buildersInfo = new LinkedList<>();
    }

    public BuilderBasedFactory(List<Builder<T>> builders) {
        this(); // Llama al constructor vacío para inicializar los atributos

        for (Builder<T> b : builders) {
            addBuilder(b);
        }
    }

    public void addBuilder(Builder<T> b) {
        builders.put(b.getTypeTag(), b); // Asocia el tipo del Builder con su instancia
        buildersInfo.add(b.getInfo()); // Almacena la información de qué puede crear este Builder
    }

    @Override
    public T createInstance(JSONObject info) { // preguntar a eugenio lo de las excepciones
        if (info == null) {
            throw new IllegalArgumentException("‘info’ cannot be null");
        }

        String type = info.getString("type");
        Builder<T> builder = builders.get(type);

        if (builder != null) {
            return builder.createInstance(info.has("data") ? info.getJSONObject("data") : new JSONObject());
        }

        throw new IllegalArgumentException("Unrecognized ‘info’: " + info.toString());
    }

    @Override
    public List<JSONObject> getInfo() {
        return Collections.unmodifiableList(buildersInfo);
    }
}
