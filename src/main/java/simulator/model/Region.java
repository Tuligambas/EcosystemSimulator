package simulator.model;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo{

    protected final static double FOOD_EAT_RATE_HERBS = 60.0;
    protected final static double FOOD_SHORTAGE_TH_HERBS = 5.0;
    protected final static double FOOD_SHORTAGE_EXP_HERBS = 2.0;
    // Atributos.
    protected List<Animal> AnimalsThisRegion;

    // Constructora que inicializa la lista de animales por defecto.
    public Region(){
        this.AnimalsThisRegion = new ArrayList<Animal>();
    }

    // Estructura JSON.
    public JSONObject asJSON(){
        JSONObject o = new JSONObject();
        JSONArray array = new JSONArray();
        for(Animal animal: this.getAnimals()){
            array.put(animal.asJSON());
        }
        o.put("animals", array);
        return o;
    }

    final void addAnimal(Animal a){
        this.AnimalsThisRegion.add(a);
    }

    final void removeAnimal(Animal a){
        this.AnimalsThisRegion.remove(a);
    }

    final List<Animal> getAnimals(){
        return this.AnimalsThisRegion;
    }

    // Sirve para contar el numero de hervivoros y usarlo para la formula.
    public int busquedaHervivoros(){
        int n = 0;
        for(Animal animal: this.getAnimals()){
            if(animal.getDiet().equals(Diet.HERVIVORO)){
                n++;
            }
        }
        return n;
    }
}
