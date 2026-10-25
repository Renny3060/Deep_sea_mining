package pt.isec.pa.deepsea.ui;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RecentFilesManager — Responsável por persistir a lista dos últimos ficheiros de gravação abertos.
 * Os dados são guardados numa pasta oculta na diretoria do utilizador para persistirem mesmo
 * após fechar a aplicação.
 */
public class RecentFilesManager {

    // Define o caminho da pasta de configuração
    private static final String CONFIG_DIR  = System.getProperty("user.home") + "/.deepsea";
    // Define o caminho do ficheiro de texto que contém a lista de caminhos
    private static final String CONFIG_FILE = CONFIG_DIR + "/recent.txt";

    private final int maxEntries; // Número máximo de ficheiros a lembrar
    private final List<String> entries = new ArrayList<>();

    public RecentFilesManager(int maxEntries) {
        this.maxEntries = maxEntries;
        load(); // Carrega os dados guardados assim que o gestor é criado
    }

    /** * Adiciona um novo caminho à lista.
     * Implementa a lógica de "Mais Recente": se o ficheiro já existia na lista,
     * ele é movido para o topo.
     */
    public void add(String path) {
        entries.remove(path); // Remove se já existir para evitar duplicados
        entries.add(0, path); // Adiciona na primeira posição

        // Garante que a lista não cresce além do limite definido
        while (entries.size() > maxEntries) {
            entries.remove(entries.size() - 1);
        }
        save();
    }

    /** * Remove um caminho da lista.*/
    public void remove(String path) {
        if (entries.remove(path)) {
            save();
        }
    }

    /** * Retorna uma lista que não pode ser modificada externamente.
     * O primeiro elemento é sempre o ficheiro aberto mais recentemente.
     */
    public List<String> getAll() {
        return Collections.unmodifiableList(entries);
    }

    // ── Persistência (Leitura e Escrita em Disco) ──────────────────

    /**
     * Lê os caminhos do ficheiro de texto e coloca-os na memória.
     */
    private void load() {
        entries.clear();
        try {
            File f = new File(CONFIG_FILE);
            if (!f.exists()) return; // Se não houver ficheiro, não há nada a carregar

            // Lê todas as linhas do ficheiro
            List<String> lines = Files.readAllLines(f.toPath());
            for (String line : lines) {
                line = line.trim();
                // Apenas adiciona se a linha não for vazia e se o ficheiro físico ainda existir
                if (!line.isEmpty() && new File(line).exists()) {
                    entries.add(line);
                }
                // Respeita o limite máximo definido no construtor
                if (entries.size() >= maxEntries) break;
            }
        } catch (IOException e) {
            System.err.println("[RecentFiles] Erro ao carregar: " + e.getMessage());
        }
    }

    /**
     * Guarda a lista atual de memória para o ficheiro de texto.
     */
    private void save() {
        try {
            // Cria a diretoria .deepsea caso ela ainda não exista
            Files.createDirectories(Paths.get(CONFIG_DIR));

            // Grava cada caminho numa linha nova
            try (PrintWriter pw = new PrintWriter(new FileWriter(CONFIG_FILE))) {
                entries.forEach(pw::println);
            }
        } catch (IOException e) {
            System.err.println("[RecentFiles] Erro ao gravar: " + e.getMessage());
        }
    }
}