package pt.isec.pa.deepsea.model.data;

public abstract class TrenchCell extends Cell {

    public TrenchCell(String r) { super(r); }

}

class Water extends TrenchCell {
    public Water() { super("W"); } //Water
}

class Rock extends TrenchCell {
    public Rock() { super("R"); } //rock
}

class SeaAnimal extends TrenchCell {
    public SeaAnimal() { super("SA"); } //Sea Animal
}

class Current extends TrenchCell {
    public Current() { super("C"); } //Current
}
