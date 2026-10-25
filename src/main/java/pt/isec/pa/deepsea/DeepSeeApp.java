package pt.isec.pa.deepsea;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import pt.isec.pa.deepsea.model.fsm.DeepSeaManager;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.ui.RootPane;
import pt.isec.pa.deepsea.ui.util.CSSManager;

public class DeepSeeApp extends Application {

    private DeepSeaManager model;
    private RootPane root;

    @Override
    public void init() throws Exception {
        super.init();
        model = new DeepSeaManager();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new RootPane(model);
        CSSManager.applyCSS(root, "style.css");

        Scene scene = new Scene(root, 1300, 780);
        primaryStage.setTitle("Deep Sea Mining");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // ── Teclas registadas com addEventFilter na Scene ─────────
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            DeepSeaState st = model.getCurrentState();

            switch (st) {
                // Superfície: setas movem o navio
                case SETUP_STATE, IDLE_STATE -> {
                    boolean handled = true;
                    switch (e.getCode()) {
                        case UP    -> model.moveShip(-1,  0);
                        case DOWN  -> model.moveShip( 1,  0);
                        case LEFT  -> model.moveShip( 0, -1);
                        case RIGHT -> model.moveShip( 0,  1);
                        default    -> handled = false;
                    }
                    if (handled) e.consume();
                }

                // Fosso (descida): ←/→ lateral, ↓ ou Enter avança para o fundo
                case DESCEND_STATE -> {
                    boolean handled = true;
                    switch (e.getCode()) {
                        case LEFT        -> model.moveDroneLateral(-1);
                        case RIGHT       -> model.moveDroneLateral( 1);
                        case DOWN, ENTER, SPACE -> model.exploreSeabed();
                        default          -> handled = false;
                    }
                    if (handled) e.consume();
                }

                // Fosso (subida): ←/→ lateral, ↑ ou Enter regressa ao navio
                case ASCEND_STATE -> {
                    boolean handled = true;
                    switch (e.getCode()) {
                        case LEFT       -> model.moveDroneLateral(-1);
                        case RIGHT      -> model.moveDroneLateral( 1);
                        case UP, ENTER, SPACE -> model.returnToShip();
                        default         -> handled = false;
                    }
                    if (handled) e.consume();
                }

                default -> {}
            }
        });

        Platform.runLater(root::forceUpdate);
    }

    public static void main(String[] args) {
        launch(args);
    }
}