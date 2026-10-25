package pt.isec.pa.deepsea.model.data;

public abstract class FloorCell extends Cell {
    private boolean revealed; // informação sobre se a célula foi revelada

    public FloorCell(String name) {
        super(name);
        this.revealed = false;
    }

    public boolean isRevealed() { return revealed; }
    public void setRevealed(boolean revealed) { this.revealed = revealed; }
}

class EmptyFloor extends FloorCell {
    public EmptyFloor() { super("E"); } //Empty
}

class Monster extends FloorCell {
    public Monster() { super("MO"); } //Mosnters
}

// --- Minerias ---
class Mineral extends FloorCell {
    private final double amount; //quantidade de minérios pode ter valores diferentes

    public Mineral(double amount) {
        super("MI");
        this.amount = amount;
    }

    public double getAmount() { return amount; }
}