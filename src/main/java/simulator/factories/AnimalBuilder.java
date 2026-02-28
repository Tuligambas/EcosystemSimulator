package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectionStrategy;

public abstract class AnimalBuilder extends Builder<Animal> {
    protected final Factory<SelectionStrategy> strategyFactory;

    protected AnimalBuilder(String typeTag, String desc, Factory<SelectionStrategy> strategyFactory) {
        super(typeTag, desc);
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected void fillInData(JSONObject o) {
        o.put("mate_strategy", "JSON {type, data} (opcional, por defecto 'first')");

        JSONObject pos = new JSONObject();
        pos.put("x_range", "[x_min, x_max] (double) (opcional si 'pos' existe)");
        pos.put("y_range", "[y_min, y_max] (double) (opcional si 'pos' existe)");
        o.put("pos", pos);
    }

    protected SelectionStrategy buildStrategy(JSONObject data, String key) {
        if (data.has(key)) {
            return strategyFactory.createInstance(data.getJSONObject(key));
        }
        return strategyFactory.createInstance(new JSONObject().put("type", "first").put("data", new JSONObject()));
    }

    // Construye la posición inicial a partir de los rangos recibidos.
    protected Vector2D buildPos(JSONObject data) {
        if (!data.has("pos"))
            return null;

        JSONObject p = data.getJSONObject("pos");
        if (!p.has("x_range") || !p.has("y_range"))
            throw new IllegalArgumentException("Faltan rangos en 'pos'");

        double xMin = p.getJSONArray("x_range").getDouble(0);
        double xMax = p.getJSONArray("x_range").getDouble(1);
        double yMin = p.getJSONArray("y_range").getDouble(0);
        double yMax = p.getJSONArray("y_range").getDouble(1);

        if (xMin > xMax || yMin > yMax)
            throw new IllegalArgumentException("Rangos de posición inválidos");

        double x = xMin + Utils.RAND.nextDouble() * (xMax - xMin);
        double y = yMin + Utils.RAND.nextDouble() * (yMax - yMin);

        return new Vector2D(x, y);
    }
}
