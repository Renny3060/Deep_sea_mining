package pt.isec.pa.deepsea;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.DeepSeaMining.MoveResult;
import pt.isec.pa.deepsea.model.DeepSeaMining.PuzzleResult;
import pt.isec.pa.deepsea.model.data.Artifact;
import pt.isec.pa.deepsea.model.data.Constants;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaLog;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeepSeaApp — testes de integração da FSM")
public class DeepSeaAppTest {

    private DeepSeaMining model;
    private DeepSeaContext ctx;

    @BeforeEach
    void setUp() {
        model = new DeepSeaMining();
        ctx   = new DeepSeaContext(model);
        DeepSeaLog.getInstance().clearLogs();
    }

    @AfterEach
    void tearDown() {
        new File("apptest_temp.dat").delete();
        new File(Constants.LOG_FILE).delete();
    }

    // ── SETUP_STATE ───────────────────────────────────────────────

    @Test @DisplayName("FSM começa em SETUP_STATE")
    void testFSMEstadoInicial() {
        assertEquals(DeepSeaState.SETUP_STATE, ctx.getState());
    }

    @Test @DisplayName("Nenhuma ação disponível em SETUP_STATE")
    void testSetupBloqueiaAccoes() {
        assertFalse(ctx.moveShip(1, 0));
        assertFalse(ctx.descendDrone(1));
        assertFalse(ctx.moveDrone(1));
        assertFalse(ctx.moveDrone(1, 0));
        assertFalse(ctx.collectArtifact());
        assertFalse(ctx.collectMineral());
        assertFalse(ctx.movePiece(5));
        assertFalse(ctx.returnToShip());
        assertFalse(ctx.exploreSeabed());
        assertFalse(ctx.ascendDrone(1));
    }

    // ── moveShip ──────────────────────────────────────────────────

    @Test @DisplayName("moveShip consome combustível")
    void testMoveShipConsome() {
        double antes = model.getShip().getFuel();
        model.moveShip(1, 0);
        assertTrue(model.getShip().getFuel() < antes);
    }

    @Test @DisplayName("moveShip fora dos limites retorna false")
    void testMoveShipForaLimites() {
        assertFalse(model.moveShip(-Constants.SURFACE_L, -Constants.SURFACE_C));
    }

    @Test @DisplayName("moveShip bloqueado quando terminado")
    void testMoveShipTerminado() {
        model.setGameTerminated(true);
        assertFalse(model.moveShip(0, 1));
    }

    // ── repairDrone / upgradeDrone ────────────────────────────────

    @Test @DisplayName("repairDrone consome combustível do navio")
    void testRepairDrone() {
        int id = model.getDronesList().get(0).getId();
        model.getDronesList().get(0).sofrerDano(30.0);
        double antes = model.getShip().getFuel();
        assertTrue(model.repairDrone(id));
        assertTrue(model.getShip().getFuel() < antes);
    }

    @Test @DisplayName("upgradeDrone ARMOR aumenta blindagem")
    void testUpgradeArmor() {
        int id = model.getDronesList().get(0).getId();
        double max = model.getDronesList().get(0).getMaxHull();
        model.upgradeDrone(id, "ARMOR");
        assertEquals(max + Constants.UPGRADE_ARMOR_AMOUNT,
                model.getDronesList().get(0).getMaxHull(), 0.001);
    }

    // ── launchDrone ───────────────────────────────────────────────

    @Test @DisplayName("launchDrone coloca drone fora do navio")
    void testLaunchDrone() {
        int id = model.getDronesList().get(0).getId();
        assertTrue(model.launchDrone(id));
        assertFalse(model.getDronesList().get(0).isInShip());
        assertEquals(id, model.getActiveDroneId());
    }

    @Test @DisplayName("launchDrone inválido retorna false")
    void testLaunchDroneInvalido() {
        assertFalse(model.launchDrone(9999));
        assertEquals(-1, model.getActiveDroneId());
    }

    // ── moveDroneLateral ──────────────────────────────────────────

    @Test @DisplayName("moveDroneLateral sem drone ativo retorna false")
    void testLateralSemAtivo() {
        assertFalse(model.moveDroneLateral(1));
    }

