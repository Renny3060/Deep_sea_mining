package pt.isec.pa.deepsea.model.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeaTrenchTest {

    @Test
    void testLimpezaDeCaminho() {
        SeaTrench trench = new SeaTrench();
        int row = 5;
        int col = Constants.FOSSO_L / 2;

        // Se estivermos no modo defesa, sabemos que existe um animal em [5, centro]
        if (Constants.MODO_DEFESA) {
            // 1. Verificar se a ameaça existe antes de limpar
            assertTrue(trench.getCell(row, col) instanceof SeaAnimal,
                    "ERRO: Devia existir um SeaAnimal em [5, meio] no Modo Defesa.");

            // 2. Executar a nova lógica de limpeza
            trench.limparCaminho(row, col);

            // 3. Verificar se a célula foi convertida em água (Encapsulamento com sucesso)
            assertTrue(trench.getCell(row, col) instanceof Water,
                    "ERRO: A célula devia ser Water após chamar limparCaminho.");
        }
    }

    @Test
    void testParedesNoModoDefesa() {
        if (Constants.MODO_DEFESA) {
            SeaTrench trench = new SeaTrench();

            // Verifica as rochas fixas das margens na linha central
            int row = Constants.FOSSO_P / 2;
            assertTrue(trench.getCell(row, 0) instanceof Rock,
                    "ERRO: Devia haver uma rocha na margem esquerda (coluna 0).");
            assertTrue(trench.getCell(row, Constants.FOSSO_L - 1) instanceof Rock,
                    "ERRO: Devia haver uma rocha na margem direita.");
        }
    }

    @Test
    void testGeracaoDinamicaDeAmeacas() {
        // Testa se o getCell gera animais/correntes fora do modo defesa
        if (!Constants.MODO_DEFESA) {
            SeaTrench trench = new SeaTrench();
            boolean encontrouAlgo = false;

            // Varremos uma parte do fosso para validar a geração aleatória
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < Constants.FOSSO_L; c++) {
                    Cell cell = trench.getCell(r, c);
                    if (cell instanceof SeaAnimal || cell instanceof Current) {
                        encontrouAlgo = true;
                        break;
                    }
                }
            }
            // Verifica se a lógica probabilística do getter está a funcionar
            assertTrue(encontrouAlgo, "ERRO: O fosso devia gerar fauna dinamicamente ao aceder às células.");
        }
    }
}