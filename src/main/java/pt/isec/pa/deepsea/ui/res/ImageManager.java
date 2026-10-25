package pt.isec.pa.deepsea.ui.res;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * Padrão Multiton para carregamento e cache de imagens.
 * Carrega cada imagem uma única vez a partir de resources/images/.
 */
public class ImageManager {
    private static final Map<String, Image> cache = new HashMap<>();
    private static final String BASE_PATH = "/images/";

    private ImageManager() {}

    /**
     * Devolve a imagem com o nome indicado, carregando-a da primeira vez.
     * @param name nome do ficheiro, ex: "drone.png"
     * @return Image carregada, ou null se não encontrada
     */
    public static Image getImage(String name) {
        return cache.computeIfAbsent(name, key -> {
            var url = ImageManager.class.getResource(BASE_PATH + key);
            if (url == null) {
                System.err.println("ImageManager: imagem não encontrada — " + key);
                return null;
            }
            return new Image(url.toExternalForm());
        });
    }
}