package pt.isec.pa.deepsea.model.fsm;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.data.Constants;
import pt.isec.pa.deepsea.model.data.FloorCell;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DeepSeaContext — Contexto da FSM e Facade de acesso ao modelo.
 *
 *  - getShipMinerals()          → minérios do navio (InfoPanel)
 *  - getArtifactSurfacePositions() → heatmap do SurfaceCanvas
 *  - getTrenchCellType(r,c)     → tipo de célula do fosso (TrenchCanvas)
 *  - getDronePosition()         → posição atual do drone (TrenchCanvas)
 *  - getActiveDroneFuel/Hull()  → recursos do drone (HUD do TrenchCanvas)
 */
public class DeepSeaContext implements Serializable {

    // Modelo principal do jogo — contém todos os dados. */
    private DeepSeaMining data;

    // Estado atual da FSM — só alterado por changeState(). */
    private IDeepSeaState currentState;

    /**
     * Construtor — inicia a FSM em SETUP_STATE.
     * @param data modelo de dados criado externamente (no DeepSeaManager)
     */
    public DeepSeaContext(DeepSeaMining data) {
        this.data = data;
        // Factory Method do enum cria a instância do estado inicial
        this.currentState = DeepSeaState.SETUP_STATE.createState(this, data);
    }

    // ── Gestão interna de estados ─────────────────────────────────

    /**
     * Muda o estado atual.
     * Package-private: só o DeepSeaStateAdapter pode chamar este método.
     * Garante que nenhum código externo força transições ilegais.
     */
    void changeState(IDeepSeaState newState) { this.currentState = newState; }

    // Devolve o enum do estado atual — usado pela UI e pelo Manager.
    public DeepSeaState getState() { return currentState.getState(); }

    // ── Delegação para o estado ativo ─────────────────────────────
    // Cada método devolve false se a ação não for permitida no estado atual.
    // A lógica de "o que é permitido em cada estado" está nos estados concretos.

    public boolean moveShip(int dx, int dy)               { return currentState.moveShip(dx, dy); }
    public boolean refuelDrone(int droneId, int volume)   { return currentState.refuelDrone(droneId, volume); }
    public boolean repairDrone(int droneId)               { return currentState.repairDrone(droneId); }
    public boolean upgradeDrone(int droneId, String type) { return currentState.upgradeDrone(droneId, type); }
    public boolean descendDrone(int droneId)              { return currentState.descendDrone(droneId); }
    public boolean moveDrone(int dx)                      { return currentState.moveDrone(dx); }
    public boolean exploreSeabed()                        { return currentState.exploreSeabed(); }
    public boolean moveDrone(int dx, int dy)              { return currentState.moveDrone(dx, dy); }
    public boolean collectMineral()                       { return currentState.collectMineral(); }
    public boolean collectArtifact()                      { return currentState.collectArtifact(); }
    public boolean ascendDrone(int droneId)               { return currentState.ascendDrone(droneId); }
    public boolean movePiece(int piecePosition)           { return currentState.movePiece(piecePosition); }
    public boolean returnToShip()                         { return currentState.returnToShip(); }
    public int[][] getPuzzleBoard() {
        return data.getPuzzleBoard();
    }

    // ── Log ───────────────────────────────────────────────────────

    // Regista uma mensagem no DeepSeaLog Singleton com timestamp automático. */
    public void addLog(String message) { DeepSeaLog.getInstance().addLog(message); }

    // ── Save / Load ───────────────────────────────────────────────

    /**
     * Grava o contexto completo em ficheiro binário (serialização).
     * Toda a árvore de objetos referenciada (estados, dados, drones…) é serializada.
     */
    public boolean save(String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(this);
            addLog("Jogo gravado: " + fileName);
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao gravar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carrega um contexto previamente gravado.
     * @return contexto restaurado, ou null em caso de erro
     */
    public static DeepSeaContext load(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            DeepSeaContext ctx = (DeepSeaContext) ois.readObject();
            ctx.addLog("Jogo carregado: " + fileName);
            return ctx;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao carregar: " + e.getMessage());
            return null;
        }
    }

    // ── Gets básicos (tipos primitivos) ───────────────────────────

    // Combustível atual do navio.
    public double getShipFuel() { return data.getShip().getFuel(); }

    // Minérios acumulados no navio.
    public double getShipMinerals() { return data.getShip().getMinerals(); }

    // Número de drones operacionais.
    public int getAvailableDronesCount() {
        return (int) data.getDronesList().stream()
                .filter(d -> d.isInShip() && !d.isDestroyed())
                .count();
    }

