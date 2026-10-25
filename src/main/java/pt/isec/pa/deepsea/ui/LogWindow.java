package pt.isec.pa.deepsea.ui;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;

import java.util.List;

public class LogWindow extends Stage {
    private final TextArea textArea;

    public LogWindow(DeepSeaManager manager) {
        this.setTitle("Histórico de Logs do Sistema");
        this.setWidth(500);
        this.setHeight(400);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Courier New';");

        BorderPane root = new BorderPane(textArea);
        this.setScene(new Scene(root));

        // Subscrever à alteração de logs do manager
        manager.addPropertyChangeListener("logAdded", evt -> updateLogs(manager.getLogs()));

        // Carregamento inicial
        updateLogs(manager.getLogs());
    }

    private void updateLogs(List<String> logs) {
        StringBuilder sb = new StringBuilder();
        for (String log : logs) {
            sb.append(log).append("\n");
        }
        textArea.setText(sb.toString());
        textArea.setScrollTop(Double.MAX_VALUE); // Faz scroll automático para o fundo
    }
}