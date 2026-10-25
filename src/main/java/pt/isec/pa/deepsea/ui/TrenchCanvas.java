package pt.isec.pa.deepsea.ui;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pt.isec.pa.deepsea.model.data.Constants;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.ui.util.ImageManager;


 // TrenchCanvas — Representação visual do fosso

public class TrenchCanvas extends Canvas {

    // Definições de dimensão
    private static final int    ROWS = Constants.FOSSO_P; // Profundidade
    private static final int    COLS = Constants.FOSSO_L; // Largura
    private static final double CELL = 36.0;               // Células maiores que na superfície
    private static final double W    = COLS * CELL;
    private static final double H    = ROWS * CELL;

    // Cores temáticas para o ambiente abissal
    private static final Color C_BG_TOP  = Color.web("#071828"); // Azul escuro
    private static final Color C_BG_BOT  = Color.web("#020c12"); // Quase preto
    private static final Color C_GRID    = Color.web("#0d2a40", 0.5);

    // Cores para os obstáculos e elementos
    private static final Color C_ROCK    = Color.web("#3d4a52");
    private static final Color C_ROCK_HI = Color.web("#5a6a74");
    private static final Color C_ANIMAL  = Color.web("#d4a017");
    private static final Color C_CURRENT = Color.web("#4ab8e8");

    // Cores do Drone e Status
    private static final Color C_DRONE   = Color.web("#64d8cb");
    private static final Color C_DESC    = Color.web("#4aebd4"); // Cor para modo Descida
    private static final Color C_ASC     = Color.web("#f4a44a"); // Cor para modo Subida
    private static final Color C_WARN    = Color.web("#f4c44a"); // Aviso (vida média)
    private static final Color C_DANGER  = Color.web("#e85d3c"); // Perigo (vida baixa)

    private final DeepSeaManager model;

    public TrenchCanvas(DeepSeaManager model) {
        super(W, H);
        this.model = model;
        registerHandlers();
        update();
    }


     // Subscreve eventos do modelo para atualizar o desenho do fosso.

    private void registerHandlers() {
        // Redesenha quando o drone se move (lateralmente ou verticalmente)
        model.addPropertyChangeListener(DeepSeaManager.PROP_DRONE_MOVED,
                evt -> Platform.runLater(this::update));

        // Redesenha se houver alteração de dados ou estado
        model.addPropertyChangeListener(DeepSeaManager.PROP_DATA_CHANGED,
                evt -> Platform.runLater(this::update));
        model.addPropertyChangeListener(DeepSeaManager.PROP_STATE_CHANGED,
                evt -> Platform.runLater(this::update));

        // Garante atualização ao tornar-se visível
        visibleProperty().addListener((obs, old, now) -> {
            if (now) update();
        });
    }

