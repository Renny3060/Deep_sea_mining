package pt.isec.pa.deepsea.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;
import pt.isec.pa.deepsea.model.data.Constants;
import pt.isec.pa.deepsea.model.data.FloorCell;
import pt.isec.pa.deepsea.ui.res.ImageManager;

public class FloorCanvas extends Canvas {
    private final DeepSeaManager manager;
    private static final int CELL_SIZE = 40;


    public FloorCanvas(DeepSeaManager manager) {
        super(Constants.FUNDO_DIM * CELL_SIZE, Constants.FUNDO_DIM * CELL_SIZE);
        this.manager = manager;

        this.setFocusTraversable(true);

        // Captura de teclas corrigida para usar o método moveDrone(dx, dy)
        this.setOnKeyPressed(e -> {
            boolean acted = false;
            if (e.getCode() == KeyCode.LEFT)  acted = manager.moveDroneSeabed(-1, 0);
            if (e.getCode() == KeyCode.RIGHT) acted = manager.moveDroneSeabed(1, 0);
            if (e.getCode() == KeyCode.UP)    acted = manager.moveDroneSeabed(0, -1);
            if (e.getCode() == KeyCode.DOWN)  acted = manager.moveDroneSeabed(0, 1);

            // Tecla opcional para recolher minério se quiseres mapear
            if (e.getCode() == KeyCode.G)     acted = manager.collectMineral();

            // Tecla opcional para recolher artefacto / iniciar puzzle
            if (e.getCode() == KeyCode.A)     acted = manager.collectArtifact();

            if (acted) {
                render();
            }
        });

        // Atualizar automaticamente quando o drone se mover ou os dados mudarem
        manager.addPropertyChangeListener(DeepSeaManager.PROP_DRONE_MOVED, e -> render());
        manager.addPropertyChangeListener(DeepSeaManager.PROP_DATA_CHANGED, e -> render());
    }

    public void render() {
        GraphicsContext gc = this.getGraphicsContext2D();
        int dim = Constants.FUNDO_DIM;
        int[] shipPos = manager.getShipPosition();
        int shipRow = shipPos[0]; // ou conforme o teu modelo (linha do navio)
        int shipCol = shipPos[1]; // (coluna do navio)

        // Limpar o Canvas antes de desenhar
        gc.clearRect(0, 0, getWidth(), getHeight());

        for (int r = 0; r < dim; r++) {
            for (int c = 0; c < dim; c++) {
                FloorCell cell = manager.getFloorCell(shipRow, shipCol, r, c);

                // Desenhar o fundo
                gc.setFill(Color.DARKBLUE);
                gc.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                gc.setStroke(Color.BLUE);
                gc.strokeRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                if (cell != null) {
                    String symbol = cell.toString();

                    if (symbol.startsWith("MI")) {
                        // Se o ImageManager ainda não estiver pronto, podes usar gc.fillText("M", ...) temporariamente
                        var img = ImageManager.getImage("mineral.png");
                        if (img != null) gc.drawImage(img, c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    } else if (symbol.contains("(")) {
                        var img = ImageManager.getImage("artifact.png");
                        if (img != null) gc.drawImage(img, c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    } else if (symbol.equals("MO")) {
                        var img = ImageManager.getImage("monster.png");
                        if (img != null) gc.drawImage(img, c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }

        // Desenhar o Drone na sua posição atual
        int[] dronePos = manager.getDronePosition();
        if (dronePos != null && dronePos[0] != -1 && dronePos[1] != -1) {
            int dRow = dronePos[1]; // Linha
            int dCol = dronePos[0]; // Coluna

            var droneImg = ImageManager.getImage("drone.png");
            if (droneImg != null) {
                gc.drawImage(droneImg, dCol * CELL_SIZE, dRow * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            } else {
                // Desenho de recurso caso não encontre a imagem
                gc.setFill(Color.RED);
                gc.fillOval(dCol * CELL_SIZE + 5, dRow * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            }
        }
    }
}