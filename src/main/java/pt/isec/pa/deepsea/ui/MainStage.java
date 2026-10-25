package pt.isec.pa.deepsea.ui;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;

public class MainStage extends Stage {
    private final DeepSeaManager manager;

    private Canvas surfaceCanvas;
    private TrenchCanvas trenchCanvas;
    private FloorCanvas floorCanvas;
    private PuzzleCanvas puzzleCanvas;

    private StackPane centerStack;

    public MainStage(DeepSeaManager manager) {
        this.manager = manager;
        this.setTitle("Deep Sea Mining Simulator");

        BorderPane root = new BorderPane();

        LogWindow logWindow = new LogWindow(manager);
        AppMenu menuBar = new AppMenu(manager, logWindow);
        root.setTop(menuBar);

        centerStack = new StackPane();

        // Canvas temporário para simular a superfície
        surfaceCanvas = new Canvas(600, 400);
        trenchCanvas = new TrenchCanvas(manager);
        floorCanvas = new FloorCanvas(manager);
        puzzleCanvas = new PuzzleCanvas(manager);
        centerStack.getChildren().addAll(surfaceCanvas, trenchCanvas, floorCanvas, puzzleCanvas);
        root.setCenter(centerStack);

        DroneStatusPanel droneStatus = new DroneStatusPanel(manager);
        root.setRight(droneStatus);

        this.setScene(new Scene(root, 950, 650));

        // Sincronização automática com a máquina de estados
        manager.addPropertyChangeListener("stateChanged", evt -> showCanvas(manager.getCurrentState()));

        showCanvas(manager.getCurrentState());
    }

    private void showCanvas(DeepSeaState state) {
        surfaceCanvas.setVisible(false);
        trenchCanvas.setVisible(false);
        floorCanvas.setVisible(false);
        puzzleCanvas.setVisible(false);

        switch (state) {
            case IDLE_STATE -> {
                surfaceCanvas.setVisible(true);
                surfaceCanvas.requestFocus();
            }
            case DESCEND_STATE, ASCEND_STATE -> {
                trenchCanvas.setVisible(true);
                trenchCanvas.update();
                trenchCanvas.requestFocus();
            }
            case EXPLORE_STATE -> {
                floorCanvas.setVisible(true);
                floorCanvas.render();
                floorCanvas.requestFocus();
            }
            case PUZZLE_STATE -> {
                puzzleCanvas.setVisible(true);
                puzzleCanvas.render();
                puzzleCanvas.requestFocus();
            }
            case WIN_STATE, GAME_OVER_STATE -> {

            }
        }
    }
}