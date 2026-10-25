package pt.isec.pa.deepsea.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;

public class PuzzleCanvas extends Canvas {
    private final DeepSeaManager manager;
    private static final int CELL_SIZE = 80; // Tamanho em pixéis de cada quadrado do puzzle

    public PuzzleCanvas(DeepSeaManager manager) {
        // Cria um Canvas de tamanho fixo para o tabuleiro 4x4
        super(4 * CELL_SIZE, 4 * CELL_SIZE);
        this.manager = manager;
        this.setFocusTraversable(true);

        // Captura o clique do rato e converte em coordenada linear
        this.setOnMouseClicked(e -> {
            int col = (int) (e.getX() / CELL_SIZE);
            int row = (int) (e.getY() / CELL_SIZE);

            if (col >= 0 && col < 4 && row >= 0 && row < 4) {
                int piecePosition = row * 4 + col; // Fórmula pedida no guião
                manager.movePiece(piecePosition);
                render();
            }
        });
    }

    public void render() {
        GraphicsContext gc = this.getGraphicsContext2D();
        int[][] board = manager.getPuzzleBoard();

        // Limpar o fundo do puzzle
        gc.setFill(Color.web("#2c3e50"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        if (board == null) return;

        gc.setFont(new Font("Arial Black", 24));
        gc.setTextAlign(TextAlignment.CENTER);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int value = board[r][c];

                if (value == 0) {
                    // Espaço vazio
                    gc.setFill(Color.web("#34495e"));
                    gc.fillRect(c * CELL_SIZE + 2, r * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                } else {
                    // Peça numerada com highlight
                    gc.setFill(Color.web("#3498db"));
                    gc.fillRect(c * CELL_SIZE + 2, r * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);

                    // Contorno interno da peça
                    gc.setStroke(Color.web("#5faee3"));
                    gc.setLineWidth(2);
                    gc.strokeRect(c * CELL_SIZE + 4, r * CELL_SIZE + 4, CELL_SIZE - 8, CELL_SIZE - 8);

                    // Desenhar o número centrado
                    gc.setFill(Color.WHITE);
                    gc.fillText(String.valueOf(value), c * CELL_SIZE + (CELL_SIZE / 2.0), r * CELL_SIZE + (CELL_SIZE / 2.0) + 9);
                }
            }
        }
    }
}