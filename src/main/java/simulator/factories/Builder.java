package simulator.factories;

import org.json.JSONObject;

public abstract class Builder<T> {
    private String typeTag; // Atributo que coincide con campo "type" de la estructura JSON
    private String desc; // descripe QUE tipos de objetos pueden ser creados por este constructor.

    public Builder(String typeTag, String desc) {
        if (typeTag == null || desc == null || typeTag.isBlank() || desc.isBlank())
            throw new IllegalArgumentException("Invalid type/desc");
        this.typeTag = typeTag;
        this.desc = desc;
    }

    public String getTypeTag() {
        return typeTag;
    }

    public JSONObject getInfo() {
        JSONObject info = new JSONObject();
        info.put("type", typeTag);
        info.put("desc", desc);
        JSONObject data = new JSONObject();
        fillInData(data);
        info.put("data", data);
        return info;
    }

    protected void fillInData(JSONObject o) {
    }

    @Override
    public String toString() {
        return desc;
    }

    protected abstract T createInstance(JSONObject data); // Crea un objeto de tipo T en caso de que data tenga toda la
                                                          // info, si no illegalArgument
}
