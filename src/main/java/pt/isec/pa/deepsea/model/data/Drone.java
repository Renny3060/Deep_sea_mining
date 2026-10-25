package pt.isec.pa.deepsea.model.data;

import java.io.Serializable;
import java.util.*;

public class Drone implements Serializable{    //Atributos para o ID Único
    //Atributos para o ID Único
    private static int idCounter = 0; // Contador estático para todos os drones
    private final int id;             // ID único deste drone

    // Atributos
    private double fuel;          // Energia atual
    private double maxFuel;       // O tamanho do depósito
    private double hull;          // A vida do drone
    private double maxHull;
    private List<FloorCell> cargo; // Guarda objetos do tipo FloorCell Artifact, Mineral, etc
    private int posX, posY;       // Coordenadas
    private boolean inShip;       // true= no navio // False= fora do navio

    // Construtor do drone
    public Drone() {
        Constants c = new Constants(); // definições do jogo

        // Atribui o ID automaticamente
        this.id = ++idCounter;

        //Começa
        this.maxFuel = c.FUEL_INICIAL_DRONE;
        this.fuel = maxFuel;           //depósito cheio
        this.hull    = maxHull;
        this.hull = c.DRONE_HULL_MAX;        // vida 100%
        this.cargo = new ArrayList<>(); // vazio
        this.inShip = true;            // Por defeito nascem dentro do navio
    }

    // ── Getters originais ─────────────────────────────────────────
    public int getId()        { return id; }
    public double getFuel()   { return fuel; }
    public double getHull()   { return hull; }
    public double getMaxFuel(){ return maxFuel; }
    public double getMaxHull(){ return maxHull; }
    public int getPosX()      { return posX; }
    public int getPosY()      { return posY; }
    public boolean isInShip() { return inShip; }

    // true quando o casco chegou a 0 — drone permanentemente destruído.
    public boolean isDestroyed() { return hull <= 0; }

    // Retorna uma cópia imutável da carga
    //Impede que código externo altere a lista sem passar pelos métodos do Drone
    public List<FloorCell> getCargo() {
        return Collections.unmodifiableList(cargo);
    }

    // Métodos de logica SETTERS

    // Reduz o combustível baseado numa taxa de consumo
    public void aplicarConsumo(double quantidade) {
        if (inShip) return; // Não consome combustível parado no navio
        this.fuel = Math.max(0, this.fuel - quantidade);
    }


    // Aplica dano ao casco do drone
    public void sofrerDano(double dano) {
        this.hull = Math.max(0.0, this.hull - dano);
    }

    // Recupera totalmente o combustível e o casco
    // Só funciona se o drone estiver " no navio
    public void realizarManutencao() {
        if (inShip) {
            this.fuel = maxFuel;
            this.hull = maxHull;
        }
    }

    // Adiciona um item à carga do drone.
    public void recolherItem(FloorCell item) {
        if (item != null) cargo.add(item);
    }

    // Métodos de controlo
    public void setInShip(boolean status) { this.inShip = status; }

    public void atualizarPosicao(int x, int y) {
        this.posX = x;
        this.posY = y;
    }

    // ── Novos métodos chamados por DeepSeaMining ──────────────────

    //Recarrega combustível até ao máximo do tanque.
    public void recarregarCombustivel(double quantidade) {
        this.fuel = Math.min(maxFuel, this.fuel + quantidade);
    }

    // Restaura o casco ao máximo atual.
    public void repararCasco() { this.hull = maxHull; }

    // Upgrade: aumenta capacidade do tanque.
    public void melhorarTanque() {
        this.maxFuel += Constants.UPGRADE_TANK_AMOUNT;
    }

    // Upgrade: aumenta a blindagem máxima e o casco atual.
    public void melhorarBlindagem() {
        this.maxHull += Constants.UPGRADE_ARMOR_AMOUNT;
        this.hull = Math.min(this.hull + Constants.UPGRADE_ARMOR_AMOUNT, this.maxHull);
    }

    // Esvazia a carga após entrega ou perda do drone.
    public void limparCarga() { cargo.clear(); }

    // ── Reset de fase ─────────────────────────────────
    public void resetParaDescerFosso() {
        Constants c = new Constants();
        this.inShip = false;
        this.posX = 0;
        this.posY = c.FOSSO_L / 2;
    }

    public void resetParaFundo() {
        Constants c = new Constants();
        this.inShip = false;
        this.posX = c.FUNDO_DIM / 2;
        this.posY = c.FUNDO_DIM / 2;
    }

    @Override
    public String toString() {
        return String.format("Drone #%d [%.1f%% Fuel | %.1f%% Hull] - InShip: %b",
                id, (fuel/maxFuel)*100, hull, inShip);
    }
}