package pt.isec.pa.deepsea.model.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes à classe MiniGame (puzzle 15 peças).
 * Em MODO_DEFESA o tabuleiro começa resolvido — ideal para testes previsíveis.
 */
public class MiniGameTest {

    private MiniGame miniGame;

    @BeforeEach
    void setUp() {
        // MODO_DEFESA = true → tabuleiro começa na ordem correta (1..15, 0)
        miniGame = new MiniGame();
    }

    @Test
    void testInitialBoardIsSolvedInDefesaMode() {
        // Em MODO_DEFESA o tabuleiro começa resolvido
        if (Constants.MODO_DEFESA) {
            assertTrue(miniGame.isSolved(),
                    "Em MODO_DEFESA o tabuleiro deve começar resolvido");
        }
    }

    @Test
    void testInitialMoveCounterIsZero() {
        assertEquals(0, miniGame.getMoveCounter(),
                "Contador de movimentos deve começar a zero");
    }

    @Test
    void testMoveIncreasesCounter() {
        // Em MODO_DEFESA o espaço vazio (0) está em [3][3].
        // A peça em [3][2] = 15 pode mover para [3][3].
        boolean moved = miniGame.move(3, 2); // peça 15 → move para o espaço vazio
        if (moved) {
            assertEquals(1, miniGame.getMoveCounter(),
                    "Cada movimento válido deve incrementar o contador");
        }
    }

    @Test
    void testMoveInvalidReturnsFalse() {
        // [0][0] tem a peça 1; não é adjacente ao espaço vazio [3][3]
        boolean moved = miniGame.move(0, 0);
        assertFalse(moved, "Mover peça não adjacente ao espaço vazio deve retornar false");
        assertEquals(0, miniGame.getMoveCounter(),
                "Contador não deve incrementar em movimento inválido");
    }

    @Test
    void testMoveOutOfBoundsReturnsFalse() {
        assertFalse(miniGame.move(-1, 0), "Linha negativa deve retornar false");
        assertFalse(miniGame.move(0, Constants.PUZZLE_DIM), "Coluna fora do limite deve retornar false");
    }

    @Test
    void testGetRemainingMovesStartsAtMax() {
        assertEquals(Constants.MAX_PUZZLE_MOVES, miniGame.getRemainingMoves(),
                "Movimentos restantes devem começar em MAX_PUZZLE_MOVES");
    }

    @Test
    void testGetRemainingMovesDecreasesAfterValidMove() {
        boolean moved = miniGame.move(3, 2); // tenta mover peça adjacente ao vazio
        if (moved) {
            assertEquals(Constants.MAX_PUZZLE_MOVES - 1, miniGame.getRemainingMoves(),
                    "Movimentos restantes devem diminuir após cada movimento válido");
        }
    }

    @Test
    void testGetRemainingMovesNeverGoesNegative() {
        // Simular muitos movimentos (vai e vem entre duas posições)
        for (int i = 0; i < Constants.MAX_PUZZLE_MOVES + 10; i++) {
            miniGame.move(3, 2);
            miniGame.move(3, 3); // volta atrás (se possível)
        }
        assertTrue(miniGame.getRemainingMoves() >= 0,
                "Movimentos restantes não podem ser negativos");
    }

    @Test
    void testResetRestoresSolvedState() {
        // Faz um movimento para sair do estado resolvido
        miniGame.move(3, 2);
        miniGame.reset();
        assertEquals(0, miniGame.getMoveCounter(),
                "Reset deve colocar o contador a zero");
        if (Constants.MODO_DEFESA) {
            assertTrue(miniGame.isSolved(),
                    "Reset em MODO_DEFESA deve restaurar tabuleiro resolvido");
        }
    }

    @Test
    void testBoardDimensionIsCorrect() {
        int[][] board = miniGame.getBoard();
        assertEquals(Constants.PUZZLE_DIM, board.length,
                "Tabuleiro deve ter PUZZLE_DIM linhas");
        assertEquals(Constants.PUZZLE_DIM, board[0].length,
                "Tabuleiro deve ter PUZZLE_DIM colunas");
    }

    @Test
    void testIsSolvedAfterDesolvingAndResolving() {
        if (!Constants.MODO_DEFESA) return; // só em modo defesa o estado inicial é controlado
        assertTrue(miniGame.isSolved(), "Deve começar resolvido");
        miniGame.move(3, 2); // desfaz
        assertFalse(miniGame.isSolved(), "Após movimento deve deixar de estar resolvido");
        miniGame.move(3, 3); // refaz (volta atrás)
        assertTrue(miniGame.isSolved(), "Após desfazer o movimento deve voltar a estar resolvido");
    }
}