package pt.isec.pa.deepsea.ui.util;

import javafx.scene.Parent;

/**
 * CSSManager — Classe utilitária responsável por centralizar a aplicação
 * de folhas de estilo (CSS) aos componentes da interface JavaFX.
 */
public class CSSManager {

    // Construtor privado para impedir a instanciação da classe (Padrão Utility Class).
    // Como todos os métodos são estáticos, não faz sentido criar objetos desta classe.
    private CSSManager() { }

    public static void applyCSS(Parent parent, String filename) {
        var url = CSSManager.class.getResource("/css/" + filename);

        // Verifica se o ficheiro foi realmente encontrado para evitar erros de ponteiro nulo (NullPointerException).
        if (url == null) {
            System.err.println("[CSSManager] CSS não encontrado: /css/" + filename);
            return;
        }

        // Adiciona a folha de estilo à lista de stylesheets do componente.
        // toExternalForm() converte o URL do recurso numa String formatada que o JavaFX compreende.
        parent.getStylesheets().add(url.toExternalForm());
    }
}