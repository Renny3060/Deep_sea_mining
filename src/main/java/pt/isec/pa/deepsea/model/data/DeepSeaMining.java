package pt.isec.pa.deepsea.model;

import pt.isec.pa.deepsea.model.data.*;
import java.io.Serializable;
import java.util.List;

/**
 * Modelo principal do jogo Deep Sea Mining.
 *
 * Todas as constantes usam a classe Constants existente.
 * Enums internos: MoveResult (ExploreState) e PuzzleResult (PuzzleState).
 */
public class DeepSeaMining implements Serializable {

    // ── Enums internos ────────────────────────────────────────────
    public enum MoveResult {
        MOVED_EMPTY, MOVED_MINERAL, MOVED_MONSTER, MOVED_ARTIFACT, FUEL_EMPTY, BLOCKED
    }

    public enum PuzzleResult {
        MOVED, SOLVED, MOVES_EXHAUSTED, INVALID_MOVE
    }

    // ── Campos ────────────────────────────────────────────────────
    private Ship ship;
    private MiniGame miniGame;
    private boolean gameTerminated;
    private Drone activeDrone;
    private int artifactsCollected;


    // Grelha da superfície usando AreaExploracao em vez de CellType
    // Este é o array bidimensional
    private AreaExploracao[][] surfaceGrid;

    // ── Construtor ────────────────────────────────────────────────
    public DeepSeaMining() {
        // Inicialização de todos os componentes
        this.ship = new Ship();
        this.miniGame = new MiniGame();
        this.gameTerminated = false;
        this.activeDrone        = null;
        this.artifactsCollected = 0;
        // Inicializa a grelha de superfície com as dimensões das constantes
        this.surfaceGrid = new AreaExploracao[Constants.SURFACE_L][Constants.SURFACE_C];
        setupSurface();
    }

    // Método auxiliar para preparar a superfície
    private void setupSurface() {
        for (int i = 0; i < Constants.SURFACE_L; i++)
            for (int j = 0; j < Constants.SURFACE_C; j++)
                surfaceGrid[i][j] = new AreaExploracao();
        if (Constants.MODO_DEFESA) setupDefenceMode();
    }
    // Configura artefactos fixos para testes no Modo Defesa.
    private void setupDefenceMode() {
        int midL = Constants.SURFACE_L / 2;
        int midC = Constants.SURFACE_C / 2;
        // Coloca um artefacto no fundo da área central
        surfaceGrid[midL][midC].getFundo().configurarCell(0, 0, new Artifact());
    }

    // getters
    public Ship getShip() { return ship; }
    public MiniGame getMiniGame() { return miniGame; }
    public boolean isGameTerminated() { return gameTerminated; }
    public void     setGameTerminated(boolean v) { this.gameTerminated = v; }

    //Retorna uma área específica
    //Protege o array bidimensional de acesso direto
    public AreaExploracao getArea(int r, int c) {
        if (r >= 0 && r < Constants.SURFACE_L && c >= 0 && c < Constants.SURFACE_C)
            return surfaceGrid[r][c];
        return null;
    }

    public List<Drone> getDronesList() { return ship.getDronesSortedByID(); }

    // ── Helpers privados ──────────────────────────────────────────
    private AreaExploracao currentArea()   { return surfaceGrid[ship.getRow()][ship.getCol()]; }
    private SeaTrench      currentTrench() { return currentArea().getFosso(); }
    private SeaFloor       currentFloor()  { return currentArea().getFundo(); }

    // ══════════════════════════════════════════════════════════════
    //  IDLE_STATE
    // ══════════════════════════════════════════════════════════════

    public boolean moveShip(int dx, int dy) {
        if (gameTerminated) return false;
        boolean ok = ship.mover(dx, dy);
        if (ok) ship.consumirCombustivel(Constants.FUEL_SHIP_MOVE);
        return ok;
    }

