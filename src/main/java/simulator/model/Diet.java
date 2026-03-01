package simulator.model;

public enum Diet {
    HERBIVORO, CARNIVORO;

    public Diet conviertoAenum(String dieta) {
        Diet enumerado = null;
        switch (dieta) {
            case "HERVIVORO":
                enumerado = HERBIVORO;
                break;
            case "CARNIVORO":
                enumerado = CARNIVORO;
                break;
            default:
                break;
        }
        return enumerado;
    }
}
