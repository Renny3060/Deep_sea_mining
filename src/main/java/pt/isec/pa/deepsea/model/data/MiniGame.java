package pt.isec.pa.deepsea.model.data;

import java.io.Serializable;
import java.util.*;

/**
 * Puzzle 4×4 com 15 peças e um espaço vazio (valor 0).
 * */
public class MiniGame implements Serializable {

    private int[][] board;
    private int moveCounter;
    private static final int DIM = Constants.PUZZLE_DIM; // 4

    public MiniGame() {
        board = new int[DIM][DIM];
        moveCounter = 0;
        initializeBoard();
        // Se não estiver em modo defesao jogo é baralhado no inicio
        if (!Constants.MODO_DEFESA) shuffle(); // só baralha fora do modo defesa
    }

    // ── Setup ─────────────────────────────────────────────────────
    // Inicializa o tabuleiro
    private void initializeBoard() {
        int value = 1;
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++)
                board[i][j] = (i == DIM - 1 && j == DIM - 1) ? 0 : value++;
    }

    // Baralha as peças de forma aleatória
    public void shuffle() {
        List<Integer> pieces = new ArrayList<>();
        for (int i = 0; i < DIM * DIM; i++) pieces.add(i);
        Collections.shuffle(pieces);
        int index = 0;
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++)
                board[i][j] = pieces.get(index++);
        moveCounter = 0; // Reinicia o contador
    }

    /**
     * Reinicia o puzzle para nova tentativa de recolha de artefacto.
     *
     * CORREÇÃO: agora respeita MODO_DEFESA — em modo de defesa o tabuleiro
     * volta ao estado resolvido (1..15, 0 no canto), sem baralhar.
     * Antes, reset() chamava shuffle() incondicionalmente, o que causava
     * isSolved()=false mesmo em MODO_DEFESA.
     */
    public void reset() {
        initializeBoard();
        if (!Constants.MODO_DEFESA) shuffle(); // CORREÇÃO: igual ao construtor
        moveCounter = 0;
    }

    // ── Lógica ────────────────────────────────────────────────────

    /**
     * Move a peça na posição (row, col) para o espaço vazio adjacente.
     * @return true se o movimento foi válido
     */
    public boolean move(int row, int col) {
        // Verifica se as coordenadas são válidas
        if (row < 0 || row >= DIM || col < 0 || col >= DIM) return false;
        // Procurar o espaço vazio nas posições Cima, Baixo, Esquerda, Direita
        int[][] adj = {{row-1,col},{row+1,col},{row,col-1},{row,col+1}};
        for (int[] pos : adj) {
            int r = pos[0], c = pos[1];
            // Se a posição for válida e o espaço vazio
            if (r >= 0 && r < DIM && c >= 0 && c < DIM && board[r][c] == 0) {
                // Troca a peça pelo espaço vazio
                board[r][c] = board[row][col];
                board[row][col] = 0;
                moveCounter++;
                return true;
            }
        }
        return false;
    }

    /** true quando as peças estão ordenadas 1..15 com 0 no canto [3][3]. */
    // Verifica se o puzzle está resolvido
    public boolean isSolved() {
        int expected = 1;
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                if (i == DIM - 1 && j == DIM - 1) return board[i][j] == 0;
                if (board[i][j] != expected++) return false;
            }
        return true;
    }

    // ── Getters ───────────────────────────────────────────────────

    public int[][] getBoard()        { return board; }
    public int getMoveCounter()      { return moveCounter; }

    /**
     * Movimentos restantes até ao limite MAX_PUZZLE_MOVES.
     * Quando chega a 0, o PuzzleState transita para ASCEND_STATE.
     */
    public int getRemainingMoves() {
        return Math.max(0, Constants.MAX_PUZZLE_MOVES - moveCounter);
    }
}