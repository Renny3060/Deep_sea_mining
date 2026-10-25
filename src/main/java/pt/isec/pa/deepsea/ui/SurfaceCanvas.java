package pt.isec.pa.deepsea.ui;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pt.isec.pa.deepsea.model.data.Constants;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;
import pt.isec.pa.deepsea.ui.util.ImageManager;

import java.util.List;

/**
 * SurfaceCanvas — Representação visual da superfície do oceano.
 * Estende Canvas para permitir desenho direto de alta performance.
 */
public class SurfaceCanvas extends Canvas {

    // Configurações da Grelha baseadas nas constantes do modelo
    private static final int    ROWS        = Constants.SURFACE_L; // 15 linhas
    private static final int    COLS        = Constants.SURFACE_C; // 30 colunas
    private static final double CELL        = 28.0;                // Tamanho de cada célula em pixeis
    private static final double W           = COLS * CELL;         // Largura total
    private static final double H           = ROWS * CELL;         // Altura total
    private static final int    HEAT_RADIUS = 4;                   // Alcance visual do calor dos artefactos

    // Paleta de cores para o ambiente marítimo e heatmap
    private static final Color C_SEA_TOP = Color.web("#061828"); // Azul escuro (topo)
    private static final Color C_SEA_BOT = Color.web("#0a2a4a"); // Azul mais profundo (fundo)
    private static final Color C_GRID    = Color.web("#102a40", 0.6); // Cor das linhas da grelha
    private static final Color C_HEAT_LO = Color.web("#1a7a3e"); // Verde (calor baixo)
    private static final Color C_HEAT_HI = Color.web("#ff6600"); // Laranja/Vermelho (calor alto)
    private static final Color C_SHIP    = Color.web("#e8c96a"); // Dourado para o navio (fallback)

    private final DeepSeaManager model;
    private List<int[]> cachedArtifacts = null; // Cache para não pedir posições ao modelo em todos os frames

    public SurfaceCanvas(DeepSeaManager model) {
        super(W, H); // Define o tamanho do canvas
        this.model = model;
        registerHandlers();
        update();
    }

    /**
     * Regista ouvintes para reagir a mudanças no estado do jogo.
     */
    private void registerHandlers() {
        // Redesenha sempre que o navio mudar de posição no modelo
        model.addPropertyChangeListener(DeepSeaManager.PROP_SHIP_MOVED,
                evt -> Platform.runLater(this::update));

        // Se os dados gerais mudarem, limpa o cache de artefactos e redesenha
        model.addPropertyChangeListener(DeepSeaManager.PROP_DATA_CHANGED,
                evt -> Platform.runLater(() -> { cachedArtifacts = null; update(); }));

        // Se o canvas voltar a ficar visível, força uma atualização
        visibleProperty().addListener((obs, old, now) -> {
            if (now) { cachedArtifacts = null; update(); }
        });
    }

