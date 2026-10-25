package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;

import java.io.Serializable;

/**
 * SETUP_STATE — Estado inicial da FSM (configuração do jogo).
 *
 * NÃO transita no construtor — isso causava o problema:
 * o evento disparava antes de a UI ter listeners registados.
 *
 * Em vez disso, expõe startGame() para a UI chamar.
 * Como alternativa temporária de teste, a UI pode chamar
 * manager.startGame() após estar pronta.
 */
public class SetupState extends DeepSeaStateAdapter implements Serializable {

    public SetupState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
    }

    /**
     * Inicia o jogo — chamado pela UI após estar inicializada.
     * Usa moveShip como canal de comunicação reutiliza interface existente.
     */
    @Override
    public boolean moveShip(int dx, int dy) {
        // Em SETUP_STATE, qualquer tecla de seta inicia o jogo
        changeState(DeepSeaState.IDLE_STATE);
        // e executa logo o movimento
        return context.moveShip(dx, dy);
    }

    @Override
    public DeepSeaState getState() { return DeepSeaState.SETUP_STATE; }
}