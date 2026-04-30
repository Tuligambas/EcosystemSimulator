package simulator.model;

public enum State {
    NORMAL, MATE, HUNGER, DANGER, DEAD;

    public State StringToEnum(String estado) {
        State state = null;
        switch (estado) {
            case "NORMAL":
                state = NORMAL;
                break;
            case "MATE":
                state = MATE;
                break;
            case "HUNGER":
                state = HUNGER;
                break;
            case "DANGER":
                state = DANGER;
                break;
            case "DEAD":
                state = DEAD;
                break;
            default:
                state = null;
                break;
        }
        return state;
    }
}
