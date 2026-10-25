package pt.isec.pa.deepsea.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import pt.isec.pa.deepsea.model.data.Constants;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;

/**
 * InfoPanel — Painel lateral esquerdo que exibe estatísticas em tempo real.
 * Estende VBox para organizar os elementos verticalmente.
 */
public class InfoPanel extends VBox {

    private final DeepSeaManager model; // Referência para o modelo (FSM)

    // Elementos da interface que precisam de ser atualizados dinamicamente
    private Label       lblState;       // Exibe o estado atual (ex: IDLE, EXPLORE)
    private ProgressBar pbFuel;        // Barra visual de combustível do navio
    private Label       lblFuelVal;     // Texto descritivo do combustível (valor e %)
    private Label       lblDrones;      // Quantidade de drones operacionais
    private Label       lblArtifacts;   // Progresso de recolha de artefactos
    private Label       lblMinerals;    // Quantidade de minérios no navio

    public InfoPanel(DeepSeaManager model) {
        this.model = model;

        createViews();      // Configura os componentes visuais
        registerHandlers(); // Configura os ouvintes
        update();
    }

    /**
     * Cria e organiza todos os elementos visuais do painel.
     */
    private void createViews() {
        setPrefWidth(210); setMinWidth(210);
        setPadding(new Insets(16, 12, 16, 12)); // Espaçamento interno
        setSpacing(8); // Espaço entre os elementos do VBox
        getStyleClass().add("side-panel"); // Classe CSS para estilização externa

        Label title = new Label("MISSÃO");
        title.getStyleClass().add("panel-title");

        lblState = new Label();
        lblState.getStyleClass().add("panel-state");

        // Secção de Combustível
        Label lFuelT = new Label("Combustível do navio");
        lFuelT.getStyleClass().add("panel-label");
        pbFuel = new ProgressBar(1.0);
        pbFuel.setPrefWidth(186); pbFuel.setMaxWidth(Double.MAX_VALUE);
        pbFuel.getStyleClass().add("fuel-bar");
        lblFuelVal = new Label();
        lblFuelVal.getStyleClass().add("panel-value-small");

        // Secção de Drones
        Label lDronesT = new Label("Drones operacionais");
        lDronesT.getStyleClass().add("panel-label");
        lblDrones = new Label();
        lblDrones.getStyleClass().add("panel-value");

        // Secção de Artefactos
        Label lArtT = new Label("Artefactos recolhidos");
        lArtT.getStyleClass().add("panel-label");
        lblArtifacts = new Label();
        lblArtifacts.getStyleClass().add("panel-value");

        // Secção de Minérios
        Label lMinT = new Label("Minérios acumulados");
        lMinT.getStyleClass().add("panel-label");
        lblMinerals = new Label();
        lblMinerals.getStyleClass().add("panel-value");

        // Adiciona todos os elementos e separadores ao VBox
        getChildren().addAll(
                title, lblState, new Separator(),
                lFuelT, pbFuel, lblFuelVal, new Separator(),
                lDronesT, lblDrones, new Separator(),
                lArtT, lblArtifacts, new Separator(),
                lMinT, lblMinerals
        );
    }

    private void registerHandlers() {
        // Notificação quando o estado do jogo muda (ex: de IDLE para DESCEND) [cite: 8]
        model.addPropertyChangeListener(DeepSeaManager.PROP_STATE_CHANGED,
                evt -> Platform.runLater(this::update));

        // Notificação quando dados internos mudam (combustível, itens, etc.) [cite: 10]
        model.addPropertyChangeListener(DeepSeaManager.PROP_DATA_CHANGED,
                evt -> Platform.runLater(this::update));
    }

    /**
     * Lê os dados atuais do modelo e atualiza os componentes visuais.
     */
    public void update() {
        // Atualização do Nome do Estado e Cores Dinâmicas
        String st = model.getCurrentState().toString()
                .replace("_STATE","").replace("_"," ");
        lblState.setText(st);

        // Altera a cor do texto dependendo do estado atual do jogo
        lblState.setStyle(switch (model.getCurrentState()) {
            case IDLE_STATE      -> "-fx-text-fill:#4aebd4;";
            case DESCEND_STATE   -> "-fx-text-fill:#4ab8e8;";
            case EXPLORE_STATE   -> "-fx-text-fill:#a8d8ea;";
            case PUZZLE_STATE    -> "-fx-text-fill:#f4c44a;";
            case ASCEND_STATE    -> "-fx-text-fill:#82c8a0;";
            case WIN_STATE       -> "-fx-text-fill:#7eff7e;-fx-font-weight:bold;";
            case GAME_OVER_STATE -> "-fx-text-fill:#ff6b4a;-fx-font-weight:bold;";
            default              -> "-fx-text-fill:#9ab8d4;";
        });

        // 2. Atualização da Barra de Combustível
        double fuel  = model.getShipFuel();
        double max   = Constants.SHIP_FUEL_MAX;
        double ratio = max > 0 ? fuel / max : 0;

        pbFuel.setProgress(ratio);
        lblFuelVal.setText(String.format("%.0f / %.0f  (%.0f%%)", fuel, max, ratio * 100));

        // Gestão de cores da barra (verde, amarelo, vermelho) via classes CSS
        pbFuel.getStyleClass().removeAll("fuel-low","fuel-mid","fuel-ok");
        if      (ratio < 0.25) pbFuel.getStyleClass().add("fuel-low");
        else if (ratio < 0.50) pbFuel.getStyleClass().add("fuel-mid");
        else                   pbFuel.getStyleClass().add("fuel-ok");

        // Atualização de Drones
        int dronesOk = model.getAvailableDrones();
        lblDrones.setText(dronesOk + " / " + Constants.DRONES_INICIAIS);
        if (dronesOk == 0)
            lblDrones.setStyle("-fx-text-fill:#ff6b4a;-fx-font-weight:bold;");
        else
            lblDrones.setStyle("-fx-text-fill:#c8d8e8;");

        // Atualização de Artefactos
        if (model.allArtifactsCollected()) {
            lblArtifacts.setText("✓ Todos recolhidos!");
            lblArtifacts.setStyle("-fx-text-fill:#7eff7e;-fx-font-weight:bold;");
        } else {
            lblArtifacts.setText("? / " + Constants.NUM_ARTEFACTOS);
            lblArtifacts.setStyle("-fx-text-fill:#c8d8e8;");
        }

        // Atualização de Minérios
        double min = model.getShipMinerals();
        lblMinerals.setText(String.format("%.1f unidades", min));
        lblMinerals.setStyle(min > 0 ? "-fx-text-fill:#f4c44a;" : "-fx-text-fill:#6a8aa8;");
    }
}