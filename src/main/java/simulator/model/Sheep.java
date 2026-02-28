package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

    private static final String SHEEP_GENETIC_CODE = "Sheep";
    private static final double INIT_SIGHT_SHEEP = 40.0;
    private static final double INIT_SPEED_SHEEP = 35.0;
    private static final double BOOST_FACTOR_SHEEP = 2.0;
    private static final double MAX_AGE_SHEEP = 8.0;
    private static final double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
    private static final double FOOD_DROP_RATE_SHEEP = 20.0;
    private static final double DESIRE_THRESHOLD_SHEEP = 65.0;
    private static final double DESIRE_INCREASE_RATE_SHEEP = 40.0;
    private static final double PREGNANT_PROBABILITY_SHEEP = 0.9;

    private Animal dangerSource;
    private SelectionStrategy dangerStrategy;

    // Primera constructora
    public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos) {
        super(SHEEP_GENETIC_CODE, Diet.HERVIVORO, INIT_SIGHT_SHEEP, INIT_SPEED_SHEEP, mateStrategy, pos);
        this.dangerStrategy = dangerStrategy;
        this.dangerSource = null;
    }

    // Constructora para el nacimineto del bebe tipo sheep
    protected Sheep(Sheep p1, Animal p2) {
        super(p1, p2);
        this.dangerStrategy = p1.dangerStrategy;
        this.dangerSource = null;
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
                case DANGER:
                    actionDanger(dt);
                    break;
                default:
                    break;
            }
            int col = (int) Math.floor(getPosition().getX() / regionMngr.getRegionWidth());
            int fila = (int) Math.floor(getPosition().getY() / regionMngr.getRegionHeight());
            if (!regionMngr.intToMatrix(fila, col)) {
                ajustarPos();
                this.setNormalStateAction();
            }
            if ((this.getEnergy() == 0.0) || (this.getAge() > MAX_AGE_SHEEP)) {
                this.setDeadStateAction();
            }
            if (!this.state.equals(State.DEAD)) {
                double foot = this.regionMngr.getfood(this, dt);
                energy = Utils.constrainValueInRange(energy + foot, 0.0, MAX_ENERGY );
            }
        }
    }

    ///////////////////////////////////////////////////// FUNCIONES AUXILIARES

    private List<Animal> getDangerousAnimals() {
        return regionMngr.getAnimalsInRange(this,
                a -> a.getState() != State.DEAD && a.getDiet() == Diet.CARNIVORO);
    }

    ////////////////////////////// NORMAL

    public void actionNormal(double dt) {
        advanceAnimalNormal(dt);
        changeStateNormal();
    }

    @Override
    protected void setNormalStateAction() {
        this.state = State.NORMAL;
        this.dangerSource = null;
        mateTarget = null;
    }

    private void changeStateNormal() {
        if (this.dangerSource == null) {
            this.dangerSource = this.dangerStrategy.select(this, getDangerousAnimals());
        }
        if (this.dangerSource != null) {
            setDangerStateAction();
        } else if (this.desire > DESIRE_THRESHOLD_SHEEP)
            setMateStateAction();
    }

    private void advanceAnimalNormal(double dt) {
        if (pos.distanceTo(dest) < COLLISION_RANGE )
            randomDestination();
        this.move(speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR ));
        age += dt;
        energy = Utils.constrainValueInRange(energy - FOOD_DROP_RATE_SHEEP * dt, 0.0, MAX_ENERGY );
        desire = Utils.constrainValueInRange(desire + DESIRE_INCREASE_RATE_SHEEP  * dt, 0.0, MAX_DESIRE );

    }

    ////////////////////////////// MATE

    public void actionMate(double dt) {
        // Cuidado con este if
        if ((mateTarget != null && (mateTarget.getState().equals(State.DEAD))
                || getPosition().distanceTo(mateTarget.getPosition()) >= this.getSightRange())) {
            mateTarget = null;
        }
        if (this.mateTarget == null) {
            mateTarget = mateStrategy.select(this, getMateAnimals());
            if (mateTarget == null)
                advanceAnimalNormal(dt);
        } else {
            advanceAnimalMate(dt);
        }
        if (this.dangerSource == null) {
            this.dangerSource = this.dangerStrategy.select(this, getDangerousAnimals());
        }
        if (this.dangerSource != null) {
            setDangerStateAction();
        } else if (this.dangerSource == null && desire < DESIRE_THRESHOLD_SHEEP) {
            setNormalStateAction();
        }

    }

    @Override
    protected void setMateStateAction() {
        this.state = State.MATE;
        this.dangerSource = null;
    }

    private void advanceAnimalMate(double dt) {
        this.dest = mateTarget.getPosition();
        move(BOOST_FACTOR_SHEEP * speed * dt * Math.exp((this.energy - 100.0) * HUNGER_DECAY_EXP_FACTOR));
        age += dt;
        energy -= FOOD_DROP_RATE_SHEEP  * FOOD_DROP_BOOST_FACTOR_SHEEP * dt;
        if (this.getPosition().distanceTo(mateTarget.getPosition()) < COLLISION_RANGE) {
            this.setDesire(0.0);
            mateTarget.setDesire(0);
            if (!isPregnant() && Utils.RAND.nextDouble() < PREGNANT_PROBABILITY_SHEEP)
                this.setBaby(new Sheep(this, mateTarget));
            mateTarget = null;
        }
    }
    ////////////////////////////// HUNGER

    @Override
    protected void setHungerStateAction() {
    }

    ////////////////////////////// DANGER

    public void actionDanger(double dt) {
        if (this.dangerSource != null && this.dangerSource.getState().equals(State.DEAD)) {
            this.dangerSource = null;
        }
        if (this.dangerSource == null) {
            advanceAnimalNormal(dt);
        } else {
            advanceAnimalDanger(dt);
            ChangeStateDanger();
        }
    }

    @Override
    protected void setDangerStateAction() {
        this.state = State.DANGER;
        mateTarget = null;
    }

    // Metodo para hacer el paso tres para el estado DANGER.
    private void ChangeStateDanger() {
        if (this.dangerSource == null
                || (this.getPosition().distanceTo(this.dangerSource.getPosition())) >= this.getSightRange()) {
            this.dangerSource = this.dangerStrategy.select(this, getDangerousAnimals());
            // Esto lo que hace es que busca un nuevo animal que se considere peligroso y lo
            // elige segun la estrategia
        }
        if (this.dangerSource == null) {
            if (this.desire < DESIRE_THRESHOLD_SHEEP) {
                setNormalStateAction();
            } else {
                setMateStateAction();
            }
        }
    }

    private void advanceAnimalDanger(double dt) {
        double form = BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - 100.0) * HUNGER_DECAY_EXP_FACTOR );
        this.setDest(this.getPosition().plus(pos.minus(dangerSource.getPosition()).direction()));
        this.move(form);
        this.age += dt;
        energy = Utils.constrainValueInRange(energy - FOOD_DROP_RATE_SHEEP * FOOD_DROP_BOOST_FACTOR_SHEEP * dt, 0.0, MAX_ENERGY);
        desire = Utils.constrainValueInRange(desire + 40.0 * dt, 0.0, MAX_DESIRE);
    }

    //////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////// DEAD
    @Override
    protected void setDeadStateAction() {
        this.state = State.DEAD;
    }
}
