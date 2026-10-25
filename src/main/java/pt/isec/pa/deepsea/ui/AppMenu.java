package pt.isec.pa.deepsea.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;

import java.io.File;

public class AppMenu extends MenuBar {

    public AppMenu(DeepSeaManager manager, LogWindow logWindow) {
        // Menu Jogo
        Menu menuGame = new Menu("Jogo");
        MenuItem miSave = new MenuItem("Guardar Jogo");
        MenuItem miLoad = new MenuItem("Carregar Jogo");
        MenuItem miExit = new MenuItem("Sair");

        miExit.setOnAction(e -> System.exit(0));
        menuGame.getItems().addAll(miSave, miLoad, miExit);

        // Menu Janelas / Logs
        Menu menuLogs = new Menu("Logs");
        MenuItem miToggleLog = new MenuItem("Mostrar/Esconder Janela de Logs");
        MenuItem miSaveLogs = new MenuItem("Exportar Logs para Texto");
        MenuItem miClearLogs = new MenuItem("Limpar Histórico");

        miToggleLog.setOnAction(e -> {
            if (logWindow.isShowing()) {
                logWindow.hide();
            } else {
                logWindow.show();
            }
        });

        miSaveLogs.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exportar Logs");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ficheiros de Texto (*.txt)", "*.txt"));
            File file = fileChooser.showSaveDialog(this.getScene().getWindow());
            if (file != null) {
                manager.exportLogsToText();
            }
        });

        miClearLogs.setOnAction(e -> {
            manager.clearLogs();
        });

        menuLogs.getItems().addAll(miToggleLog, miSaveLogs, miClearLogs);
        this.getMenus().addAll(menuGame, menuLogs);
    }
}