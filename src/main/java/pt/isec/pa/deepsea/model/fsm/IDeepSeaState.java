package pt.isec.pa.deepsea.model.fsm;

/**
 * Contrato de todas as transições da FSM Deep Sea Mining.
 */
//Cada método representa uma transição/ação possível no jogo.
public interface IDeepSeaState {

    // ═══════════════════════════════════════════════════════
    //  IDLE_STATE  –  Navio à superfície
    // ═══════════════════════════════════════════════════════

    /**
     * Move o navio para uma célula adjacente na grelha de superfície.
     * Consome combustível. Se combustível = 0 → GAME_OVER_STATE.
     */
    boolean moveShip(int dx, int dy);

    /**
     * Abastece o drone selecionado transferindo combustível do navio.
     */
    boolean refuelDrone(int droneId, int volume);

    /**
     * Repara o casco do drone. Consome combustível do navio.
     * Se combustível do navio = 0 após reparação → GAME_OVER_STATE.
     */
    boolean repairDrone(int droneId);

    /**
     * Melhora o drone (tanque ou blindagem) usando minérios recolhidos.
     * @param type tipo de melhoria, ex: "TANK" ou "ARMOR"
     */
    boolean upgradeDrone(int droneId, String type);

    /**
     * Lança o drone para iniciar a descida pelo fosso.
     * IDLE_STATE → DESCEND_STATE.
     */
    boolean descendDrone(int droneId);

    // ═══════════════════════════════════════════════════════
    //  DESCEND_STATE  –  Drone a descer pelo fosso
    // ═══════════════════════════════════════════════════════

    /**
     * Move o drone lateralmente no fosso para evitar obstáculos ou rochas.
     * Disponível em DESCEND_STATE e ASCEND_STATE.
     * Consome combustível (dobro em correntes). Colisões reduzem o casco.
     * Se fuelEmpty() ou allDronesLost() → GAME_OVER_STATE.
     */
    boolean moveDrone(int dx);

    /**
     * Sinaliza que o drone atingiu o fundo e inicia a exploração.
     * DESCEND_STATE → EXPLORE_STATE.
     * Chamado pela UI quando a descida chega ao fim.
     */
    boolean exploreSeabed();

    // ═══════════════════════════════════════════════════════
    //  EXPLORE_STATE  –  Drone no fundo do mar
    // ═══════════════════════════════════════════════════════

    /**
     * Move o drone célula a célula no fundo do mar em 4 direções.
     * Revela a célula destino; pode encontrar minérios, monstros ou artefactos.
     * Disponível APENAS em EXPLORE_STATE (assinatura diferente de moveDrone(dx)).
     */
    boolean moveDrone(int dx, int dy);

    /**
     * Recolhe o minério da célula atual do drone.
     * Ação SEPARADA de collectArtifact — consome combustível extra.
     * Disponível APENAS em EXPLORE_STATE.
     */
    boolean collectMineral();

    /**
     * Tenta recolher o artefacto da célula atual → inicia o puzzle.
     * EXPLORE_STATE → PUZZLE_STATE.
     * Só funciona se a célula atual contiver um artefacto.
     */
    boolean collectArtifact();

    /**
     * Ordena ao drone que inicie a subida pelo fosso de volta ao navio.
     * EXPLORE_STATE → ASCEND_STATE.
     */
    boolean ascendDrone(int droneId);

    // ═══════════════════════════════════════════════════════
    //  PUZZLE_STATE  –  Minijogo Puzzle 15 peças
    // ═══════════════════════════════════════════════════════

    /**
     * Desloca a peça na posição indicada para o espaço vazio adjacente.
     *
     * Resultado possível:
     *  – Movimento válido, puzzle ainda por resolver → continua em PUZZLE_STATE.
     *  – Puzzle resolvido → EXPLORE_STATE (artefacto adicionado ao inventário).
     *  – Movimentos esgotados → ASCEND_STATE (drone sobe sem o artefacto).
     *
     * NÃO existe puzzleSolved() como transição — é verificação interna após movePiece().
     *
     * @param piecePosition índice (0-14) da peça a mover
     */
    boolean movePiece(int piecePosition);

    // ═══════════════════════════════════════════════════════
    //  ASCEND_STATE  –  Drone a subir pelo fosso
    // ═══════════════════════════════════════════════════════

    /**
     * O drone chegou ao navio após a subida.
     * ASCEND_STATE → IDLE_STATE  (ou WIN_STATE se todos os artefactos recolhidos).
     * Artefactos e minérios são adicionados ao portfólio do jogador.
     */
    boolean returnToShip();

    // ═══════════════════════════════════════════════════════
    //  Consulta
    // ═══════════════════════════════════════════════════════

    /** Devolve o enum do estado concreto atual. */
    DeepSeaState getState();
}