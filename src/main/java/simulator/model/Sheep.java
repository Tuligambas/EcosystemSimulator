package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {

    private Animal dangerSource;
    private SelectionStrategy dangerStrategy;

    // Primera constructora
    public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos) {
        super("Sheep", Diet.HERVIVORO, 40.0, 35.0, mateStrategy, pos);
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
            if ((this.getEnergy() == 0.0) || (this.getAge() > 8.0)) {
                this.setDeadStateAction();
            }
            if (!this.state.equals(State.DEAD)) {
                double foot = this.regionMngr.getfood(this, dt);
                energy = Utils.constrainValueInRange(energy + foot, 0.0, 100.0);
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
        } else if (this.desire > 65)
            setMateStateAction();
    }

    private void advanceAnimalNormal(double dt) {
        if (pos.distanceTo(dest) < 8)
            randomDestination();
        this.move(speed * dt * Math.exp((energy - 100.0) * 0.007));
        age += dt;
        energy = Utils.constrainValueInRange(energy - 20.0 * dt, 0.0, 100.0);
        desire = Utils.constrainValueInRange(desire + 40.0 * dt, 0.0, 100.0);

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
        } else if (this.dangerSource == null && desire < 65) {
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
        move(2.0 * speed * dt * Math.exp((this.energy - 100.0) * 0.007));
        age += dt;
        energy -= 20.0 * 1.2 * dt;
        if (this.getPosition().distanceTo(mateTarget.getPosition()) < 8) {
            this.setDesire(0.0);
            mateTarget.setDesire(0);
            if (!isPregnant() && Utils.RAND.nextDouble() < 0.9)
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
            if (this.desire < 65.0) {
                setNormalStateAction();
            } else {
                setMateStateAction();
            }
        }
    }

    private void advanceAnimalDanger(double dt) {
        double form = 2.0 * speed * dt * Math.exp((energy - 100.0) * 0.007);
        this.setDest(this.getPosition().plus(pos.minus(dangerSource.getPosition()).direction()));
        this.move(form);
        this.age += dt;
        energy = Utils.constrainValueInRange(energy - 20.0 * 1.2 * dt, 0.0, 100.0);
        desire = Utils.constrainValueInRange(desire + 40.0 * dt, 0.0, 100.0);
    }

    //////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////// DEAD
    @Override
    protected void setDeadStateAction() {
        this.state = State.DEAD;
    }
}
