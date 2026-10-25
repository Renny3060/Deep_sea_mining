package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;

/**
 * WIN_STATE — Jogo terminado com vitória.
 *
 * Atingido quando todos os n1 artefactos foram recolhidos e o drone
 * regressou ao navio com sucesso (returnToShip no ASCEND_STATE,
 * ou puzzleSolved internamente no PUZZLE_STATE).
 *
 * Nenhuma ação é permitida — todos os métodos herdam false do Adapter.
 */
public class WinState extends DeepSeaStateAdapter {

    public WinState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
        data.setGameTerminated(true);
    }

    @Override
    public DeepSeaState getState() { return DeepSeaState.WIN_STATE; }
}