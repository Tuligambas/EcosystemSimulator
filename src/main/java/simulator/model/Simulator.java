package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable {
    // duroooo yo sigo leyendo las factorias
    private double time;
    private final List<Animal> animals;
    private final RegionManager regionManager;
    private final Factory<Animal> animalsFactory;
    private final Factory<Region> regionsFactory;

    public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory,
            Factory<Region> regionsFactory) {
        this.time = 0.0;
        this.animals = new ArrayList<>();
        this.regionManager = new RegionManager(cols, rows, width, height);
        this.animalsFactory = animalsFactory;
        this.regionsFactory = regionsFactory;
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
    }

    private void setRegion(int row, int col, JSONObject rJson) {
        Region r = regionsFactory.createInstance(rJson);
        setRegion(row, col, r);
    }

    // Animales
    private void addAnimal(Animal a) {
        this.animals.add(a);
        this.regionManager.registerAnimal(a);
    }

    private void addAnimal(JSONObject aJson) {
        Animal a = animalsFactory.createInstance(aJson);
        addAnimal(a);
    }

    // Consultas
    public MapInfo getMapInfo() {
        return this.regionManager;
    }

    public List<? extends AnimalInfo> getAnimals() {
        return Collections.unmodifiableList(this.animals);
    }

    public double getTime() {
        return this.time;
    }

    public void advance(double dt) {
        time += dt;
        // eliminar muertos
        for (int i = animals.size() - 1; i >= 0; i--) {
            Animal a = animals.get(i);
            if (a.getState() == State.DEAD) {
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
    }
}
