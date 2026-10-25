package pt.isec.pa.deepsea.ui.util;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;

/**
 * ImageManager — Gerencia o carregamento e cache de imagens.
 * Implementa o padrão Multiton para garantir que imagens repetidas não ocupem memória extra.
 */
public class ImageManager {

    private ImageManager() { }

    // Cache estático: armazena as imagens já carregadas.
    // A chave (String) é o nome do ficheiro e o valor é o objeto Image do JavaFX.
    private static final HashMap<String, Image> images = new HashMap<>();

    /**
     * Obtém uma imagem do cache ou carrega-a se for a primeira vez que é solicitada.
     */
    public static Image getImage(String filename) {
        // Tenta recuperar a imagem do mapa.
        Image image = images.get(filename);

        // Se não estiver no cache, tenta carregar do sistema de ficheiros.
        if (image == null) {
            // Usa try-with-resources para garantir que o InputStream é fechado automaticamente.
            try (InputStream is = ImageManager.class.getResourceAsStream("/images/" + filename)) {

                // Se o ficheiro não existir na pasta de recursos, retorna null.
                if (is == null) {
                    System.err.println("[ImageManager] Ficheiro não encontrado: /images/" + filename);
                    return null;
                }

                // Cria a imagem JavaFX a partir do stream de dados.
                image = new Image(is);

                // Guarda no cache para utilizações futuras.
                images.put(filename, image);

            } catch (Exception e) {
                // Captura erros de leitura ou formato de ficheiro inválido.
                return null;
            }
        }
        return image;
    }

    /**
     * Remove uma imagem específica do cache para libertar memória.
     * @param filename Nome do ficheiro a remover.
     */
    public static void purgeImage(String filename) {
        images.remove(filename);
    }
}