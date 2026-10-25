package pt.isec.pa.deepsea.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;

import java.io.File;

/**
 * RootPane — O "tabuleiro" principal da aplicação.
 * Organiza o layout em BorderPane: Menu (Top), Jogo (Center), Info (Left) e Oficina (Right).
 */
public class RootPane extends BorderPane {

    private final DeepSeaManager model;

    // Componentes da Interface
    private StackPane     canvasStack;
    private InfoPanel     infoPanel;
    private WorkshopPanel workshopPanel;
    private SurfaceCanvas surfaceCanvas;
    private TrenchCanvas  trenchCanvas;

    private Menu menuOpenRecent;
    private final RecentFilesManager recentFiles = new RecentFilesManager(5);

    public RootPane(DeepSeaManager model) {
        this.model = model;
        createViews();
        registerHandlers();
        update();
    }

    /** Chamado pelo DeepSeeApp após show() para sincronizar o estado visual. */
    public void forceUpdate() {
        update();
    }

    // =========================================================
    // createViews
    // =========================================================
    private void createViews() {
        // Barra de Menus no topo
        setTop(buildMenuBar());

        // Centro: Camadas de desenho
        surfaceCanvas = new SurfaceCanvas(model);
        trenchCanvas  = new TrenchCanvas(model);

        // O StackPane permite colocar os canvas uns sobre os outros;
        // controlamos qual se vê usando o método setVisible() no update()
        canvasStack   = new StackPane(surfaceCanvas, trenchCanvas);
        canvasStack.setStyle("-fx-background-color: #061020;");
        StackPane.setAlignment(surfaceCanvas, Pos.CENTER);
        setCenter(canvasStack);

        // Laterais
        infoPanel     = new InfoPanel(model);
        workshopPanel = new WorkshopPanel(model);
        setLeft(infoPanel);
        setRight(workshopPanel);
    }

    // =========================================================
    // registerHandlers
    // =========================================================
    private void registerHandlers() {
        // Reage a mudanças de estado (ex: mudar de Superfície para Fosso)
        model.addPropertyChangeListener(DeepSeaManager.PROP_STATE_CHANGED,
                evt -> Platform.runLater(this::update));

        // Reage a mudanças de dados gerais
        model.addPropertyChangeListener(DeepSeaManager.PROP_DATA_CHANGED,
                evt -> Platform.runLater(this::update));

        // Pop-up de Vitória
        model.addPropertyChangeListener(DeepSeaManager.PROP_WIN,
                evt -> Platform.runLater(() ->
                        new Alert(Alert.AlertType.INFORMATION,
                                "Parabéns! Todos os artefactos foram recuperados!",
                                ButtonType.OK).showAndWait()));

        // Pop-up de Derrota
        model.addPropertyChangeListener(DeepSeaManager.PROP_GAME_OVER,
                evt -> Platform.runLater(() ->
                        new Alert(Alert.AlertType.ERROR,
                                "Game Over! A missão falhou.",
                                ButtonType.OK).showAndWait()));
    }

    // =========================================================
    // update
    // =========================================================
    private void update() {
        // Esconde todos os canvas primeiro
        canvasStack.getChildren().forEach(n -> n.setVisible(false));
        // Ativa apenas o canvas correspondente ao estado atual
        switch (model.getCurrentState()) {
            case SETUP_STATE, IDLE_STATE     -> surfaceCanvas.setVisible(true);
            case DESCEND_STATE, ASCEND_STATE -> trenchCanvas.setVisible(true);
            case WIN_STATE, GAME_OVER_STATE  -> surfaceCanvas.setVisible(true);
            default                          -> surfaceCanvas.setVisible(true);
        }
        // Atualiza os painéis laterais
        infoPanel.update();
        workshopPanel.update();
    }

    // =========================================================
    // MenuBar
    // =========================================================
    private MenuBar buildMenuBar() {
        MenuBar bar = new MenuBar();
        bar.getMenus().addAll(buildMenuGame(), buildMenuLog());
        return bar;
    }

    private Menu buildMenuGame() {
        Menu menu = new Menu("_Game");

        // Novo Jogo
        MenuItem miNew  = new MenuItem("New");
        miNew.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+N"));
        miNew.setOnAction(e -> actionNew());

        // Abrir Jogo Existente
        MenuItem miOpen = new MenuItem("Open…");
        miOpen.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+O"));
        miOpen.setOnAction(e -> actionOpen());

        // Submenu de ficheiros recentes
        menuOpenRecent = new Menu("Open Recent");
        refreshRecentMenu();

        // Gravar
        MenuItem miSave = new MenuItem("Save As…");
        miSave.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+S"));
        miSave.setOnAction(e -> actionSaveAs());

        MenuItem miExit = new MenuItem("Exit");
        miExit.setOnAction(e -> actionExit());

        menu.getItems().addAll(miNew, miOpen, menuOpenRecent, miSave,
                new SeparatorMenuItem(), miExit);
        return menu;
    }

