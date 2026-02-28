package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region { // Representa una region que da comida SOLO a animales herbivoros y la
                                                  // cantidad de comida puede crecer o decrecer.

    // se usan en DynamicSupplyRegion
    private final static double INIT_FOOD = 100.0;
    private final static double FACTOR = 2.0;

    private double initFood;
    private double factor;

    public DynamicSupplyRegion(double initFood, double increaseDecrease) {
        if (initFood != 0 && increaseDecrease != 0) {
            this.factor = increaseDecrease;
            this.initFood = initFood;
        } else {
            throw new IllegalArgumentException("Los valores introducidos no son correctos");
        }
    }

    @Override
    public double getfood(AnimalInfo a, double dt) {
        if (!a.getDiet().equals(Diet.CARNIVORO) && !a.getDiet().equals(Diet.HERVIVORO)) {
            throw new IllegalArgumentException("El animal tiene una dieta no correcta o nula.");
        } else {
            double newFood = 0.0;
            int n = super.busquedaHervivoros();
            if (!a.getDiet().equals(Diet.CARNIVORO)) {
                newFood = Math.min(this.initFood, FOOD_EAT_RATE_HERBS
                        * Math.exp(-Math.max(0, n - FOOD_SHORTAGE_TH_HERBS) * FOOD_SHORTAGE_EXP_HERBS) * dt);
            }
            this.initFood -= newFood;
            return newFood;
        }
    }

    @Override
    public void update(double dt) {
        double random = Utils.RAND.nextDouble();
        if (random <= 0.5) {
            this.initFood += dt * this.factor;
        }
    }

    public double getInitFood() {
        return this.initFood;
    }

    public double getIncreaseDecrease() {
        return this.factor;
    }

    public void setInitFood(double initFood) {
        this.initFood = initFood;
    }

    public void setIncreaseDecrease(double factor) {
        this.factor = factor;
    }
}
