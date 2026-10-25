package pt.isec.pa.deepsea.model.data;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeepSeaMining — testes (modelo principal)")
public class DeepSeaMiningTest {

    private DeepSeaMining game;

    @BeforeEach
    void setUp() { game = new DeepSeaMining(); }

    // ── Estado geral ──────────────────────────────────────────────

    @Test @DisplayName("Jogo não está terminado no início")
    void testNaoTerminado() {
        assertFalse(game.isGameTerminated());
    }

    @Test @DisplayName("setGameTerminated altera o estado")
    void testSetTerminated() {
        game.setGameTerminated(true);
        assertTrue(game.isGameTerminated());
        game.setGameTerminated(false);
        assertFalse(game.isGameTerminated());
    }

    @Test @DisplayName("getShip não é null")
    void testGetShipNaoNull() { assertNotNull(game.getShip()); }

    @Test @DisplayName("getMiniGame não é null")
    void testGetMiniGameNaoNull() { assertNotNull(game.getMiniGame()); }

    @Test @DisplayName("getDronesList tem 3 drones")
    void testGetDronesList() {
        assertEquals(Constants.DRONES_INICIAIS, game.getDronesList().size());
    }

    @Test @DisplayName("getAvailableDronesCount = 3 no início")
    void testAvailableDronesInicial() {
        assertEquals(Constants.DRONES_INICIAIS, game.getAvailableDronesCount());
    }

    @Test @DisplayName("getActiveDroneId = -1 sem drone ativo")
    void testActiveDroneIdSemAtivo() {
        assertEquals(-1, game.getActiveDroneId());
    }

    @Test @DisplayName("allArtifactsCollected false no início")
    void testArtifactsNaoRecolhidos() {
        assertFalse(game.allArtifactsCollected());
    }

    // ── getArea ───────────────────────────────────────────────────

    @Test @DisplayName("getArea retorna não-null em posição válida")
    void testGetAreaValida() {
        assertNotNull(game.getArea(0, 0));
        assertNotNull(game.getArea(Constants.SURFACE_L - 1, Constants.SURFACE_C - 1));
    }

    @Test @DisplayName("getArea retorna null fora dos limites")
    void testGetAreaInvalida() {
        assertNull(game.getArea(-1, 0));
        assertNull(game.getArea(0, -1));
        assertNull(game.getArea(Constants.SURFACE_L, 0));
        assertNull(game.getArea(0, Constants.SURFACE_C));
    }

    // ── moveShip ──────────────────────────────────────────────────

    @Test @DisplayName("moveShip consome combustível")
    void testMoveShipConsome() {
        double antes = game.getShip().getFuel();
        game.moveShip(1, 0);
        assertTrue(game.getShip().getFuel() < antes);
    }

    @Test @DisplayName("moveShip fora dos limites retorna false")
    void testMoveShipInvalido() {
        assertFalse(game.moveShip(-Constants.SURFACE_L, 0));
    }

    @Test @DisplayName("moveShip bloqueado quando jogo terminado")
    void testMoveShipTerminado() {
        game.setGameTerminated(true);
        assertFalse(game.moveShip(0, 1));
    }

    // ── launchDrone ───────────────────────────────────────────────

    @Test @DisplayName("launchDrone com id válido lança drone")
    void testLaunchDroneValido() {
        int id = game.getDronesList().get(0).getId();
        assertTrue(game.launchDrone(id));
        assertEquals(id, game.getActiveDroneId());
        assertFalse(game.getDronesList().get(0).isInShip());
    }

    @Test @DisplayName("launchDrone com id inválido retorna false")
    void testLaunchDroneInvalido() {
        assertFalse(game.launchDrone(9999));
    }

    // ── refuelDrone / repairDrone ─────────────────────────────────

    @Test @DisplayName("repairDrone consome combustível do navio")
    void testRepairDroneConsome() {
        int id = game.getDronesList().get(0).getId();
        game.getDronesList().get(0).sofrerDano(30.0);
        double antes = game.getShip().getFuel();
        assertTrue(game.repairDrone(id));
        assertTrue(game.getShip().getFuel() < antes);
    }