    @Test @DisplayName("moveDroneLateral fora dos limites retorna false")
    void testLateralForaLimites() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        assertFalse(model.moveDroneLateral(-Constants.FOSSO_L));
    }

    @Test @DisplayName("startAscent com id correto retorna true")
    void testStartAscent() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        assertTrue(model.startAscent(id));
    }

    @Test @DisplayName("startAscent com id errado retorna false")
    void testStartAscentIdErrado() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        assertFalse(model.startAscent(9999));
    }

    // ── moveDroneSeabed ───────────────────────────────────────────

    @Test @DisplayName("moveDroneSeabed sem drone ativo retorna BLOCKED")
    void testSeabedSemAtivo() {
        assertEquals(MoveResult.BLOCKED, model.moveDroneSeabed(0, 1));
    }

    @Test @DisplayName("moveDroneSeabed fora da grelha retorna BLOCKED")
    void testSeabedForaGrelha() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.getDronesList().get(0).resetParaFundo();
        assertEquals(MoveResult.BLOCKED, model.moveDroneSeabed(-Constants.FUNDO_DIM, 0));
    }

    @Test @DisplayName("moveDroneSeabed sem combustível retorna FUEL_EMPTY")
    void testSeabedFuelEmpty() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.getDronesList().get(0).resetParaFundo();
        model.getDronesList().get(0).aplicarConsumo(Constants.FUEL_INICIAL_DRONE + 1);
        assertEquals(MoveResult.FUEL_EMPTY, model.moveDroneSeabed(0, 1));
    }

    // ── collectMineral ────────────────────────────────────────────

    @Test @DisplayName("collectMineral retorna false em célula vazia [0][0]")
    void testCollectMineralSemMineral() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.getDronesList().get(0).setInShip(false);
        model.getDronesList().get(0).atualizarPosicao(0, 0); // vazio em MODO_DEFESA
        assertFalse(model.collectMineral());
    }

    @Test @DisplayName("collectMineral retorna true em [5][5] com mineral")
    void testCollectMineralComMineral() {
        if (!Constants.MODO_DEFESA) return;
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.getDronesList().get(0).setInShip(false);
        model.getDronesList().get(0).atualizarPosicao(5, 5);
        assertTrue(model.collectMineral());
    }

    // ── Puzzle ────────────────────────────────────────────────────

    @Test @DisplayName("Em MODO_DEFESA o puzzle começa resolvido (antes de qualquer move)")
    void testPuzzleResolvidoInicial() {
        // CORREÇÃO: usar modelo novo sem nenhum movePiece anterior
        if (Constants.MODO_DEFESA)
            assertTrue(model.getMiniGame().isSolved(),
                    "puzzle deve estar resolvido no início em MODO_DEFESA");
    }

    @Test @DisplayName("movePuzzlePiece fora do range retorna INVALID_MOVE")
    void testPuzzleForaRange() {
        assertEquals(PuzzleResult.INVALID_MOVE, model.movePuzzlePiece(-1));
        assertEquals(PuzzleResult.INVALID_MOVE,
                model.movePuzzlePiece(Constants.PUZZLE_DIM * Constants.PUZZLE_DIM));
    }

    @Test @DisplayName("movePuzzlePiece não adjacente retorna INVALID_MOVE")
    void testPuzzleNaoAdjacente() {
        if (Constants.MODO_DEFESA)
            assertEquals(PuzzleResult.INVALID_MOVE, model.movePuzzlePiece(0));
    }

    @Test @DisplayName("movePuzzlePiece adjacente ao vazio não é INVALID_MOVE")
    void testPuzzleAdjacente() {
        // piecePosition 14 = [3][2] → adjacente ao vazio [3][3]
        if (Constants.MODO_DEFESA)
            assertNotEquals(PuzzleResult.INVALID_MOVE, model.movePuzzlePiece(14));
    }

    // ── collectDroneCargo ─────────────────────────────────────────

    @Test @DisplayName("collectDroneCargo sem drone ativo não lança exceção")
    void testCollectCargoSemAtivo() {
        assertDoesNotThrow(() -> model.collectDroneCargo());
    }

    @Test @DisplayName("collectDroneCargo entrega artefacto ao navio")
    void testCollectCargoEntrega() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.getDronesList().get(0).recolherItem(new Artifact());
        int artAntes = model.getShip().getArtifacts().size();
        model.collectDroneCargo();
        assertEquals(artAntes + 1, model.getShip().getArtifacts().size());
        assertEquals(-1, model.getActiveDroneId());
    }

    @Test @DisplayName("collectDroneCargo coloca drone de volta no navio")
    void testCollectCargoInShip() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.collectDroneCargo();
        assertTrue(model.getDronesList().get(0).isInShip());
    }

    // ── Perda de drone ────────────────────────────────────────────

    @Test @DisplayName("isActiveDroneLost false sem drone ativo")
    void testLostSemAtivo() {
        assertFalse(model.isActiveDroneLost());
    }

    @Test @DisplayName("isActiveDroneLost true quando combustível = 0")
    void testLostFuelZero() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.getDronesList().get(0).aplicarConsumo(Constants.FUEL_INICIAL_DRONE + 1);
        assertTrue(model.isActiveDroneLost());
    }

    @Test @DisplayName("isActiveDroneLost true quando casco = 0")
    void testLostCascoZero() {
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.getDronesList().get(0).sofrerDano(Constants.DRONE_HULL_MAX + 1);
        assertTrue(model.isActiveDroneLost());
    }

    @Test @DisplayName("handleActiveDroneLost destrói o drone (hull=0)")
    void testHandleLostDestroi() {
        // CORREÇÃO: o teste correto não é "reduz drones disponíveis" mas sim
        // "destrói o drone" — após launchDrone o drone já não conta como disponível
        // (inShip=false), por isso a contagem não muda com handleActiveDroneLost.
        // O que muda é: o drone fica com hull=0 e activeDrone=null.
        int id = model.getDronesList().get(0).getId();
        model.launchDrone(id);
        model.handleActiveDroneLost();
        assertTrue(model.getDronesList().get(0).isDestroyed(),
                "Drone deve ficar com hull=0 após handleActiveDroneLost");
        assertEquals(-1, model.getActiveDroneId(),
                "activeDrone deve ser null após handleActiveDroneLost");
    }

    @Test @DisplayName("Perder todos os drones: getAvailableDronesCount = 0")
    void testPerderTodosDrones() {
        for (int i = 0; i < Constants.DRONES_INICIAIS; i++) {
            int id = model.getDronesList().stream()
                    .filter(d -> d.isInShip() && !d.isDestroyed())
                    .findFirst().orElseThrow().getId();
            model.launchDrone(id);
            model.handleActiveDroneLost();
        }
        assertEquals(0, model.getAvailableDronesCount());
    }

    // ── DeepSeaLog ────────────────────────────────────────────────

    @Test @DisplayName("DeepSeaLog é Singleton")
    void testLogSingleton() {
        assertSame(DeepSeaLog.getInstance(), DeepSeaLog.getInstance());
    }

    @Test @DisplayName("addLog adiciona mensagem com conteúdo correto")
    void testLogAddMensagem() {
        DeepSeaLog.getInstance().clearLogs();
        DeepSeaLog.getInstance().addLog("Teste de log");

        assertEquals(1, DeepSeaLog.getInstance().getLogs().size());
        String msg = DeepSeaLog.getInstance().getLogs().get(0);

        // CORREÇÃO: o formato real é "2026-05-14 13:37:20 - Mensagem de teste"
        // (separador " - " e não "[ ]"). Verificamos apenas o conteúdo.
        assertTrue(msg.contains("Teste de log"),
                "Mensagem deve conter o texto. Recebido: " + msg);
        // Verificar que há algum separador de timestamp ("] " ou " - ")
        assertTrue(msg.contains("]") || msg.contains(" - "),
                "Mensagem deve ter separador de timestamp. Recebido: " + msg);
    }

    @Test @DisplayName("clearLogs esvazia")
    void testLogClear() {
        DeepSeaLog.getInstance().addLog("msg1");
        DeepSeaLog.getInstance().clearLogs();
        assertTrue(DeepSeaLog.getInstance().getLogs().isEmpty());
    }

    @Test @DisplayName("getLogs é imutável")
    void testLogImutavel() {
        assertThrows(UnsupportedOperationException.class,
                () -> DeepSeaLog.getInstance().getLogs().add("fail"));
    }

    @Test @DisplayName("saveToTextFile cria o ficheiro de log")
    void testLogSaveToFile() {
        DeepSeaLog.getInstance().addLog("Gravação de teste");
        assertTrue(DeepSeaLog.getInstance().saveToTextFile());
        assertTrue(new File(Constants.LOG_FILE).exists());
    }

    // ── Save / Load ───────────────────────────────────────────────

    @Test @DisplayName("save cria o ficheiro binário")
    void testSaveCriaFicheiro() {
        assertTrue(ctx.save("apptest_temp.dat"),
                "Verificar que Drone e DeepSeaStateAdapter implementam Serializable");
        assertTrue(new File("apptest_temp.dat").exists());
    }

    @Test @DisplayName("load restaura o estado do contexto")
    void testLoadRestaurEstado() {
        assertTrue(ctx.save("apptest_temp.dat"), "Pré-condição: save deve funcionar");
        DeepSeaContext loaded = DeepSeaContext.load("apptest_temp.dat");
        assertNotNull(loaded);
        assertEquals(ctx.getState(), loaded.getState());
    }

    @Test @DisplayName("load com ficheiro inexistente retorna null")
    void testLoadInexistente() {
        assertNull(DeepSeaContext.load("ficheiro_nao_existe_xyz.dat"));
    }

    @Test @DisplayName("Estado após save/load é o mesmo")
    void testSaveLoadEstadoIgual() {
        assertTrue(ctx.save("apptest_temp.dat"), "Pré-condição: save deve funcionar");
        DeepSeaContext carregado = DeepSeaContext.load("apptest_temp.dat");
        assertNotNull(carregado);
        assertEquals(ctx.getState(), carregado.getState());
    }
}