    public boolean refuelDrone(int droneId, int volume) {
        for (Drone d : ship.getDrones()) {
            if (d.getId() == droneId && d.isInShip() && !d.isDestroyed()) {
                double transfer = Math.min(volume, ship.getFuel());
                if (transfer <= 0) return false;
                d.recarregarCombustivel(transfer);
                ship.consumirCombustivel(transfer);
                return true;
            }
        }
        return false;
    }

    public boolean repairDrone(int droneId) {
        if (ship.getFuel() < Constants.FUEL_REPAIR_COST) return false;
        for (Drone d : ship.getDrones()) {
            if (d.getId() == droneId && d.isInShip() && !d.isDestroyed()) {
                d.repararCasco();
                ship.consumirCombustivel(Constants.FUEL_REPAIR_COST);
                return true;
            }
        }
        return false;
    }

    public boolean upgradeDrone(int droneId, String type) {
        for (Drone d : ship.getDrones()) {
            if (d.getId() == droneId && d.isInShip() && !d.isDestroyed()) {
                if   ("TANK".equalsIgnoreCase(type))   d.melhorarTanque();
                else if ("ARMOR".equalsIgnoreCase(type)) d.melhorarBlindagem();
                else return false;
                return true;
            }
        }
        return false;
    }

    public boolean launchDrone(int droneId) {
        for (Drone d : ship.getDrones()) {
            if (d.getId() == droneId && d.isInShip() && !d.isDestroyed() && d.getFuel() > 0) {
                activeDrone = d;
                d.resetParaDescerFosso();
                return true;
            }
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════
    //  DESCEND / ASCEND — fosso
    //  Usa SeaTrench.isRock/isAnimal/isCurrent para evitar instanceof
    //  de fora do package
    // ══════════════════════════════════════════════════════════════

    public boolean moveDroneLateral(int dx) {
        if (activeDrone == null) return false;
        int newCol = activeDrone.getPosY() + dx;
        int row    = activeDrone.getPosX();
        if (newCol < 0 || newCol >= Constants.FOSSO_L) return false;

        SeaTrench trench = currentTrench();
        if (trench.isRock(row, newCol)) return false;

        double consumo = trench.isCurrent(row, newCol)
                ? Constants.FUEL_DRONE_MOVE * 2
                : Constants.FUEL_DRONE_MOVE;
        activeDrone.aplicarConsumo(consumo);

        if (trench.isAnimal(row, newCol))
            activeDrone.sofrerDano(Constants.DANO_ANIMAL);

        activeDrone.atualizarPosicao(row, newCol);
        return true;
    }

    public boolean startAscent(int droneId) {
        return activeDrone != null && activeDrone.getId() == droneId;
    }

    // ══════════════════════════════════════════════════════════════
    //  EXPLORE_STATE — fundo do mar
    //  Usa SeaFloor.isMineral/isArtifact/isMonster
    // ══════════════════════════════════════════════════════════════

    public MoveResult moveDroneSeabed(int dx, int dy) {
        if (activeDrone == null)        return MoveResult.BLOCKED;
        if (activeDrone.getFuel() <= 0) return MoveResult.FUEL_EMPTY;

        int newRow = activeDrone.getPosX() + dx;
        int newCol = activeDrone.getPosY() + dy;

        if (newRow < 0 || newRow >= Constants.FUNDO_DIM ||
                newCol < 0 || newCol >= Constants.FUNDO_DIM)
            return MoveResult.BLOCKED;

        activeDrone.aplicarConsumo(Constants.FUEL_DRONE_MOVE);
        activeDrone.atualizarPosicao(newRow, newCol);

        if (activeDrone.getFuel() <= 0) return MoveResult.FUEL_EMPTY;

        SeaFloor floor = currentFloor();
        floor.getCell(newRow, newCol).setRevealed(true);

        if (floor.isMonster(newRow, newCol)) {
            activeDrone.sofrerDano(Constants.DANO_MONSTRO);
            if (activeDrone.isDestroyed()) return MoveResult.FUEL_EMPTY;
            return MoveResult.MOVED_MONSTER;
        }
        if (floor.isMineral(newRow, newCol))  return MoveResult.MOVED_MINERAL;
        if (floor.isArtifact(newRow, newCol)) return MoveResult.MOVED_ARTIFACT;
        return MoveResult.MOVED_EMPTY;
    }

    public boolean collectMineral() {
        if (activeDrone == null) return false;
        int r = activeDrone.getPosX(), c = activeDrone.getPosY();
        double amount = currentFloor().getMineralAmount(r, c);
        if (amount <= 0) return false;
        currentFloor().extrairMineralObjeto(r, c);
        activeDrone.aplicarConsumo(Constants.FUEL_MINERAL_COLLECT);
        ship.adicionarMinerais(amount);
        return true;
    }

    public boolean initMinigame() {
        if (activeDrone == null) return false;
        int r = activeDrone.getPosX(), c = activeDrone.getPosY();
        if (!currentFloor().isArtifact(r, c)) return false;
        miniGame.reset();
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  PUZZLE_STATE
    //  Usa Constants.PUZZLE_DIM e Constants.MAX_PUZZLE_MOVES (nova)
    // ══════════════════════════════════════════════════════════════

    public PuzzleResult movePuzzlePiece(int piecePosition) {
        if (piecePosition < 0 || piecePosition >= Constants.PUZZLE_DIM * Constants.PUZZLE_DIM)
            return PuzzleResult.INVALID_MOVE;
        int row = piecePosition / Constants.PUZZLE_DIM;
        int col = piecePosition % Constants.PUZZLE_DIM;
        if (!miniGame.move(row, col))          return PuzzleResult.INVALID_MOVE;
        if (miniGame.isSolved())               return PuzzleResult.SOLVED;
        if (miniGame.getRemainingMoves() <= 0) return PuzzleResult.MOVES_EXHAUSTED;
        return PuzzleResult.MOVED;
    }

    /** Usa Constants.NUM_ARTEFACTOS — não precisa de TOTAL_ARTIFACTS. */
    public boolean allArtifactsCollected() {
        return artifactsCollected >= Constants.NUM_ARTEFACTOS;
    }

    public int getActiveDroneId() {
        return activeDrone != null ? activeDrone.getId() : -1;
    }

    public int[][] getPuzzleBoard() {
        return miniGame.getBoard();
    }

    // ══════════════════════════════════════════════════════════════
    //  ASCEND_STATE
    // ══════════════════════════════════════════════════════════════

    public void collectDroneCargo() {
        if (activeDrone == null) return;
        for (FloorCell item : activeDrone.getCargo()) {
            if (item instanceof Artifact a) {
                ship.adicionarArtefacto(a);
                artifactsCollected++;
            }
        }
        activeDrone.limparCarga();
        activeDrone.setInShip(true);
        activeDrone = null;
    }

    // ── Gestão de perda de drone ──────────────────────────────────

    public boolean isActiveDroneLost() {
        if (activeDrone == null) return false;
        return activeDrone.getFuel() <= 0 || activeDrone.isDestroyed();
    }

    public void handleActiveDroneLost() {
        if (activeDrone == null) return;
        int r = activeDrone.getPosX(), c = activeDrone.getPosY();
        for (FloorCell item : activeDrone.getCargo()) {
            if (item instanceof Artifact)
                currentFloor().configurarCell(r, c, item);
        }
        activeDrone.limparCarga();
        activeDrone.sofrerDano(activeDrone.getHull()); // casco → 0
        activeDrone.setInShip(false);
        activeDrone = null;
    }

    /** Usa Constants.DRONES_INICIAIS para referência mas conta dinamicamente. */
    public int getAvailableDronesCount() {
        return (int) ship.getDrones().stream()
                .filter(d -> d.isInShip() && !d.isDestroyed())
                .count();
    }


}