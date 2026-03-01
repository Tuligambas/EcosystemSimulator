package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

    private static final String WOLF_GENETIC_CODE = "Wolf";
    private static final double INIT_SIGHT_WOLF = 50.0;
    private static final double INIT_SPEED_WOLF = 60.0;
    private static final double BOOST_FACTOR_WOLF = 3.0;
    private static final double MAX_AGE_WOLF = 14.0;
    private static final double FOOD_THRSHOLD_WOLF = 50.0; // Nota: he mantenido tu nombre original (falta una 'E' en
                                                           // THRESHOLD, pero compilará igual si lo usas así)
    private static final double FOOD_DROP_BOOST_FACTOR_WOLF = 1.2;
    private static final double FOOD_DROP_RATE_WOLF = 18.0;
    private static final double FOOD_DROP_DESIRE_WOLF = 10.0;
    private static final double FOOD_EAT_VALUE_WOLF = 50.0;
    private static final double DESIRE_THRESHOLD_WOLF = 65.0;
    private static final double DESIRE_INCREASE_RATE_WOLF = 30.0;
    private static final double PREGNANT_PROBABILITY_WOLF = 0.9;

    private Animal huntTarget;
    private SelectionStrategy huntingStrategy;

    public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos) {
        super(WOLF_GENETIC_CODE, Diet.CARNIVORO, INIT_SIGHT_WOLF, INIT_SPEED_WOLF, mateStrategy, pos);
        this.huntingStrategy = huntingStrategy;
    }

    protected Wolf(Wolf p1, Animal p2) {
        super(p1, p2);
        this.huntingStrategy = p1.huntingStrategy;
        this.huntTarget = null;
    }

    @Override
    public void update(double dt) {
        if (!this.state.equals(State.DEAD)) {
            switch (this.state) {
                case NORMAL:
                    actionNormal(dt);
                    break;
                case MATE:
                    actionMate(dt);
                    break;
                case HUNGER:
                    actionHunger(dt);
                    break;
                default:
                    break;
            }
            int col = (int) Math.floor(getPosition().getX() / regionMngr.getRegionWidth());
            int fila = (int) Math.floor(getPosition().getY() / regionMngr.getRegionHeight());
            // si está fuera del mapa, lo recolocamos y lo llevamos a estado NORMAL
            if (!regionMngr.intToMatrix(fila, col)) {
                ajustarPos();
                setState(State.NORMAL);
            }

            if ((this.getEnergy() == 0.0) || (this.getAge() > MAX_AGE_WOLF)) {
                setState(State.DEAD);
            }

            if (this.state != State.DEAD) {
                double food = regionMngr.getfood(this, dt);
                energy = Utils.constrainValueInRange(energy + food, 0.0, MAX_ENERGY);
            }
        }
    }

    ////////////////////////////// FUNCIONES AUXILIARES
    private List<Animal> getAnimalsToEat() {
        return regionMngr.getAnimalsInRange(this,
                a -> a.getState() != State.DEAD && a.getDiet() == Diet.HERBIVORO);
    }

    ////////////////////////////// NORMAL
    private void actionNormal(double dt) {
        advanceAnimalNormal(dt);
        changeStateNormal();
    }

    private void advanceAnimalNormal(double dt) {
        if (pos.distanceTo(dest) < COLLISION_RANGE)
            randomDestination();
        this.move(speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));
        age += dt;
        energy = Utils.constrainValueInRange(energy - FOOD_DROP_RATE_WOLF * dt, 0.0, MAX_ENERGY);
        desire = Utils.constrainValueInRange(desire + DESIRE_INCREASE_RATE_WOLF * dt, 0.0, MAX_DESIRE);
    }

    private void changeStateNormal() {
        if (energy < FOOD_THRSHOLD_WOLF)
            setState(State.HUNGER);
        else if (desire > DESIRE_THRESHOLD_WOLF)
            setState(State.MATE);
    }

    @Override
    protected void setNormalStateAction() {
        huntTarget = mateTarget = null;
        this.state = State.NORMAL;
    }

    ////////////////////////////// MATE
    private void actionMate(double dt) {
        if (mateTarget != null && (mateTarget.getState() == State.DEAD ||
                getPosition().distanceTo(mateTarget.getPosition()) >= getSightRange())) {
            mateTarget = null;
        }
        // si no tiene pareja, busca una
        if (mateTarget == null)
            mateTarget = mateStrategy.select(this, getMateAnimals());

        // y si no la encuentra
        if (mateTarget == null)
            advanceAnimalNormal(dt);
        else
            advanceAnimalMate(dt);

        if (energy < FOOD_THRSHOLD_WOLF)
            setState(State.HUNGER);
        else if (desire < DESIRE_THRESHOLD_WOLF)
            setState(State.NORMAL);
    }

    private void advanceAnimalMate(double dt) {
        this.dest = mateTarget.getPosition();
        move(BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));
        age += dt;
        energy = Utils.constrainValueInRange(energy - FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt, 0.0,
                MAX_ENERGY);
        desire = Utils.constrainValueInRange(desire + DESIRE_INCREASE_RATE_WOLF * dt, 0.0, MAX_DESIRE);

        if (this.getPosition().distanceTo(mateTarget.getPosition()) < COLLISION_RANGE) {
            this.setDesire(0.0);
            mateTarget.setDesire(0);
            if (!isPregnant() && Utils.RAND.nextDouble() < PREGNANT_PROBABILITY_WOLF) {
                this.setBaby(new Wolf(this, mateTarget));
            }
            energy = Utils.constrainValueInRange(energy - FOOD_DROP_DESIRE_WOLF, 0.0, MAX_ENERGY);
            mateTarget = null;
        }
    }

    @Override
    protected void setMateStateAction() {
        huntTarget = null;
        state = State.MATE;
    }

    ////////////////////////////// HUNGER
    private void actionHunger(double dt) {
        if (huntTarget == null
                || huntTarget.getState() == State.DEAD
                || getPosition().distanceTo(huntTarget.getPosition()) >= getSightRange()) {
            huntTarget = huntingStrategy.select(this, getAnimalsToEat());
        }

        if (huntTarget == null) {
            advanceAnimalNormal(dt);
        } else {
            advanceAnimalHunger(dt);
        }
        changeStateHunger();
    }

    private void advanceAnimalHunger(double dt) {
        dest = huntTarget.getPosition();
        move(BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));
        age += dt;
        energy = Utils.constrainValueInRange(energy - FOOD_DROP_RATE_WOLF * FOOD_DROP_BOOST_FACTOR_WOLF * dt, 0.0,
                MAX_ENERGY);
        desire = Utils.constrainValueInRange(desire + DESIRE_INCREASE_RATE_WOLF * dt, 0.0, MAX_DESIRE);

        if (pos.distanceTo(huntTarget.getPosition()) < COLLISION_RANGE) {
            hunt();
        }
    }

    private void changeStateHunger() {
        if (energy > FOOD_THRSHOLD_WOLF) {
            if (desire < DESIRE_THRESHOLD_WOLF)
                setState(State.NORMAL);
            else
                setState(State.MATE);
        }
    }

    private void hunt() {
        huntTarget.setState(State.DEAD);
        huntTarget = null;
        energy = Utils.constrainValueInRange(energy + FOOD_EAT_VALUE_WOLF, 0.0, MAX_ENERGY);
    }

    @Override
    protected void setHungerStateAction() {
        mateTarget = null;
        this.state = State.HUNGER;
    }

    ////////////////////////////// DANGER
    @Override
    protected void setDangerStateAction() {
    }

    ////////////////////////////// DEAD
    @Override
    protected void setDeadStateAction() {
        state = State.DEAD;
    }
}
