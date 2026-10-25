package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;

/**
 * GAME_OVER_STATE — Jogo terminado.
 *
 * Atingido quando:
 *  – fuelEmpty(): combustível do navio = 0
 *  – allDronesLost(): todos os 3 drones foram perdidos/destruídos
 *
 * Nenhuma ação é permitida — todos os métodos herdam false do Adapter.
 */
public class GameOverState extends DeepSeaStateAdapter {

    public GameOverState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
        data.setGameTerminated(true);
    }

    // Não fazemos override de nada.
    // Por herança do Adapter, todos os métodos retornam 'false'.

    @Override
    public DeepSeaState getState() { return DeepSeaState.GAME_OVER_STATE; }
}