    private Menu buildMenuLog() {
        Menu menu = new Menu("_Log");

        // Show/Hide
        MenuItem miShowHide = new MenuItem("Show/Hide");
        miShowHide.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Ctrl+L"));
        miShowHide.setOnAction(e -> System.out.println("toggle LogStage"));

        MenuItem miSaveLogs = new MenuItem("Save logs…");
        miSaveLogs.setOnAction(e -> actionSaveLogs());

        MenuItem miClear = new MenuItem("Clear logs");
        miClear.setOnAction(e -> model.clearLogs());

        menu.getItems().addAll(miShowHide, miSaveLogs, miClear);
        return menu;
    }

    // =========================================================
    // Acções do menu
    // =========================================================
    private void actionNew() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Iniciar novo jogo?\nO progresso actual será perdido.",
                ButtonType.YES, ButtonType.NO);
        c.setTitle("Novo jogo"); c.setHeaderText(null);
        c.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES) return;
            createViews();
            registerHandlers();
            update();
        });
    }

    // Lógica para carregar/gravar usando FileChooser e o RecentFilesManager
    private void actionOpen() {
        File f = fileChooser("Abrir jogo", "*.dat").showOpenDialog(getScene().getWindow());
        if (f == null) return;
        if (model.loadGame(f.getAbsolutePath())) {
            recentFiles.add(f.getAbsolutePath()); refreshRecentMenu();
        } else showError("Não foi possível carregar:\n" + f.getName());
    }

    // Atualiza dinamicamente o menu "Open Recent" com os ficheiros guardados no disco
    private void actionOpenRecent(String path) {
        if (model.loadGame(path)) {
            recentFiles.add(path); refreshRecentMenu();
        } else {
            recentFiles.remove(path); refreshRecentMenu();
            showError("Ficheiro não encontrado:\n" + path);
        }
    }

    // Abre uma caixa de diálogo para guardar o estado atual do jogo num ficheiro.
    private void actionSaveAs() {
        FileChooser fc = fileChooser("Gravar jogo", "*.dat");
        fc.setInitialFileName("deepsea_save.dat");
        File f = fc.showSaveDialog(getScene().getWindow());
        if (f == null) return;
        if (model.saveGame(f.getAbsolutePath())) {
            recentFiles.add(f.getAbsolutePath()); refreshRecentMenu();
        } else showError("Não foi possível gravar:\n" + f.getName());
    }

    // Exporta o histórico de eventos logs para um ficheiro de texto simples.
    private void actionSaveLogs() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Gravar logs");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto (*.txt)", "*.txt"));
        fc.setInitialFileName("deepsea_log.txt");
        File f = fc.showSaveDialog(getScene().getWindow());
        if (f != null) model.exportLogsToText(f.getAbsolutePath());
    }

    // Termina a aplicação após confirmação do utilizador.
    private void actionExit() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Sair da aplicação?", ButtonType.YES, ButtonType.NO);
        c.setTitle("Sair"); c.setHeaderText(null);
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) javafx.application.Platform.exit();
        });
    }

    // Reconstrói o submenu de ficheiros recentes com base na lista guardada no disco.
    private void refreshRecentMenu() {
        menuOpenRecent.getItems().clear();
        var list = recentFiles.getAll();
        // Caso a lista esteja vazia, mostra uma opção desativada
        if (list.isEmpty()) {
            MenuItem e = new MenuItem("(vazio)"); e.setDisable(true);
            menuOpenRecent.getItems().add(e); return;
        }
        // Cria um MenuItem para cada ficheiro na lista de recentes
        for (int i = 0; i < list.size(); i++) {
            String path = list.get(i);
            // Mostra apenas o nome do ficheiro em vez do caminho completo
            MenuItem mi = new MenuItem((i+1) + ".  " + new File(path).getName());
            mi.setOnAction(e -> actionOpenRecent(path));
            menuOpenRecent.getItems().add(mi);
        }
        // Adiciona opção para limpar o histórico no final do menu
        menuOpenRecent.getItems().add(new SeparatorMenuItem());
        MenuItem clear = new MenuItem("Limpar lista");
        clear.setOnAction(e -> { list.forEach(recentFiles::remove); refreshRecentMenu(); });
        menuOpenRecent.getItems().add(clear);
    }

    /**
     * Método utilitário para configurar rapidamente um FileChooser.
     */
    private FileChooser fileChooser(String title, String pattern) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Deep Sea Mining (" + pattern + ")", pattern));
        return fc;
    }

    // Método utilitário para mostrar mensagens de erro ao utilizador.
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}