    //Ciclo update
    public void update() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);

        drawBackground(gc); // Fundo gradiente
        drawGrid(gc);       // Grelha
        drawElements(gc);   // Obstáculos e perigos Rochas, etc
        drawDrone(gc);      // Drone
        drawHUD(gc);        // Interface superior Vida/Combustível do Drone
    }

    // Fundo que escurece à medida que o drone desce.
    private void drawBackground(GraphicsContext gc) {
        for (int r = 0; r < ROWS; r++) {
            gc.setFill(C_BG_TOP.interpolate(C_BG_BOT, (double) r / ROWS));
            gc.fillRect(0, r * CELL, W, CELL);
        }
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(C_GRID);
        gc.setLineWidth(0.5);
        for (int r = 0; r <= ROWS; r++) gc.strokeLine(0, r * CELL, W, r * CELL);
        for (int c = 0; c <= COLS; c++) gc.strokeLine(c * CELL, 0, c * CELL, H);
    }

    // Percorre a matriz do modelo e desenha o que está em cada célula
    private void drawElements(GraphicsContext gc) {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                switch (model.getTrenchCellType(r, c)) {
                    case "ROCK"    -> drawRock(gc, r, c);
                    case "ANIMAL"  -> drawAnimal(gc, r, c);
                    case "CURRENT" -> drawCurrent(gc, r, c);
                }
    }

    // --- Métodos de desenho de elementos com Fallback ---

    private void drawRock(GraphicsContext gc, int r, int c) {
        double x = c * CELL, y = r * CELL;
        var img = ImageManager.getImage("rock.png");
        if (img != null) { gc.drawImage(img, x + 1, y + 1, CELL - 2, CELL - 2); return; }

        gc.setFill(C_ROCK);    gc.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);
        gc.setFill(C_ROCK_HI); gc.fillRect(x + 2, y + 2, CELL / 2, 3);
    }

    private void drawAnimal(GraphicsContext gc, int r, int c) {
        double x = c * CELL, y = r * CELL;
        var img = ImageManager.getImage("sea_animal.png");
        if (img != null) { gc.drawImage(img, x + 3, y + 3, CELL - 6, CELL - 6); return; }

        double cx = x + CELL / 2, cy = y + CELL / 2;
        gc.setFill(C_ANIMAL.deriveColor(0, 1, 1, 0.9));
        gc.fillOval(cx - 8, cy - 8, 16, 16);
    }

    private void drawCurrent(GraphicsContext gc, int r, int c) {
        double x = c * CELL, y = r * CELL;
        var img = ImageManager.getImage("current.png");
        if (img != null) { gc.drawImage(img, x + 3, y + 3, CELL - 6, CELL - 6); return; }

        // Desenha ondas se não houver imagem
        double cx = x + CELL / 2, cy = y + CELL / 2;
        gc.setStroke(C_CURRENT); gc.setLineWidth(2.0);
        gc.strokeLine(cx - 10, cy, cx + 10, cy);
    }

    // Desenha o drone na sua posição (R, C).
    private void drawDrone(GraphicsContext gc) {
        int[] pos = model.getDronePosition();
        if (pos[0] < 0 || pos[0] >= ROWS || pos[1] < 0 || pos[1] >= COLS) return;

        double x = pos[1] * CELL, y = pos[0] * CELL;
        var img = ImageManager.getImage("drone.png");

        if (img != null) {
            gc.drawImage(img, x + 3, y + 3, CELL - 6, CELL - 6);
        } else {
            // Desenha um hexágono se a imagem do drone não existir
            double cx = x + CELL / 2, cy = y + CELL / 2, rad = CELL / 2 - 4;
            gc.setFill(C_DRONE);
            gc.fillOval(cx - rad, cy - rad, rad*2, rad*2);
        }

        // Cursor de seleção branco ao redor do drone
        gc.setStroke(Color.WHITE.deriveColor(0, 1, 1, 0.7));
        gc.setLineWidth(1.5);
        gc.strokeRect(x + 1, y + 1, CELL - 2, CELL - 2);
    }

     // Desenha o HUD no topo do canvas.
    private void drawHUD(GraphicsContext gc) {
        boolean desc = model.getCurrentState() == DeepSeaState.DESCEND_STATE;

        // Fundo escuro para leitura do HUD
        gc.setFill(Color.web("#000000", 0.6));
        gc.fillRect(0, 0, W, 52);

        // Título do Estado
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        gc.setFill(desc ? C_DESC : C_ASC);
        gc.fillText(desc ? "↓  DESCIDA" : "↑  SUBIDA", 8, 16);

        // Posição textual
        int[] pos = model.getDronePosition();
        gc.setFont(Font.font("Monospaced", 10));
        gc.setFill(Color.web("#9ab8d4"));
        gc.fillText(String.format("Drone: (%d, %d)", pos[0], pos[1]), 8, 30);

        // Barras de Combustível e Integridade do Drone
        double fuelMax = Constants.FUEL_INICIAL_DRONE;
        double hullMax = Constants.DRONE_HULL_MAX;
        double fuelR   = fuelMax > 0 ? model.getActiveDroneFuel() / fuelMax : 0;
        double hullR   = hullMax > 0 ? model.getActiveDroneHull() / hullMax : 0;

        drawBar(gc, 8,   38, 80, "⛽", fuelR, C_DESC);
        drawBar(gc, 100, 38, 80, "🛡", hullR,
                hullR > 0.5 ? C_DESC : hullR > 0.25 ? C_WARN : C_DANGER);

        // Instruções no fundo do canvas
        gc.setFont(Font.font("Monospaced", 9));
        gc.setFill(Color.web("#6a8aa8"));
        gc.fillText("←/→ mover  |  ↓/Enter → fundo", 4, H - 6);
    }

    // Método auxiliar para desenhar barras de progresso Health/Fuel.
    private void drawBar(GraphicsContext gc, double x, double y, double bw,
                         String icon, double ratio, Color color) {
        gc.setFont(Font.font(10));
        gc.setFill(Color.web("#9ab8d4"));
        gc.fillText(icon, x, y + 8);

        gc.setFill(Color.web("#1a3050")); // Fundo da barra
        gc.fillRoundRect(x + 16, y, bw, 8, 3, 3);

        gc.setFill(color);
        gc.fillRoundRect(x + 16, y, bw * Math.max(0, Math.min(1, ratio)), 8, 3, 3);
    }
}