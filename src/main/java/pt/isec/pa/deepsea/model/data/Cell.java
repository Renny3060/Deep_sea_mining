package pt.isec.pa.deepsea.model.data;

import java.io.Serializable;

public abstract class Cell implements Serializable {
    private static int count = 0;
    private final int id;
    private final String typeName;

    public Cell(String typeName) {
        this.id = ++count; // ID único para cada célula/elemento
        this.typeName = typeName;
    }

    public int getId() { return id; }
    public String getTypeName() { return typeName; }

    @Override
    public String toString() {
        return typeName + " (ID: " + id + ")";
    }
}
