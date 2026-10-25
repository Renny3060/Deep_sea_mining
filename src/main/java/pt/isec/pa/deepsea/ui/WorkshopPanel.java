package pt.isec.pa.deepsea.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import pt.isec.pa.deepsea.model.data.Constants;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;

/**
 * WorkshopPanel — Painel lateral direito para gestão e melhoria de drones.
 * Este painel só está ativo quando o jogo está no estado IDLE.
 */
public class WorkshopPanel extends VBox {

    private final DeepSeaManager model;

    // Grupo de botões para selecionar apenas um drone de cada vez
    private final ToggleGroup  droneToggle = new ToggleGroup();
    private final ToggleButton[] droneBtns = new ToggleButton[Constants.DRONES_INICIAIS];
    private int selectedIndex = 0; // Índice do drone selecionado na interface

    // Elementos visuais de estado
    private Label       lblDroneName;
    private ProgressBar pbFuel, pbHull;
    private Label       lblFuelVal, lblHullVal;

    // Controlos de ação
    private Spinner<Integer> spinFuel; // Seletor de quantidade de combustível a transferir
    private Button btnRefuel, btnRepair, btnTank, btnArmor, btnLaunch;

    public WorkshopPanel(DeepSeaManager model) {
        this.model = model;
        createViews();
        registerHandlers();
        update();
    }

    // Constrói a interface da oficina organizada em secções.
    private void createViews() {
        setPrefWidth(220); setMinWidth(220);
        setPadding(new Insets(14, 12, 14, 12));
        setSpacing(10);
        getStyleClass().add("side-panel");

        Label title = new Label("OFICINA");
        title.getStyleClass().add("panel-title");

        // Secção de Seleção: Cria botões D1, D2, D3...
        Label lSel = new Label("Selecionar drone");
        lSel.getStyleClass().add("panel-label");
        HBox droneRow = new HBox(6);
        droneRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < Constants.DRONES_INICIAIS; i++) {
            ToggleButton tb = new ToggleButton("D" + (i+1));
            tb.setToggleGroup(droneToggle);
            tb.setPrefWidth(60);
            tb.getStyleClass().add("drone-btn");
            droneBtns[i] = tb;
            droneRow.getChildren().add(tb);
        }
        droneBtns[0].setSelected(true); // Seleciona o primeiro drone por defeito

        // Secção de Status: Barras de vida e combustível do drone selecionado
        Label lStatus = new Label("Status do drone");
        lStatus.getStyleClass().add("panel-label");
        lblDroneName = new Label();
        lblDroneName.getStyleClass().add("panel-value");

        // Barra de Combustível do Drone
        Label lF = new Label("Combustível"); lF.getStyleClass().add("panel-label");
        pbFuel = new ProgressBar(1.0);
        pbFuel.setPrefWidth(196);
        lblFuelVal = new Label(); lblFuelVal.getStyleClass().add("panel-value-small");

        // Barra de Integridade do Casco (Hull)
        Label lH = new Label("Casco"); lH.getStyleClass().add("panel-label");
        pbHull = new ProgressBar(1.0);
        pbHull.setPrefWidth(196);
        lblHullVal = new Label(); lblHullVal.getStyleClass().add("panel-value-small");

        // Secção de Manutenção: Abastecer e Reparar
        Label lVol = new Label("Volume a transferir");
        lVol.getStyleClass().add("panel-label");
        spinFuel = new Spinner<>(1, 500, 50, 10); // Min, Max, Inicial, Passo
        spinFuel.setEditable(true); spinFuel.setPrefWidth(196);

        btnRefuel = new Button("⛽  Abastecer");
        btnRefuel.setMaxWidth(Double.MAX_VALUE);

        btnRepair = new Button("🔧  Reparar casco");
        btnRepair.setMaxWidth(Double.MAX_VALUE);

        // Secção de Upgrades: Melhorar Tanque ou Blindagem
        Label lUpg = new Label("Melhorias");
        lUpg.getStyleClass().add("panel-label");
        HBox upgRow = new HBox(6);
        btnTank  = new Button("▲ Tanque");  btnTank.setPrefWidth(94);
        btnArmor = new Button("🛡 Blindagem"); btnArmor.setPrefWidth(94);
        upgRow.getChildren().addAll(btnTank, btnArmor);

