package pt.isec.pa.deepsea.model.fsm;

import pt.isec.pa.deepsea.model.data.Constants;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DeepSeaLog {
    private static DeepSeaLog instance = null;
    private List<String> logEntries;

    private DeepSeaLog() {
        logEntries = new ArrayList<>();
    } // construtor privado


    public static DeepSeaLog getInstance() {
        if (instance == null) {
            instance = new DeepSeaLog();
        }
        return instance;
    }

    public void addLog(String msg) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logEntries.add(timestamp + " - " + msg);
    }


    public List<String> getLogs() {
        return List.copyOf(logEntries);
    } // copia imutável

    public void clearLogs() {
        logEntries.clear();
    }


    public boolean saveToTextFile() {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Constants.LOG_FILE)))) {
            for (String entry : logEntries) {
                out.println(entry);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
