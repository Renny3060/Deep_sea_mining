package pt.isec.pa.deepsea.model.data;

import java.io.Serializable;
import java.util.*;

public class Ship implements Serializable {

    private double fuel;
    private double minerals;
    private List<Artifact> artifacts;
    private Set<Drone> drones;
    private int posX, posY;

    public Ship() {
        // Vai buscar as regras das contantes
        Constants c = new Constants();

        this.fuel = c.SHIP_FUEL_MAX; // Depósito cheio
        this.minerals = 0.0;              // Sem minérios

        // Inicializamos as coleções
        this.artifacts = new ArrayList<>(); // Lista para a ordem em que os itens chegam
        this.drones = new HashSet<>();      // Set para o conjunto de drones

        // Criar os 3 drones e coloca os no navio
        for (int i = 0; i < 3; i++) drones.add(new Drone());
        // Posição inicial do navio
        this.posX = c.SURFACE_L / 2;
        this.posY = c.SURFACE_C / 2;
    }

    // ── Getters originais ─────────────────────────────────────────
    public double getFuel() { return fuel; }
    public double getMinerals() { return minerals; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }

    // posX é a linha na grelha de superfície.
    public int getRow() { return posX; }
    // posY é a coluna na grelha de superfície.
    public int getCol() { return posY; }

    public List<Artifact> getArtifacts() {
        return Collections.unmodifiableList(artifacts);
    }

    public Set<Drone> getDrones() {
        return Collections.unmodifiableSet(drones);
    }

    // ── Movimento ─────────────────────────────────────────────────

    //Move o navio na superfície com base num deslocamento.
    // true se o movimento foi bem sucedido, false se for inválido.
    public boolean mover(int deltaX, int deltaY) {
        int novaX = posX + deltaX;
        int novaY = posY + deltaY;

        // Verifica se a nova posição está dentro dos limites
        if (novaX >= 0 && novaX < Constants.SURFACE_L &&
                novaY >= 0 && novaY < Constants.SURFACE_C) {

            this.posX = novaX;
            this.posY = novaY;
            return true;
        }

        // Se chegar aqui, o movimento era para fora do mapa
        return false;
    }

    // ── Novos métodos chamados por DeepSeaMining ──────────────────

    /** Reduz combustível. Nunca fica negativo. */
    public void consumirCombustivel(double quantidade) {
        this.fuel = Math.max(0.0, this.fuel - quantidade);
    }

    /** Adiciona artefacto ao portfólio do navio. */
    public void adicionarArtefacto(Artifact a) {
        if (a != null) artifacts.add(a);
    }

    /** Adiciona minérios ao total do navio. */
    public void adicionarMinerais(double quantidade) {
        this.minerals += quantidade;
    }

    // ── Ordenações auxiliares (originais) ────────────────────────
    public List<Drone> getDronesSortedByID() {
        List<Drone> list = new ArrayList<>(drones);
        list.sort(Comparator.comparingInt(Drone::getId));
        return list;
    }

    public List<Drone> getDronesByFuel() {
        List<Drone> list = new ArrayList<>(drones);
        list.sort((a, b) -> Double.compare(b.getFuel(), a.getFuel()));
        return list;
    }

    public List<Drone> getDronesByHull() {
        List<Drone> list = new ArrayList<>(drones);
        list.sort((a, b) -> Double.compare(b.getHull(), a.getHull()));
        return list;
    }
}