    // Posição do navio na grelha de superfície [linha, coluna].
    public int[] getShipPosition() {
        return new int[]{ data.getShip().getRow(), data.getShip().getCol() };
    }

    // true quando todos os artefactos foram recolhidos condição de vitória.
    public boolean allArtifactsCollected() { return data.allArtifactsCollected(); }

    // true quando o jogo terminou WIN ou GAME_OVER.
    public boolean isGameOver() { return data.isGameTerminated(); }

    // ── Artefactos na superfície (para SurfaceCanvas heatmap) ─────

    /**
     * Posições [row, col] da grelha de SUPERFÍCIE onde existem artefactos
     * no fundo do mar correspondente. Usado pelo SurfaceCanvas para desenhar
     * o heatmap de calor que orienta o jogador.
     *
     * Percorre todo o surfaceGrid; para cada área verifica se alguma célula
     * do fundo tem artefacto via SeaFloor.isArtifact() (dentro do package data).
     *
     * @return lista imutável de int[]{row, col}
     */
    public List<int[]> getArtifactSurfacePositions() {
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < Constants.SURFACE_L; r++) {
            for (int c = 0; c < Constants.SURFACE_C; c++) {
                var area = data.getArea(r, c);
                if (area == null) continue;
                var floor = area.getFundo();
                boolean found = false;
                for (int fr = 0; fr < Constants.FUNDO_DIM && !found; fr++)
                    for (int fc = 0; fc < Constants.FUNDO_DIM && !found; fc++)
                        if (floor.isArtifact(fr, fc)) {
                            positions.add(new int[]{r, c});
                            found = true;
                        }
            }
        }
        return Collections.unmodifiableList(positions);
    }

    // ── Fosso (para TrenchCanvas) ─────────────────────────────────

    /**
     * Tipo de célula do fosso correspondente à posição atual do navio.
     * Devolve String para não expor classes package-private do package data.
     *
     * Valores possíveis:
     *   "ROCK"    → rocha (bloqueia o drone)
     *   "ANIMAL"  → animal marinho (causa dano)
     *   "CURRENT" → corrente (dobra o consumo de combustível)
     *   "WATER"   → água livre (célula vazia)
     *
     * @param row linha no fosso (0 = topo, FOSSO_P-1 = fundo)
     * @param col coluna no fosso (0 = esquerda, FOSSO_L-1 = direita)
     */
    public String getTrenchCellType(int row, int col) {
        int[] shipPos = getShipPosition();
        var area = data.getArea(shipPos[0], shipPos[1]);
        if (area == null) return "WATER";
        var trench = area.getFosso();
        // isRock/isAnimal/isCurrent são métodos bridge no SeaTrench
        // que encapsulam o instanceof dentro do package data
        if (trench.isRock(row, col))    return "ROCK";
        if (trench.isAnimal(row, col))  return "ANIMAL";
        if (trench.isCurrent(row, col)) return "CURRENT";
        return "WATER";
    }

    /**
     * Posição atual do drone ativo [posX, posY].
     * Devolve [-1, -1] se não há drone em missão.
     */
    public int[] getDronePosition() {
        int id = data.getActiveDroneId();
        if (id == -1) return new int[]{-1, -1};
        for (var d : data.getDronesList())
            if (d.getId() == id) return new int[]{d.getPosX(), d.getPosY()};
        return new int[]{-1, -1};
    }

    // Combustível atual do drone ativo em missão.
    public double getActiveDroneFuel() {
        int id = data.getActiveDroneId();
        if (id == -1) return 0.0;
        for (var d : data.getDronesList())
            if (d.getId() == id) return d.getFuel();
        return 0.0;
    }

    // Casco atual do drone ativo em missão.
    public double getActiveDroneHull() {
        int id = data.getActiveDroneId();
        if (id == -1) return 0.0;
        for (var d : data.getDronesList())
            if (d.getId() == id) return d.getHull();
        return 0.0;
    }

    public FloorCell getFloorCell(int shipRow, int shipCol, int floorRow, int floorCol) {
        var area = data.getArea(shipRow, shipCol);
        if (area == null) {
            return null;
        }
        // Nota: Ajusta o método abaixo consoante o nome real que está na tua classe Area/DeepSeaMining
        // Assumindo que a tua classe Area tem um método para obter o fundo/grelha:
        return area.getFundo().getCell(floorRow, floorCol);
    }
}