package pt.isec.pa.deepsea.model.data;

import java.io.Serializable;
import java.util.Random;

public class SeaTrench implements Serializable {
    private Cell[] grid; // Array 1D para cumprir a regra de apenas um array 2D no projeto
    private final int rows = Constants.FOSSO_P;
    private final int cols = Constants.FOSSO_L;

    public SeaTrench() {
        grid = new Cell[rows * cols];
        fillWithWater();
        if (Constants.MODO_DEFESA) generateDefenceMode();
        else generateRocks();
    }

    private void fillWithWater() {
        for (int i = 0; i < grid.length; i++) grid[i] = new Water();
    }

    // ── Consulta de célula ────────────────────────────────────────

    public Cell getCell(int r, int c) {
        int idx = r * cols + c;
        // Animais e correntes gerados apenas quando se acede ao local
        if (grid[idx] instanceof Water && !Constants.MODO_DEFESA) {
            Random rand = new Random();
            double chance = rand.nextDouble();
            if (chance < Constants.PROB_ANIMAL) grid[idx] = new SeaAnimal();
            else if (chance < Constants.PROB_ANIMAL + Constants.PROB_CORRENTE) grid[idx] = new Current();
        }
        return grid[idx];
    }

    // ── Métodos de tipo (evitam instanceof de fora do package) ────

    // true se a célula é uma rocha — bloqueia o movimento do drone.
    public boolean isRock(int r, int c) {
        return grid[r * cols + c] instanceof Rock;
    }

    // true se a célula é um animal marinho — causa dano no casco.
    public boolean isAnimal(int r, int c) {
        return getCell(r, c) instanceof SeaAnimal;
    }

    // true se a célula é uma corrente — dobra o consumo de combustível.
    public boolean isCurrent(int r, int c) {
        return getCell(r, c) instanceof Current;
    }

    // ── Ações ─────────────────────────────────────────────────────

    // Remove uma ameaça temporária (Animal ou Corrente) da célula,
    // transformando-a em água limpa.
    public void limparCaminho(int r, int c) {
        int idx = r * cols + c;
        if (grid[idx] instanceof SeaAnimal || grid[idx] instanceof Current)
            grid[idx] = new Water();
    }

    //Impede que a UI ou outras classes fora do package
    // alterem a estrutura do mapa
    protected void setCell(int r, int c, Cell cell) {
        grid[r * cols + c] = cell;
    }

    // ── Geração ───────────────────────────────────────────────────

    private void generateRocks() {
        Random rand = new Random();
        int leftWall = 1, rightWall = 1;
        for (int i = 0; i < rows; i++) {
            // Oscilação máxima de 1 coluna face à linha anterior
            leftWall = Math.max(0, leftWall + rand.nextInt(3) - 1);
            rightWall = Math.max(0, rightWall + rand.nextInt(3) - 1);

            // Soma das rochas não excede 50% da largura
            if (leftWall + rightWall > cols / 2) { leftWall--; rightWall--; }

            for (int j = 0; j < leftWall; j++) grid[i * cols + j] = new Rock();
            for (int j = 0; j < rightWall; j++) grid[i * cols + (cols - 1 - j)] = new Rock();
        }
    }

    private void generateDefenceMode() {
        grid[(rows / 2) * cols + 0] = new Rock();
        grid[(rows / 2) * cols + (cols - 1)] = new Rock();
        grid[5 * cols + (cols / 2)] = new SeaAnimal();
        grid[15 * cols + (cols / 2)] = new Current();
    }
}