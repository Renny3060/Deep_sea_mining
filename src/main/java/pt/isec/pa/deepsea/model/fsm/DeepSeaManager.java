package pt.isec.pa.deepsea.model.fsm;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.data.FloorCell;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * DeepSeaManager — Facade principal do modelo de dados
 *
 */
public class DeepSeaManager {

    // ── Constantes dos eventos PropertyChange ─────────────────────
    // Usadas por todos os listeners para subscrever eventos específicos
    public static final String PROP_STATE_CHANGED = "stateChanged";
    public static final String PROP_SHIP_MOVED    = "shipMoved";
    public static final String PROP_DRONE_MOVED   = "droneMoved";
    public static final String PROP_DATA_CHANGED  = "dataChanged";
    public static final String PROP_LOG_ADDED     = "logAdded";
    public static final String PROP_GAME_OVER     = "gameOver";
    public static final String PROP_WIN           = "win";

    // ── PropertyChangeSupport ─────────────────────────────────────
    // Implementa o padrão de notificações assíncronas MVC@PA
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** Subscreve todos os eventos do manager. */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    /** Subscreve apenas um evento específico. */
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        pcs.addPropertyChangeListener(prop, l);
    }
    public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
        pcs.removePropertyChangeListener(prop, l);
    }

    // ── Contexto da FSM ───────────────────────────────────────────
    // Substituído em actionNew() quando o utilizador inicia novo jogo
    private DeepSeaContext context;

    public DeepSeaManager() {
        this.context = new DeepSeaContext(new DeepSeaMining());
    }

    // ── Helpers de notificação ────────────────────────────────────

    /**
     * Dispara PROP_STATE_CHANGED se o estado mudou.
     * Também dispara WIN ou GAME_OVER se o novo estado for terminal.
     */
    private void fireState(DeepSeaState before) {
        DeepSeaState now = context.getState();
        if (now != before) {
            pcs.firePropertyChange(PROP_STATE_CHANGED, before, now);
            if (now == DeepSeaState.WIN_STATE)
                pcs.firePropertyChange(PROP_WIN, null, null);
            if (now == DeepSeaState.GAME_OVER_STATE)
                pcs.firePropertyChange(PROP_GAME_OVER, null, null);
        }
    }

    /** Notifica que os dados gerais foram alterados (InfoPanel, WorkshopPanel redesenham). */
    private void fireData() { pcs.firePropertyChange(PROP_DATA_CHANGED, null, null); }

    /** Regista no log e notifica a UI (LogStage atualiza). */
    private void log(String msg) {
        DeepSeaLog.getInstance().addLog(msg);
        pcs.firePropertyChange(PROP_LOG_ADDED, null, msg);
    }

    // ════════════════════════════════════════════════════════════════
    //  AÇÕES — delegadas ao contexto, com registo de log e notificação
    // ════════════════════════════════════════════════════════════════

    // ── IDLE_STATE ────────────────────────────────────────────────

    /** Move o navio na grelha de superfície. Consome combustível. */
    public boolean moveShip(int dx, int dy) {
        DeepSeaState b = context.getState();
        boolean r = context.moveShip(dx, dy);
        if (r) {
            log("Navio movido.");
            // Passa a nova posição como newValue para o SurfaceCanvas redesenhar
            pcs.firePropertyChange(PROP_SHIP_MOVED, null, context.getShipPosition());
            fireData();
            fireState(b);
        }
        return r;
    }

    /** Transfere combustível do navio para o drone selecionado. */
    public boolean refuelDrone(int droneId, int volume) {
        boolean r = context.refuelDrone(droneId, volume);
        if (r) { log("Drone " + droneId + " abastecido: +" + volume + "."); fireData(); }
        return r;
    }

    /** Repara o casco do drone. Consome FUEL_REPAIR_COST do navio. */
    public boolean repairDrone(int droneId) {
        boolean r = context.repairDrone(droneId);
        if (r) { log("Drone " + droneId + " reparado."); fireData(); }
        return r;
    }

    /** Melhora o drone com minérios. type = "TANK" ou "ARMOR". */
    public boolean upgradeDrone(int droneId, String type) {
        boolean r = context.upgradeDrone(droneId, type);
        if (r) { log("Drone " + droneId + " melhorado: " + type + "."); fireData(); }
        return r;
    }

    /** Lança o drone para a descida. IDLE → DESCEND_STATE. */
    public boolean descendDrone(int droneId) {
        DeepSeaState b = context.getState();
        boolean r = context.descendDrone(droneId);
        if (r) { log("Drone " + droneId + " descendo."); fireData(); fireState(b); }
        return r;
    }

    // ── DESCEND / ASCEND ──────────────────────────────────────────

    /** Move o drone lateralmente no fosso (dx = ±1). */
    public boolean moveDroneLateral(int dx) {
        DeepSeaState b = context.getState();
        boolean r = context.moveDrone(dx);
        if (r) {
            // Passa nova posição para TrenchCanvas redesenhar o drone
            pcs.firePropertyChange(PROP_DRONE_MOVED, null, context.getDronePosition());
            fireData();
            fireState(b);
        }
        return r;
    }

    /** Drone chegou ao fundo. DESCEND → EXPLORE_STATE. */
    public boolean exploreSeabed() {
        DeepSeaState b = context.getState();
        boolean r = context.exploreSeabed();
        if (r) { log("Drone atingiu o fundo."); fireData(); fireState(b); }
        return r;
    }

    /** Drone regressou ao navio. ASCEND → IDLE ou WIN. */
    public boolean returnToShip() {
        DeepSeaState b = context.getState();
        boolean r = context.returnToShip();
        if (r) { log("Drone regressou ao navio."); fireData(); fireState(b); }
        return r;
    }

    // ── EXPLORE_STATE ─────────────────────────────────────────────

    /** Move o drone no fundo do mar (dx, dy). Consome combustível. */
    public boolean moveDroneSeabed(int dx, int dy) {
        DeepSeaState b = context.getState();
        boolean r = context.moveDrone(dx, dy);
        if (r) {
            pcs.firePropertyChange(PROP_DRONE_MOVED, null, context.getDronePosition());
            fireData();
            fireState(b);
        }
        return r;
    }

    /** Recolhe o minério da célula atual do drone. */
    public boolean collectMineral() {
        boolean r = context.collectMineral();
        if (r) { log("Minério recolhido."); fireData(); }
        return r;
    }

    /** Inicia o puzzle para recolher o artefacto. EXPLORE → PUZZLE. */
    public boolean collectArtifact() {
        DeepSeaState b = context.getState();
        boolean r = context.collectArtifact();
        if (r) { log("Puzzle iniciado."); fireData(); fireState(b); }
        return r;
    }

    /** Ordena subida do drone. EXPLORE → ASCEND_STATE. */
    public boolean ascendDrone(int droneId) {
        DeepSeaState b = context.getState();
        boolean r = context.ascendDrone(droneId);
        if (r) { log("Drone " + droneId + " a subir."); fireData(); fireState(b); }
        return r;
    }

    // ── PUZZLE_STATE ──────────────────────────────────────────────

    /** Move a peça do puzzle na posição linear (0-15). */
    public boolean movePiece(int piecePosition) {
        DeepSeaState b = context.getState();
        boolean r = context.movePiece(piecePosition);
        if (r) { fireData(); fireState(b); }
        return r;
    }

    // ── Log ───────────────────────────────────────────────────────

    /** Limpa todas as entradas do log e notifica o LogStage. */
    public void clearLogs() {
        DeepSeaLog.getInstance().clearLogs();
        pcs.firePropertyChange(PROP_LOG_ADDED, null, null);
    }

    // ── Persistência ──────────────────────────────────────────────

    public boolean saveGame(String fileName) {
        boolean r = context.save(fileName);
        if (r) { log("Jogo gravado: " + fileName); fireData(); }
        return r;
    }

    public boolean loadGame(String fileName) {
        DeepSeaContext loaded = DeepSeaContext.load(fileName);
        if (loaded != null) {
            DeepSeaState b = context.getState();
            this.context = loaded;
            log("Jogo carregado: " + fileName);
            fireData(); fireState(b);
            return true;
        }
        return false;
    }

    public boolean exportLogsToText()            { return DeepSeaLog.getInstance().saveToTextFile(); }
    public boolean exportLogsToText(String path) { return DeepSeaLog.getInstance().saveToTextFile(); }

    // ════════════════════════════════════════════════════════════════
    //  CONSULTAS — só tipos primitivos, enums ou imutáveis
    // ════════════════════════════════════════════════════════════════

    /** Estado atual da FSM (enum — imutável). */
    public DeepSeaState getCurrentState()    { return context.getState(); }

    /** Combustível atual do navio (double). */
    public double getShipFuel()              { return context.getShipFuel(); }

    /** Minérios acumulados no navio (double). */
    public double getShipMinerals()          { return context.getShipMinerals(); }

    /** Número de drones operacionais no navio. */
    public int getAvailableDrones()          { return context.getAvailableDronesCount(); }

    /** Posição do navio [row, col] — array novo a cada chamada. */
    public int[] getShipPosition()           { return context.getShipPosition(); }

    /** true quando todos os artefactos foram recolhidos. */
    public boolean allArtifactsCollected()   { return context.allArtifactsCollected(); }

    /** Lista imutável de mensagens de log. */
    public List<String> getLogs()            { return DeepSeaLog.getInstance().getLogs(); }

    /**
     * Posições da superfície com artefactos em profundidade.
     * Usado pelo SurfaceCanvas para o heatmap de calor.
     * @return lista imutável de int[]{row,col}
     */
    public List<int[]> getArtifactSurfacePositions() {
        return context.getArtifactSurfacePositions();
    }

    /**
     * Tipo da célula do fosso ativo na posição dada.
     * Usado pelo TrenchCanvas para desenhar rochas, animais e correntes.
     * @return "ROCK" | "ANIMAL" | "CURRENT" | "WATER"
     */
    public String getTrenchCellType(int row, int col) {
        return context.getTrenchCellType(row, col);
    }

    /** Posição atual do drone [posX, posY] — [-1,-1] se nenhum ativo. */
    public int[] getDronePosition()          { return context.getDronePosition(); }

    /** Combustível do drone ativo para o HUD do TrenchCanvas. */
    public double getActiveDroneFuel()       { return context.getActiveDroneFuel(); }

    /** Casco do drone ativo para o HUD do TrenchCanvas. */
    public double getActiveDroneHull()       { return context.getActiveDroneHull(); }

    public FloorCell getFloorCell(int shipRow, int shipCol, int floorRow, int floorCol) {
        return context.getFloorCell(shipRow, shipCol, floorRow, floorCol);
    }

    public int[][] getPuzzleBoard() {
        return context.getPuzzleBoard();
    }
}