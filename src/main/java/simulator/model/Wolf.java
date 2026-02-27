package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {
    private Animal huntTarget;
    private SelectionStrategy huntingStrategy;

    public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos) {
        super("Wolf", Diet.CARNIVORO, 50, 60, mateStrategy, pos);
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
            if (regionMngr.intToMatrix(fila, col)) { // compruebo que no este fuera del mapa
                ajustarPos();
                setState(State.NORMAL);
            }

            if ((this.getEnergy() == 0.0) || (this.getAge() > 14.0)) {
                setState(State.DEAD);
            }

            if (this.state != State.DEAD) {
                double food = regionMngr.getfood(this, dt);
                energy = Utils.constrainValueInRange(energy + food, 0.0, 100.0);
            }
        }
    }

    ////////////////////////////// FUNCIONES AUXILIARES
    private List<Animal> getAnimalsToEat() {
        return regionMngr.getAnimalsInRange(this,
                a -> a.getState() != State.DEAD && a.getDiet() == Diet.HERVIVORO);
    }

    ////////////////////////////// NORMAL
    @Override
    protected void setNormalStateAction() {
        huntTarget = mateTarget = null;
        this.state = State.NORMAL;
    }

    private void actionNormal(double dt) {
        advanceAnimalNormal(dt);
        changeStateNormal();
    }

    private void advanceAnimalNormal(double dt) {
        if (pos.distanceTo(dest) < 8)
            randomDestination();
        this.move(speed * dt * Math.exp((energy - 100.0) * 0.007));
        age += dt;
        energy = Utils.constrainValueInRange(energy - 18.0 * dt, 0.0, 100.0);
        desire = Utils.constrainValueInRange(desire + 30.0 * dt, 0.0, 100.0);
    }

    private void changeStateNormal() {
        if (this.energy < 50)
            setState(State.HUNGER);
        else if (desire > 65)
            setState(State.MATE);
    }

    ////////////////////////////// MATE
    private void actionMate(double dt) {
        if (mateTarget != null && (mateTarget.getState() == State.DEAD ||
                getPosition().distanceTo(mateTarget.getPosition()) >= getSightRange())) {
            mateTarget = null;
        }

        if (mateTarget == null) {
            mateTarget = mateStrategy.select(this, getMateAnimals());
            if (mateTarget == null)
                advanceAnimalNormal(dt);
            else {
                advanceAnimalMate(dt);
            }
        }
        if (energy < 50)
            setState(State.HUNGER);
        else if (desire < 65)
            setState(State.NORMAL);
    }

    @Override
    protected void setMateStateAction() {
        huntTarget = null;
        state = State.MATE;
    }

    private void advanceAnimalMate(double dt) {
        this.dest = mateTarget.getPosition();
        move(3.0 * speed * dt * Math.exp((energy - 100.0) * 0.007));
        age += dt;
        energy = Utils.constrainValueInRange(energy - 18.0 * 1.2 * dt, 0.0, 100.0);
        desire = Utils.constrainValueInRange(desire + 30.0 * dt, 0.0, 100.0);

        if (this.getPosition().distanceTo(mateTarget.getPosition()) < 8) {
            this.setDesire(0.0);
            mateTarget.setDesire(0);
            if (!isPregnant() && Utils.RAND.nextDouble() < 0.9) {
                this.setBaby(new Wolf(this, mateTarget));
            }
            energy = Utils.constrainValueInRange(energy - 10, 0.0, 100.0);
            mateTarget = null;
        }
    }

    ////////////////////////////// HUNGER
    private void actionHunger(double dt) {
        if ((huntTarget == null || (huntTarget == null && this.state == State.DEAD)
                || getPosition().distanceTo(huntTarget.getPosition()) >= this.getSightRange())) {
            huntTarget = huntingStrategy.select(this, getAnimalsToEat());
        }
        if (huntTarget == null) {
            advanceAnimalNormal(dt);
        } else {
            advanceAnimalHunger(dt);
        }
        changeStateHunger();
    }

    @Override
    protected void setHungerStateAction() {
        mateTarget = null;
        this.state = State.HUNGER;
    }

    private void advanceAnimalHunger(double dt) {
        dest = huntTarget.getPosition();
        move(3.0 * speed * dt * Math.exp((energy - 100.0) * 0.007));
        age += dt;
        energy = Utils.constrainValueInRange(energy - 18.0 * 1.2 * dt, 0.0, 100.0);
        desire = Utils.constrainValueInRange(desire + 30.0 * dt, 0.0, 100.0);

        if (pos.distanceTo(huntTarget.getPosition()) < 8) {
            hunt();
        }
    }

    private void hunt() {
        huntTarget.setState(State.DEAD);
        huntTarget = null;
        energy = Utils.constrainValueInRange(energy + 50, 0.0, 100.0);
    }

    private void changeStateHunger() {
        if (energy > 50) {
            if (desire < 65)
                setState(State.NORMAL);
            else
                setState(State.MATE);
        }
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
