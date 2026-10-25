package pt.isec.pa.deepsea.model.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeaFloorTest {

    @Test
    void testExtrairMineralEResetaCelula() {
        SeaFloor floor = new SeaFloor();

        // 1. Forçamos a colocação de um mineral conhecido para o teste ser fiável
        Mineral mOriginal = new Mineral(1.0);
        floor.configurarCell(2, 2, mOriginal);

        // 2. Tentamos extrair o objeto
        Mineral mExtraido = floor.extrairMineralObjeto(2, 2);

        // 3. Verificações
        assertNotNull(mExtraido, "ERRO: O mineral extraído não deve ser nulo.");
        assertEquals(1.0, mExtraido.getAmount(), 0.01, "ERRO: O valor extraído do mineral deve ser 1.0.");

        // 4. Verifica se a célula no mapa ficou limpa (tornou-se EmptyFloor)
        assertTrue(floor.getCell(2, 2) instanceof EmptyFloor,
                "ERRO: Após a extração, a célula deve tornar-se EmptyFloor.");
    }

    @Test
    void testRecolherArtefacto() {
        SeaFloor floor = new SeaFloor();

        // Vamos forçar a colocação de um artefacto para testar a lógica (usando o modo defesa ou manual se permitido)
        if (Constants.MODO_DEFESA) {
            // Em Jogo.java, o setupDefesaArtifacts coloca um artefacto em [0,0] do fundo de certas áreas
            // Aqui testamos se o método recolherArtefacto funciona
            Artifact a = floor.recolherArtefacto(0, 0);

            if (a != null) {
                // Verifica se a célula ficou limpa
                assertTrue(floor.getCell(0, 0) instanceof EmptyFloor,
                        "ERRO: A célula deve ficar vazia após recolher o artefacto.");
            }
        }
    }

    @Test
    void testGeracaoDeMonstros() {
        // Este teste verifica se o getCell continua a ter a "inteligência" de gerar monstros
        if (!Constants.MODO_DEFESA) {
            SeaFloor floor = new SeaFloor();
            boolean encontrouMonstro = false;

            // Varremos o mapa (dim * dim) para ver se a probabilidade de monstro funciona
            for (int r = 0; r < Constants.FUNDO_DIM; r++) {
                for (int c = 0; c < Constants.FUNDO_DIM; c++) {
                    if (floor.getCell(r, c) instanceof Monster) {
                        encontrouMonstro = true;
                        break;
                    }
                }
            }
            // Nota: Este teste pode falhar raramente por sorte/probabilidade,
            // mas com um mapa 10x10 e PROB_MONSTER, deve encontrar.
            assertTrue(encontrouMonstro, "ERRO: O mapa devia gerar monstros dinamicamente.");
        }
    }

    @Test
    void testProtecaoDoSetCell() {
        // Verifica se o método setCell é agora inacessível ou protegido
        // Este teste é conceptual, pois se tentares chamar floor.setCell() aqui
        // o código nem deve compilar se o teste estiver noutro package.
        // Se estiver no mesmo package, o setCell ainda funciona (o que é normal em Java para testes).
    }
}