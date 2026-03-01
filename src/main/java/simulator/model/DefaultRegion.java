package simulator.model;

public class DefaultRegion extends Region {

    @Override
    public double getfood(AnimalInfo a, double dt) {
        if (a == null) {
            throw new NullPointerException("AnimalInfo is null");
        }
        if (!a.getDiet().equals(Diet.CARNIVORO) && !a.getDiet().equals(Diet.HERBIVORO)) {
            throw new IllegalArgumentException("El animal tiene una dieta no correcta o nula.");
        } else {
            double food = 0.0;
            int n = super.busquedaHervivoros();
            if (!a.getDiet().equals(Diet.CARNIVORO)) {
                food = FOOD_EAT_RATE_HERBS
                        * Math.exp(-Math.max(0, n - FOOD_SHORTAGE_TH_HERBS) * FOOD_SHORTAGE_EXP_HERBS) * dt;
            }
            return food;
        }
    }

    @Override
    public void update(double dt) {
    }
}
