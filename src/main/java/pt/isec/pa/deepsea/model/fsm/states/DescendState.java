package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;

import java.io.Serializable;

/**
 * DESCEND_STATE — Drone a descer pelo fosso marinho.
 * Substitui o antigo DroneDeploymentState.
 */
public class DescendState extends DeepSeaStateAdapter implements Serializable {

    public DescendState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
    }

    /**
     * Move o drone lateralmente no fosso.
     * Consome combustível (dobro em correntes). Colisões reduzem o casco.
     * Se combustível = 0 ou casco = 0 → trata perda do drone.
     */
    @Override
    public boolean moveDrone(int dx) {
        boolean moved = data.moveDroneLateral(dx);
        if (!moved) {
            context.addLog("Movimento lateral bloqueado (rocha).");
            return false;
        }
        // Verificar se o drone foi perdido após o movimento
        checkDroneLost();
        return true;
    }

    /**
     * Chamado pela UI quando o drone termina a descida e chega ao fundo.
     * DESCEND_STATE → EXPLORE_STATE.
     */
    @Override
    public boolean exploreSeabed() {
        context.addLog("Drone atingiu o fundo. A iniciar exploração.");
        changeState(DeepSeaState.EXPLORE_STATE);
        return true;
    }

    /**
     * Verifica se o drone ativo foi perdido (combustível ou casco).
     * fuelEmpty() / allDronesLost() → IDLE ou GAME_OVER.
     */
    private void checkDroneLost() {
        if (!data.isActiveDroneLost()) return;

        data.handleActiveDroneLost(); // minérios/artefactos voltam ao fundo
        context.addLog("Drone perdido durante a descida.");

        if (data.getAvailableDronesCount() > 0) {
            context.addLog("Restam " + data.getAvailableDronesCount() + " drone(s).");
            changeState(DeepSeaState.IDLE_STATE);
        } else {
            context.addLog("GAME OVER: todos os drones foram perdidos.");
            changeState(DeepSeaState.GAME_OVER_STATE);
        }
    }

    @Override
    public DeepSeaState getState() { return DeepSeaState.DESCEND_STATE; }
}