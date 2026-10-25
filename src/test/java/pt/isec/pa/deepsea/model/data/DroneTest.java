package pt.isec.pa.deepsea.model.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes à classe Drone.
 */
public class DroneTest {

    private Drone drone;

    @BeforeEach
    void setUp() {
        // Resetar o contador de IDs não é possível (estático),
        // mas cada teste cria um drone independente
        drone = new Drone();
    }

    @Test
    void testInitialState() {
        // Drone começa no navio, com depósito e casco cheios
        assertTrue(drone.isInShip(), "Drone deve começar no navio");
        assertEquals(Constants.FUEL_INICIAL_DRONE, drone.getFuel(), 0.001,
                "Combustível inicial deve ser FUEL_INICIAL_DRONE");
        assertEquals(Constants.DRONE_HULL_MAX, drone.getHull(), 0.001,
                "Casco inicial deve ser DRONE_HULL_MAX");
        assertFalse(drone.isDestroyed(), "Drone novo não deve estar destruído");
    }

    @Test
    void testAplicarConsumoSoFuncaForaDoNavio() {
        double fuelAntes = drone.getFuel();
        // No navio → não consome
        drone.aplicarConsumo(10.0);
        assertEquals(fuelAntes, drone.getFuel(), 0.001,
                "Drone no navio não deve consumir combustível");

        // Fora do navio → consome
        drone.setInShip(false);
        drone.aplicarConsumo(10.0);
        assertEquals(fuelAntes - 10.0, drone.getFuel(), 0.001,
                "Drone fora do navio deve consumir combustível");
    }

    @Test
    void testAplicarConsumoNuncaNegativo() {
        drone.setInShip(false);
        drone.aplicarConsumo(Constants.FUEL_INICIAL_DRONE + 999);
        assertEquals(0.0, drone.getFuel(), 0.001,
                "Combustível não pode ficar negativo");
    }

    @Test
    void testSofrerDano() {
        drone.sofrerDano(30.0);
        assertEquals(Constants.DRONE_HULL_MAX - 30.0, drone.getHull(), 0.001,
                "Casco deve reduzir pelo dano aplicado");
    }

    @Test
    void testSofrerDanoNuncaNegativo() {
        drone.sofrerDano(Constants.DRONE_HULL_MAX + 999);
        assertEquals(0.0, drone.getHull(), 0.001,
                "Casco não pode ficar negativo");
    }

    @Test
    void testIsDestroyedQuandoCascoZero() {
        assertFalse(drone.isDestroyed(), "Drone intacto não está destruído");
        drone.sofrerDano(Constants.DRONE_HULL_MAX); // casco → 0
        assertTrue(drone.isDestroyed(), "Drone com casco 0 está destruído");
    }

    @Test
    void testRecarregarCombustivel() {
        drone.setInShip(false);
        drone.aplicarConsumo(50.0); // gasta metade
        drone.recarregarCombustivel(30.0);
        assertEquals(Constants.FUEL_INICIAL_DRONE - 20.0, drone.getFuel(), 0.001,
                "Recarregar deve adicionar combustível até ao máximo");
    }

    @Test
    void testRecarregarNaoUltrapassaMaximo() {
        drone.recarregarCombustivel(9999.0);
        assertEquals(drone.getMaxFuel(), drone.getFuel(), 0.001,
                "Recarregar não deve ultrapassar o máximo do tanque");
    }

    @Test
    void testRepararCasco() {
        drone.sofrerDano(40.0);
        drone.repararCasco();
        assertEquals(drone.getMaxHull(), drone.getHull(), 0.001,
                "Reparar deve restaurar o casco ao máximo");
    }

    @Test
    void testRealizarManutencaoSoFuncaNoNavio() {
        drone.setInShip(false);
        drone.aplicarConsumo(50.0);
        drone.sofrerDano(30.0);
        double fuelApos = drone.getFuel();
        double cascoApos = drone.getHull();

        // Fora do navio → manutenção não funciona
        drone.realizarManutencao();
        assertEquals(fuelApos, drone.getFuel(), 0.001,
                "Manutenção fora do navio não deve alterar combustível");
        assertEquals(cascoApos, drone.getHull(), 0.001,
                "Manutenção fora do navio não deve alterar casco");

        // No navio → manutenção funciona
        drone.setInShip(true);
        drone.realizarManutencao();
        assertEquals(drone.getMaxFuel(), drone.getFuel(), 0.001,
                "Manutenção no navio deve restaurar combustível");
        assertEquals(drone.getMaxHull(), drone.getHull(), 0.001,
                "Manutenção no navio deve restaurar casco");
    }

    @Test
    void testMelhorarTanque() {
        double maxAntes = drone.getMaxFuel();
        drone.melhorarTanque();
        assertEquals(maxAntes + Constants.UPGRADE_TANK_AMOUNT, drone.getMaxFuel(), 0.001,
                "Upgrade de tanque deve aumentar o máximo de combustível");
    }

    @Test
    void testMelhorarBlindagem() {
        double maxAntes = drone.getMaxHull();
        drone.melhorarBlindagem();
        assertEquals(maxAntes + Constants.UPGRADE_ARMOR_AMOUNT, drone.getMaxHull(), 0.001,
                "Upgrade de blindagem deve aumentar o máximo de casco");
    }

    @Test
    void testLimparCarga() {
        drone.recolherItem(new Artifact());
        drone.recolherItem(new Artifact());
        assertFalse(drone.getCargo().isEmpty(), "Carga deve ter itens antes de limpar");
        drone.limparCarga();
        assertTrue(drone.getCargo().isEmpty(), "Carga deve estar vazia após limpar");
    }

    @Test
    void testResetParaDescerFosso() {
        drone.resetParaDescerFosso();
        assertFalse(drone.isInShip(), "Drone deve sair do navio ao iniciar descida");
        assertEquals(0, drone.getPosX(), "Drone deve começar no topo do fosso (row 0)");
        assertEquals(Constants.FOSSO_L / 2, drone.getPosY(),
                "Drone deve começar no centro lateral do fosso");
    }

    @Test
    void testResetParaFundo() {
        drone.resetParaFundo();
        assertFalse(drone.isInShip(), "Drone deve sair do navio ao explorar fundo");
        assertEquals(Constants.FUNDO_DIM / 2, drone.getPosX());
        assertEquals(Constants.FUNDO_DIM / 2, drone.getPosY());
    }

    @Test
    void testCombustivelInicialDrone() {
        Drone drone = new Drone();
        Constants c = new Constants();
        // Prova que o combustível inicia correto conforme as constantes
        assertEquals(c.FUEL_INICIAL_DRONE, drone.getFuel(),
                "ERRO: O drone deve começar com o combustível máximo definido.");
    }

    @Test
    void testIDsUnicosDrones() {
        // Criar dois drones seguidos
        Drone d1 = new Drone();
        Drone d2 = new Drone();

        // Prova que os drones ganham IDs diferentes e sequenciais
        assertNotEquals(d1.getId(), d2.getId(), "ERRO: Cada drone deve ter um ID único.");
        assertEquals(d1.getId() + 1, d2.getId(), "ERRO: O ID do segundo drone deve ser o seguinte ao primeiro.");
    }
}