package simulator.model;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

    // Atributos que usaran los tipos de animales que heredaran de esta clase Animal
    protected String geneticCode; // cadena de caracteres que sirve para saber si dos animales pden emparejarse
    protected Diet diet; // Es el enumerado de la clase dieta
    protected State state;
    protected Vector2D pos;
    protected Vector2D dest;
    protected double energy; // Si llega a 0.0, el animal muere.
    protected double speed;
    protected double age; // Si llega a un maximo (dependiendo del animal), ese animal muere.
    protected double desire; // Deseo del animal. Sirve para decidir si el animal sale o entra de un estado
                             // de emparejamiento.
    protected double sightRange; // Radio de vision del animal, que sirve para decidir que animales puede ver.
    protected Animal mateTarget; // Referencia a un animal con el que quiere emparejarse.
    protected Animal baby; // Referencia que indica si el animal lleva un bebe que NO ha nacido aún.
    protected AnimalMapView regionMngr;
    protected SelectionStrategy mateStrategy; // Estrategia de seleccion para buscar pareja.

    // Primera constructora, del animal en general.
    protected Animal(String geneticCode, Diet diet, double sightRange, double initSpeed, SelectionStrategy mateStrategy,
            Vector2D pos) {
        if (!geneticCode.isEmpty() && sightRange > 0 && initSpeed > 0 && mateStrategy != null) {
            this.geneticCode = geneticCode;
            this.diet = diet;
            this.sightRange = sightRange;
            this.speed = Utils.getRandomizedParameter(initSpeed, 0.1);
            this.mateStrategy = mateStrategy;
            this.pos = pos;
            this.state = State.NORMAL;
            this.energy = 100.0;
            this.desire = 0.0;
            this.dest = null;
            this.mateTarget = null;
            this.baby = null;
            this.regionMngr = null;
        } else {
            throw new IllegalArgumentException("Los valores correspondientes al animal no son correctos");
        }
    }

    // Segunda constructora, para la de los bebes.
    protected Animal(Animal p1, Animal p2) {
        this.dest = null;
        this.baby = null;
        this.mateTarget = null;
        this.regionMngr = null;
        this.state = State.NORMAL;
        this.desire = 0.0;
        this.geneticCode = p1.geneticCode;
        this.diet = p1.diet;
        this.mateStrategy = p2.mateStrategy;
        this.energy = (p1.energy + p2.energy) / 2;
        this.pos = p1.getPosition().plus(Vector2D.getRandomVector(-1, 1).scale(60.0 * (Utils.RAND.nextGaussian() + 1)));
        this.sightRange = Utils.getRandomizedParameter((p1.getSightRange() + p2.getSightRange()) / 2, 0.2);
        this.speed = Utils.getRandomizedParameter((p1.getSpeed() + p2.getSpeed()) / 2, 0.2);
    }

    // Metodo que devuelve la estructura JSON.
    public JSONObject asJSON() {
        JSONObject j = new JSONObject();
        JSONArray poss = new JSONArray();
        poss.put(this.pos.getX());
        poss.put(this.pos.getY());
        j.put("pos", poss);
        j.put("gcode", this.geneticCode);
        j.put("diet", this.diet);
        j.put("state", this.state);
        return j;
    }

    // @Override
    // public void update(double dt) {}

    protected void ajustarPos() {
        double x = pos.getX();
        double y = pos.getY();
        double width = regionMngr.getWidth();
        double height = regionMngr.getHeight();

        while (x >= width) {
            x = x - width;
        }
        while (x < 0) {
            x = x + width;
        }
        while (y >= height) {
            y = y - height;
        }
        while (y < 0) {
            y = y + height;
        }

        pos = new Vector2D(x, y);
    }

    public void init(AnimalMapView regMngr) {
        this.setRegionMngr(regMngr);
        if (pos == null) {
            // NextDoubole genera numero aleatorio entre 0 y 1 y lo x por el limite que
            // indico en el parentesis.
            double x = Utils.RAND.nextDouble(regionMngr.getWidth() - 1); // Genero un numero aleatorio entre 0 y 1 y lo
            // multiplica por ese limite para (x, y)
            double y = Utils.RAND.nextDouble(regionMngr.getHeight() - 1);
            pos = new Vector2D(x, y); // Igualo mi posicion para que apunte a la nueva posicion creada.
        } else {
            ajustarPos();
        }
        // Genero la posocion aleatoria para la posicion destino.
        double xDest = Utils.RAND.nextDouble(regionMngr.getWidth() - 1); // Genero un numero aleatorio entre 0 y 1 y lo
                                                                         // multiplica por ese limite para (x, y)
        double yDest = Utils.RAND.nextDouble(regionMngr.getHeight() - 1);
        dest = new Vector2D(xDest, yDest);
    }

    public Animal deliverBaby() {
        Animal baby = this.baby;
        this.baby = null;
        return baby;
    }

    protected void move(double speed) {
        this.pos = this.pos.plus(dest.minus(pos).direction().scale(speed));
    }

    abstract protected void setNormalStateAction();

    abstract protected void setMateStateAction();

    abstract protected void setHungerStateAction();

    abstract protected void setDangerStateAction();

    abstract protected void setDeadStateAction();

    protected void setState(State state) {
        this.state = state; // cambio estado
        switch (state) {
            case NORMAL:
                setNormalStateAction();
                break;
            case MATE:
                setMateStateAction();
                break;
            case HUNGER:
                setHungerStateAction();
                break;
            case DANGER:
                setDangerStateAction();
                break;
            case DEAD:
                setDeadStateAction();
                break;
            default:
                break;
        }
    }

    // Metodo que elige una posicion aleatoria cuando en el estado normal cuando la
    // distancia entre pos y dest es menor a 8.
    protected void randomDestination() {
        double x = Utils.RAND.nextDouble(regionMngr.getWidth() - 1);
        double y = Utils.RAND.nextDouble(regionMngr.getHeight() - 1);
        dest = new Vector2D(x, y);
    }

    protected List<Animal> getMateAnimals() {
        return regionMngr.getAnimalsInRange(this,
                a -> a.getState() != State.DEAD && a.getGeneticCode().equals(getGeneticCode()));
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public Vector2D getPosition() {
        return this.pos;
    }

    @Override
    public String getGeneticCode() {
        return this.geneticCode;
    }

    @Override
    public Diet getDiet() {
        return this.diet;
    }

    @Override
    public double getSpeed() {
        return this.speed;
    }

    @Override
    public double getSightRange() {
        return this.sightRange;
    }

    @Override
    public double getEnergy() {
        return this.energy;
    }

    @Override
    public double getAge() {
        return this.age;
    }

    @Override
    public Vector2D getDestination() {
        return this.dest;
    }

    @Override
    public boolean isPregnant() {
        return this.baby != null;
    }

    public void setGeneticCode(String geneticCode) {
        this.geneticCode = geneticCode;
    }

    public void setAnimalDiet(Diet AnimalDiet) {
        this.diet = AnimalDiet;
    }

    /*
     * public void setActualState(State ActualState) {
     * this.ActualState = ActualState;
     * }
     */

    public void setPos(Vector2D pos) {
        this.pos = pos;
    }

    public void setDest(Vector2D dest) {
        this.dest = dest;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public void setDesire(double desire) {
        this.desire = desire;
    }

    public void setSightRange(double sightRange) {
        this.sightRange = sightRange;
    }

    public void setMateTarget(Animal mateTarget) {
        this.mateTarget = mateTarget;
    }

    public void setBaby(Animal baby) {
        this.baby = baby;
    }

    public void setRegionMngr(AnimalMapView regionMngr) {
        this.regionMngr = regionMngr;
    }

    public void setMateStrategy(SelectionStrategy mateStrategy) {
        this.mateStrategy = mateStrategy;
    }
}