    /**
     * Ciclo principal de desenho. Limpa o canvas e desenha as camadas (layers).
     */
    public void update() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, W, H); // Limpa o frame anterior

        drawBackground(gc); // 1. Gradiente do mar
        drawGrid(gc);       // 2. Linhas da grelha
        drawHeatmap(gc);    // 3. Manchas de calor (detetar artefactos)
        drawShip(gc);       // 4. Navio (imagem ou forma geométrica)
        drawLegend(gc);     // 5. Interface de ajuda
    }

    /** Desenha um gradiente vertical para simular a profundidade do oceano. */
    private void drawBackground(GraphicsContext gc) {
        for (int r = 0; r < ROWS; r++) {
            // Interpola a cor entre o azul de cima e o azul de baixo
            gc.setFill(C_SEA_TOP.interpolate(C_SEA_BOT, (double) r / ROWS));
            gc.fillRect(0, r * CELL, W, CELL);
        }
    }

    /** Desenha as linhas horizontais e verticais da grelha. */
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(C_GRID);
        gc.setLineWidth(0.5);
        for (int r = 0; r <= ROWS; r++) gc.strokeLine(0, r * CELL, W, r * CELL);
        for (int c = 0; c <= COLS; c++) gc.strokeLine(c * CELL, 0, c * CELL, H);
    }

    /**
     *  Calcula a intensidade de "calor" em cada célula
     * baseada na proximidade dos artefactos escondidos para o Mapa de Calor
     */
    private void drawHeatmap(GraphicsContext gc) {
        if (cachedArtifacts == null)
            cachedArtifacts = model.getArtifactSurfacePositions();

        if (cachedArtifacts.isEmpty()) return;

        double[][] intensity = new double[ROWS][COLS];

        // Para cada artefacto, espalha calor nas células vizinhas num raio de HEAT_RADIUS
        for (int[] pos : cachedArtifacts) {
            for (int dr = -HEAT_RADIUS; dr <= HEAT_RADIUS; dr++) {
                for (int dc = -HEAT_RADIUS; dc <= HEAT_RADIUS; dc++) {
                    int r = pos[0] + dr, c = pos[1] + dc;
                    if (r < 0 || r >= ROWS || c < 0 || c >= COLS) continue;

                    double dist = Math.sqrt(dr * dr + dc * dc);
                    // Quanto mais perto do centro, maior a contribuição
                    double contrib = Math.max(0, 1.0 - dist / (HEAT_RADIUS + 1));
                    intensity[r][c] = Math.min(1.0, intensity[r][c] + contrib);
                }
            }
        }

        // Desenha os quadrados de calor com base na intensidade calculada
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                double v = intensity[r][c];
                if (v < 0.05) continue; // Ignora intensidades muito baixas

                // Transita de verde para laranja conforme a intensidade
                gc.setFill(C_HEAT_LO.interpolate(C_HEAT_HI, v).deriveColor(0, 1, 1, v * 0.5));
                gc.fillRect(c * CELL, r * CELL, CELL, CELL);
            }
    }

    /** Desenha o navio na sua posição atual. */
    private void drawShip(GraphicsContext gc) {
        int[] pos = model.getShipPosition();
        double x = pos[1] * CELL, y = pos[0] * CELL;

        // Tenta usar a imagem carregada pelo ImageManager
        var img = ImageManager.getImage("ship.png");
        if (img != null) {
            gc.drawImage(img, x + 2, y + 2, CELL - 4, CELL - 4);
        } else {
            // Desenho de reserva (triângulo) caso a imagem falhe
            double cx = x + CELL / 2, cy = y + CELL / 2;
            gc.setFill(C_SHIP);
            gc.fillPolygon(new double[]{cx, cx - 9, cx + 9},
                    new double[]{cy - 10, cy + 7, cy + 7}, 3);
        }

        // Realce visual (retângulo branco) à volta da célula do navio
        gc.setStroke(Color.WHITE.deriveColor(0, 1, 1, 0.8));
        gc.setLineWidth(1.5);
        gc.strokeRect(x + 1, y + 1, CELL - 2, CELL - 2);
    }

    /** Desenha uma legenda flutuante com os controlos e coordenadas. */
    private void drawLegend(GraphicsContext gc) {
        double lx = W - 148, ly = H - 50;

        // Fundo semitransparente da legenda
        gc.setFill(Color.web("#000000", 0.55));
        gc.fillRoundRect(lx - 4, ly - 4, 148, 46, 6, 6);

        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 9));
        gc.setFill(Color.web("#ffffff", 0.55));
        gc.fillText("■ Artefacto (calor)", lx, ly + 12);
        gc.fillText("△ Navio", lx, ly + 24);
        gc.fillText("↑↓←→  Mover navio", lx, ly + 36);

        // Mostra as coordenadas atuais (R, C) no canto inferior esquerdo
        int[] pos = model.getShipPosition();
        gc.setFont(Font.font("Monospaced", 10));
        gc.fillText(String.format("(%d, %d)", pos[0], pos[1]), 6, H - 6);
    }
}