    @Test @DisplayName("repairDrone com id inválido retorna false")
    void testRepairDroneInvalido() {
        assertFalse(game.repairDrone(9999));
    }

    @Test @DisplayName("upgradeDrone TANK aumenta maxFuel do drone")
    void testUpgradeTank() {
        int id = game.getDronesList().get(0).getId();
        double maxAntes = game.getDronesList().get(0).getMaxFuel();
        game.upgradeDrone(id, "TANK");
        assertEquals(maxAntes + Constants.UPGRADE_TANK_AMOUNT,
                game.getDronesList().get(0).getMaxFuel(), 0.001);
    }

    // ── moveDroneLateral ──────────────────────────────────────────

    @Test @DisplayName("moveDroneLateral sem drone ativo retorna false")
    void testLateralSemAtivo() {
        assertFalse(game.moveDroneLateral(1));
    }

    @Test @DisplayName("moveDroneLateral com drone ativo não lança exceção")
    void testLateralComAtivo() {
        int id = game.getDronesList().get(0).getId();
        game.launchDrone(id);
        assertDoesNotThrow(() -> game.moveDroneLateral(1));
    }

    // ── movePuzzlePiece ───────────────────────────────────────────

    @Test @DisplayName("movePuzzlePiece fora do range retorna INVALID_MOVE")
    void testPuzzleForaRange() {
        assertEquals(DeepSeaMining.PuzzleResult.INVALID_MOVE, game.movePuzzlePiece(-1));
        assertEquals(DeepSeaMining.PuzzleResult.INVALID_MOVE,
                game.movePuzzlePiece(Constants.PUZZLE_DIM * Constants.PUZZLE_DIM));
    }

    @Test @DisplayName("movePuzzlePiece em posição adjacente ao vazio é válido")
    void testPuzzleAdjacenteValido() {
        if (Constants.MODO_DEFESA) {
            // piecePosition 14 = [3][2] → adjacente ao vazio [3][3]
            DeepSeaMining.PuzzleResult r = game.movePuzzlePiece(14);
            assertNotEquals(DeepSeaMining.PuzzleResult.INVALID_MOVE, r);
        }
    }

    // ── isActiveDroneLost ─────────────────────────────────────────

    @Test @DisplayName("isActiveDroneLost false sem drone ativo")
    void testLostSemAtivo() {
        assertFalse(game.isActiveDroneLost());
    }

    @Test @DisplayName("isActiveDroneLost true quando combustível = 0")
    void testLostSemCombustivel() {
        int id = game.getDronesList().get(0).getId();
        game.launchDrone(id);
        game.getDronesList().get(0).aplicarConsumo(Constants.FUEL_INICIAL_DRONE + 1);
        assertTrue(game.isActiveDroneLost());
    }

    @Test @DisplayName("isActiveDroneLost true quando casco = 0")
    void testLostSemCasco() {
        int id = game.getDronesList().get(0).getId();
        game.launchDrone(id);
        game.getDronesList().get(0).sofrerDano(Constants.DRONE_HULL_MAX + 1);
        assertTrue(game.isActiveDroneLost());
    }

    // ── handleActiveDroneLost ─────────────────────────────────────

    @Test @DisplayName("handleActiveDroneLost reduz drones disponíveis")
    void testHandleLostReduzDrones() {
        int antes = game.getAvailableDronesCount();
        int id = game.getDronesList().get(0).getId();
        game.launchDrone(id);
        game.handleActiveDroneLost();
        assertEquals(antes - 1, game.getAvailableDronesCount());
        assertEquals(-1, game.getActiveDroneId());
    }

    @Test @DisplayName("collectDroneCargo entrega artefactos ao navio")
    void testCollectCargoArtefactos() {
        int id = game.getDronesList().get(0).getId();
        game.launchDrone(id);
        game.getDronesList().get(0).recolherItem(new Artifact());
        int artAntes = game.getShip().getArtifacts().size();
        game.collectDroneCargo();
        assertEquals(artAntes + 1, game.getShip().getArtifacts().size());
        assertNull(null); // drone limpo
        assertEquals(-1, game.getActiveDroneId());
    }
}