        // Botão Principal: Lançar o drone para o fosso
        btnLaunch = new Button("🚀  LANÇAR DRONE");
        btnLaunch.setMaxWidth(Double.MAX_VALUE);
        btnLaunch.setMinHeight(42);
        btnLaunch.getStyleClass().add("launch-btn");

        // Montagem final do painel
        getChildren().addAll(
                title, new Separator(),
                lSel, droneRow, new Separator(),
                lStatus, lblDroneName,
                lF, pbFuel, lblFuelVal,
                lH, pbHull, lblHullVal, new Separator(),
                lVol, spinFuel, btnRefuel, btnRepair, new Separator(),
                lUpg, upgRow,
                new Separator(), btnLaunch
        );
    }

    //Define o comportamento dos botões.
    private void registerHandlers() {
        // Handlers para seleção de drone
        for (int i = 0; i < Constants.DRONES_INICIAIS; i++) {
            final int idx = i;
            droneBtns[i].setOnAction(e -> { selectedIndex = idx; update(); });
        }

        // Ações de manutenção chamam métodos do modelo e validam sucesso
        btnRefuel.setOnAction(e -> {
            if (!model.refuelDrone(getDroneId(), spinFuel.getValue()))
                warn("Não foi possível abastecer.\nVerifique combustível do navio.");
            update();
        });

        btnRepair.setOnAction(e -> {
            if (!model.repairDrone(getDroneId()))
                warn("Não foi possível reparar.\nCusto: " + Constants.FUEL_REPAIR_COST + " combustível.");
            update();
        });

        btnTank.setOnAction(e -> {
            if (!model.upgradeDrone(getDroneId(), "TANK"))
                warn("Não foi possível melhorar o tanque.");
            update();
        });

        btnArmor.setOnAction(e -> {
            if (!model.upgradeDrone(getDroneId(), "ARMOR"))
                warn("Não foi possível melhorar a blindagem.");
            update();
        });

        // Lançamento: se for bem sucedido, o modelo muda de estado e a UI reage
        btnLaunch.setOnAction(e -> {
            if (!model.descendDrone(getDroneId()))
                warn("Não foi possível lançar o drone.");
        });

        // Observers para atualizar o painel quando o jogo muda
        model.addPropertyChangeListener(DeepSeaManager.PROP_DATA_CHANGED,
                evt -> Platform.runLater(this::update));
        model.addPropertyChangeListener(DeepSeaManager.PROP_STATE_CHANGED,
                evt -> Platform.runLater(this::update));
    }

    // Atualiza o estado visual do painel com base no modelo.
    public void update() {
        // A oficina só funciona se o navio no estado IDLE
        boolean isIdle = model.getCurrentState() == DeepSeaState.IDLE_STATE;
        setDisable(!isIdle);

        // Atualiza a disponibilidade dos botões de drone se um drone foi perdido, fica a vermelho
        int available = model.getAvailableDrones();
        for (int i = 0; i < Constants.DRONES_INICIAIS; i++) {
            boolean ok = i < available;
            droneBtns[i].setDisable(!ok);
            droneBtns[i].setStyle(ok ? "-fx-text-fill:#c8d8e8;" : "-fx-text-fill:#ff6b4a;");
        }

        lblDroneName.setText("Drone #" + (selectedIndex + 1));

        // Atualiza as barras de estado
        updateBar(pbFuel, lblFuelVal, 1.0, "100%");
        updateBar(pbHull, lblHullVal, 1.0, "100%");

        // Bloqueia o lançamento se não houver drones ou se não estiver no estado IDLE
        btnLaunch.setDisable(!isIdle || available == 0);
    }

    /** Atualiza o progresso e a cor da barra (Verde/Amarelo/Vermelho). */
    private void updateBar(ProgressBar pb, Label lbl, double ratio, String text) {
        pb.setProgress(ratio);
        lbl.setText(text);
        pb.getStyleClass().removeAll("fuel-low","fuel-mid","fuel-ok");
        if      (ratio < 0.25) pb.getStyleClass().add("fuel-low");
        else if (ratio < 0.50) pb.getStyleClass().add("fuel-mid");
        else                   pb.getStyleClass().add("fuel-ok");
    }

    private int getDroneId() { return selectedIndex + 1; }

    private void warn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}