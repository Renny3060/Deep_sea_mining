package pt.isec.pa.deepsea.model.data;

import java.io.Serializable;
import java.util.Random;

public class SeaFloor implements Serializable {
    private FloorCell[] grid; // Array 1D
    private final int dim = Constants.FUNDO_DIM;

    public SeaFloor() {
        grid = new FloorCell[dim * dim];
        for (int i = 0; i < grid.length; i++) grid[i] = new EmptyFloor();
        if (Constants.MODO_DEFESA) setupDefesaMode();
        else generateInitialElements();
    }

    private void generateInitialElements() {
        Random rand = new Random();
        for (int i = 0; i < grid.length; i++) {
            double chance = rand.nextDouble();
            // Percentagens vindas de Constants
            if (chance < Constants.PROB_MINERAL)  grid[i] = new Mineral(1.0 + rand.nextDouble() * 49.0);
            else if (chance < Constants.PROB_MINERAL + Constants.PROB_ARTIFACT) grid[i] = new Artifact();
        }
    }

    public FloorCell getCell(int r, int c) {
        int idx = r * dim + c;
        // Monstros gerados apenas quando chegam ao local
        if (grid[idx] instanceof EmptyFloor && !Constants.MODO_DEFESA) {
            if (new Random().nextDouble() < Constants.PROB_MONSTER)
                grid[idx] = new Monster();
        }
        return grid[idx];
    }

    // ── Métodos de tipo ───────────────────────────────────────────
    // Ficam DENTRO do package data — podem usar instanceof sobre classes
    // package-private (Mineral, Monster, EmptyFloor) sem problemas.
    // DeepSeaMining chama estes métodos em vez de fazer instanceof diretamente.

    // true se a célula contém um mineral.
    public boolean isMineral(int r, int c) {
        return grid[r * dim + c] instanceof Mineral;
    }

    // true se a célula contém um artefacto.
    public boolean isArtifact(int r, int c) {
        return grid[r * dim + c] instanceof Artifact;
    }

    // true se a célula contém um monstro (inclui geração lazy).
    public boolean isMonster(int r, int c) {
        return getCell(r, c) instanceof Monster;
    }


    //Devolve a quantidade do mineral na célula (ou 0.0 se não houver).
    public double getMineralAmount(int r, int c) {
        FloorCell cell = grid[r * dim + c];
        if (cell instanceof Mineral m) return m.getAmount();
        return 0.0;
    }

    // ── Ações (originais) ─────────────────────────────────────────
    public Mineral extrairMineralObjeto(int r, int c) {
        int idx = r * dim + c;
        if (grid[idx] instanceof Mineral m) {
            grid[idx] = new EmptyFloor();
            return m;
        }
        return null;
    }

    public Artifact recolherArtefacto(int r, int c) {
        int idx = r * dim + c;
        if (grid[idx] instanceof Artifact a) {
            grid[idx] = new EmptyFloor(); // Remove o artefacto do mapa
            return a;
        }
        return null;
    }

    protected void setCell(int r, int c, FloorCell cell) { grid[r * dim + c] = cell; }

    // Uso indireto do setCell
    public void configurarCell(int r, int c, FloorCell objeto) { setCell(r, c, objeto); }

    private void setupDefesaMode() {
        for (FloorCell cell : grid) cell.setRevealed(true); // Tudo visível
        setCell(2, 2, new Monster());
        setCell(5, 5, new Mineral(1.0));
        setCell(8, 8, new Mineral(100.0));
    }
}