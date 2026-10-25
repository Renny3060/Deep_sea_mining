package pt.isec.pa.deepsea.model.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes à classe Ship.
 */
public class ShipTest {

    private Ship ship;

    @BeforeEach
    void setUp() {
        ship = new Ship();
    }

    @Test
    void testInitialFuelIsMax() {
        // O navio começa com o depósito cheio
        assertEquals(Constants.SHIP_FUEL_MAX, ship.getFuel(), 0.001,
                "Combustível inicial deve ser igual a SHIP_FUEL_MAX");
    }

    @Test
    void testConsumirCombustivelReducesFuel() {
        double antes = ship.getFuel();
        ship.consumirCombustivel(50.0);
        assertEquals(antes - 50.0, ship.getFuel(), 0.001,
                "Combustível deve reduzir pelo valor consumido");
    }

    @Test
    void testConsumirCombustivelNeverGoesNegative() {
        // Consumir mais do que existe nunca deve dar negativo
        ship.consumirCombustivel(Constants.SHIP_FUEL_MAX + 999);
        assertEquals(0.0, ship.getFuel(), 0.001,
                "Combustível não pode ficar negativo");
    }

    @Test
    void testMoverDentroDosMapa() {
        int rowInicial = ship.getRow();
        int colInicial = ship.getCol();
        boolean moveu = ship.mover(1, 0);
        assertTrue(moveu, "Mover dentro dos limites deve retornar true");
        assertEquals(rowInicial + 1, ship.getRow(),
                "Linha deve aumentar 1 após mover(1,0)");
        assertEquals(colInicial, ship.getCol(),
                "Coluna não deve mudar após mover(1,0)");
    }

    @Test
    void testMoverForaDosMapa() {
        // Tentar mover para fora dos limites da grelha
        // Posição inicial é SURFACE_L/2, SURFACE_C/2
        // Forçar saída pelo canto negativo
        boolean moveu = ship.mover(-Constants.SURFACE_L, -Constants.SURFACE_C);
        assertFalse(moveu, "Mover fora dos limites deve retornar false");
    }

    @Test
    void testGetRowAndGetColAliases() {
        // getRow() e getCol() devem ser alias de getPosX() e getPosY()
        assertEquals(ship.getPosX(), ship.getRow());
        assertEquals(ship.getPosY(), ship.getCol());
    }

    @Test
    void testAdicionarArtefacto() {
        int antes = ship.getArtifacts().size();
        ship.adicionarArtefacto(new Artifact());
        assertEquals(antes + 1, ship.getArtifacts().size(),
                "Lista de artefactos deve crescer após adicionar");
    }

    @Test
    void testAdicionarMinerais() {
        ship.adicionarMinerais(25.5);
        assertEquals(25.5, ship.getMinerals(), 0.001,
                "Minérios devem acumular corretamente");
        ship.adicionarMinerais(10.0);
        assertEquals(35.5, ship.getMinerals(), 0.001,
                "Minérios devem acumular em chamadas sucessivas");
    }

    @Test
    void testGetDronesRetornaTreeDrones() {
        assertEquals(Constants.DRONES_INICIAIS, ship.getDrones().size(),
                "Navio deve começar com " + Constants.DRONES_INICIAIS + " drones");
    }

    @Test
    void testGetArtifactsIsUnmodifiable() {
        // A lista devolvida não deve poder ser modificada diretamente
        assertThrows(UnsupportedOperationException.class,
                () -> ship.getArtifacts().add(new Artifact()),
                "getArtifacts() deve devolver lista imutável");
    }

    @Test
    void testListagemDeDronesPorID() {
        Ship ship = new Ship();
        java.util.List<Drone> listaDrones = ship.getDronesSortedByID();

        // Verifica se a lista foi criada e tem o tamanho certo
        assertNotNull(listaDrones, "ERRO: A lista ordenada por ID não pode ser nula.");
        assertEquals(3, listaDrones.size(), "ERRO: A lista deve conter os 3 drones.");

        // Verifica se os IDs estão realmente em ordem crescente (1, 2, 3) [cite: 5, 16]
        assertTrue(listaDrones.get(0).getId() < listaDrones.get(1).getId(), "ERRO: O ID do primeiro drone deve ser menor que o do segundo.");
        assertTrue(listaDrones.get(1).getId() < listaDrones.get(2).getId(), "ERRO: O ID do segundo drone deve ser menor que o do terceiro.");
    }

    @Test
    void testProtecaoDeDadosEncapsulamento() {
        Ship ship = new Ship();

        // Testa se a lista de drones está protegida contra modificações externas
        assertThrows(UnsupportedOperationException.class, () -> {
            ship.getDrones().clear();
        }, "ERRO: Não deve ser possível apagar drones diretamente através do getter.");
    }
}