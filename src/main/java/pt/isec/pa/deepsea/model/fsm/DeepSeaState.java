package pt.isec.pa.deepsea.model.fsm;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.states.*;

/**
 * Enumeração de todos os estados da FSM + Factory Method de criação.
 */
public enum DeepSeaState {

    SETUP_STATE,
    IDLE_STATE,
    DESCEND_STATE,
    EXPLORE_STATE,
    PUZZLE_STATE,
    ASCEND_STATE,
    WIN_STATE,
    GAME_OVER_STATE;

    /**
     * Factory Method — cria a instância concreta do estado.
     * Chamado pelo DeepSeaStateAdapter.changeState() e pelo DeepSeaContext.
     */
    public IDeepSeaState createState(DeepSeaContext context, DeepSeaMining data) {
        return switch (this) {
            case SETUP_STATE     -> new SetupState(context, data);
            case IDLE_STATE      -> new IdleState(context, data);
            case DESCEND_STATE   -> new DescendState(context, data);
            case EXPLORE_STATE   -> new ExploreState(context, data);
            case PUZZLE_STATE    -> new PuzzleState(context, data);
            case ASCEND_STATE    -> new AscendState(context, data);
            case WIN_STATE       -> new WinState(context, data);
            case GAME_OVER_STATE -> new GameOverState(context, data);
        };
    }
}