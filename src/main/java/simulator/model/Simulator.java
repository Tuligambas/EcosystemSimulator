package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

    private double time;
    private final List<Animal> animals;
    private final RegionManager regionManager;
    private final Factory<Animal> animalsFactory;
    private final Factory<Region> regionsFactory;
    private List<EcoSysObserver> observers;

    public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory,
            Factory<Region> regionsFactory) {
        this.time = 0.0;
        this.animals = new ArrayList<>();
        this.regionManager = new RegionManager(cols, rows, width, height);
        this.animalsFactory = animalsFactory;
        this.regionsFactory = regionsFactory;
        this.observers = new ArrayList<>();
    }

    @Override
    public JSONObject asJSON() {
        JSONObject o = new JSONObject();
        o.put("time", this.time);
        o.put("state", this.regionManager.asJSON());
        return o;
    }

    // Regiones
    private void setRegion(int row, int col, Region r) {
        this.regionManager.setRegion(row, col, r);

        for (EcoSysObserver o : observers) {
            o.onRegionSet(row, col, regionManager, r);
        }
    }

    public void setRegion(int row, int col, JSONObject rJson) {
        Region r = regionsFactory.createInstance(rJson);
        setRegion(row, col, r);
    }

    // Animales
    private void addAnimal(Animal a) {
        this.animals.add(a);
        this.regionManager.registerAnimal(a);

        for (EcoSysObserver o : observers) {
            o.onAnimalAdded(time, regionManager, Collections.unmodifiableList(animals), a);
        }
    }

    public void addAnimal(JSONObject aJson) {
        Animal a = animalsFactory.createInstance(aJson);
        addAnimal(a);
    }

    // Consultas
    public MapInfo getMapInfo() {
        return this.regionManager;
    }

    public List<? extends AnimalInfo> getAnimals() {
        return Collections.unmodifiableList(animals);
    }

    public double getTime() {
        return this.time;
    }

    private void notifyOnAdvance(double dt) {
        for (EcoSysObserver o : observers) { // En vez de esto podria usar la funcion que tengo abajo comentada
            o.onAdvance(time, regionManager, Collections.unmodifiableList(animals), dt);
        }
    }

    public void advance(double dt) {
        time += dt;
        // eliminar muertos
        for (int i = animals.size() - 1; i >= 0; i--) {
            Animal a = animals.get(i);
            if (a.getState().equals(State.DEAD)) {
                regionManager.unregisterAnimal(a);
                animals.remove(i);
            }
        }

        // actualizar animales y su región
        List<Animal> animalsList = new ArrayList<>(animals);

        for (Animal a : animalsList) {
            a.update(dt);
            regionManager.updateanimalRegion(a);
        }

        // actualizar regiones
        regionManager.updateAllRegions(dt);

        // añadir bebés
        List<Animal> newborns = new ArrayList<>();

        for (Animal a : animals) {
            if (a.isPregnant()) {
                Animal baby = a.deliverBaby();
                if (baby != null) {
                    newborns.add(baby);
                }
            }
        }

        for (Animal b : newborns) {
            addAnimal(b);
        }

        notifyOnAdvance(dt); // Para notificar el paso de la simulacion a los observadores y que lo puedan
                             // ver la nueva actualizacion
    }

    /*
     * private void notifyOnAdvance(double dt) {
     * List<AnimalInfo> animalss = new ArrayList<>(animals);
     * for(EcoSysObserver o: observers){
     * o.onAdvance(time, regionManager, animalss, dt);
     * }
     * }
     */

    public void reset(int cols, int rows, int width, int height) {
        this.animals.clear();
        this.time = 0;
        RegionManager newRM = new RegionManager(cols, rows, width, height); // Que hago con este nuevo region manager?
                                                                            // Crep que tengo que quitar final a la
                                                                            // variable de la clase

        for (EcoSysObserver o : observers) {
            o.onReset(time, newRM, Collections.unmodifiableList(animals));
        }
    }

    @Override
    public void addObserver(EcoSysObserver o) {
        if (!observers.contains(o)) {
            observers.add(o);
            o.onRegister(time, regionManager, Collections.unmodifiableList(animals)); // Hago lo de inmodificable por
                                                                                      // quer como es AnimalInfo lo que
                                                                                      // le paso por que no quiero que
                                                                                      // se modifique nada, lo pongo
                                                                                      // inmodificable mi lista de
                                                                                      // animales
        }
    }

    @Override
    public void removeObserver(EcoSysObserver o) {
        observers.remove(o);
    }

    /*
     * public void settRegionManager(RegionManager rM){
     * this.regionManager = rM;
     * }
     */
}
