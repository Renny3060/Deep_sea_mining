package pt.isec.pa.deepsea.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;

public class DroneStatusPanel extends VBox {
    private final DeepSeaManager manager;
    private final Label lblId = new Label("Drone: Ninguém em missão");
    private final ProgressBar pbFuel = new ProgressBar(0);
    private final ProgressBar pbHull = new ProgressBar(0);
    private final Label lblPos = new Label("Posição: --");

    public DroneStatusPanel(DeepSeaManager manager) {
        this.manager = manager;
        this.setPadding(new Insets(15));
        this.setSpacing(10);
        this.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-min-width: 200px;");

        Label title = new Label("STATUS DO DRONE");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        this.getChildren().addAll(
                title,
                lblId,
                new Label("Combustível:"), pbFuel,
                new Label("Integridade do Casco:"), pbHull,
                lblPos
        );

        pbFuel.setPrefWidth(150);
        pbHull.setPrefWidth(150);

        manager.addPropertyChangeListener(DeepSeaManager.PROP_STATE_CHANGED, evt -> updateUI());
        manager.addPropertyChangeListener(DeepSeaManager.PROP_DRONE_MOVED, evt -> updateUI());
        manager.addPropertyChangeListener(DeepSeaManager.PROP_DATA_CHANGED, evt -> updateUI());

        updateUI();
    }

    private void updateUI() {
        DeepSeaState state = manager.getCurrentState();

        // O painel só deve estar visível se o drone estiver de facto na água
        if (state == DeepSeaState.DESCEND_STATE || state == DeepSeaState.EXPLORE_STATE ||
                state == DeepSeaState.PUZZLE_STATE  || state == DeepSeaState.ASCEND_STATE) {

            this.setVisible(true);
            lblId.setText("Drone Ativo");

            double fuel = manager.getActiveDroneFuel(); // Devolve valor real
            double hull = manager.getActiveDroneHull(); // Devolve valor real

            pbFuel.setProgress(fuel);
            pbHull.setProgress(hull);

            int[] dronePos = manager.getDronePosition();
            if (dronePos != null && dronePos[0] != -1) {
                lblPos.setText(String.format("X: %d | Y: %d", dronePos[0], dronePos[1]));
            } else {
                lblPos.setText("Posição: A transitar...");
            }
        } else {
            // Se o navio está IDLE, não há drone ativo para monitorizar
            this.setVisible(false);
        }
    }
}