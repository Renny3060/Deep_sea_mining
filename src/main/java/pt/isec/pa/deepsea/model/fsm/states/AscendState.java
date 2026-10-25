package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;

import java.io.Serializable;

/**
 * ASCEND_STATE — Drone a subir pelo fosso de volta ao navio.
 * Substitui o antigo DroneAscendingState.
 */
public class AscendState extends DeepSeaStateAdapter implements Serializable {

    public AscendState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
    }

    /**
     * Move o drone lateralmente no fosso durante a subida.
     * Simétrico ao DESCEND_STATE: consome combustível, colisões reduzem casco.
     */
    @Override
    public boolean moveDrone(int dx) {
        boolean moved = data.moveDroneLateral(dx);
        if (!moved) {
            context.addLog("Movimento lateral bloqueado (rocha).");
            return false;
        }
        checkDroneLost();
        return true;
    }

    /**
     * O drone chegou ao navio após a subida.
     * Carga (artefactos + minérios) é adicionada ao portfólio do jogador.
     * ASCEND_STATE → IDLE_STATE  (ou WIN_STATE se todos os artefactos recolhidos).
     */
    @Override
    public boolean returnToShip() {
        data.collectDroneCargo(); // transfere carga para o portfólio
        context.addLog("Drone regressou ao navio. Carga recolhida.");

        if (data.allArtifactsCollected()) {
            context.addLog("Todos os artefactos recolhidos. VITÓRIA!");
            changeState(DeepSeaState.WIN_STATE);
        } else {
            changeState(DeepSeaState.IDLE_STATE);
        }
        return true;
    }

    /**
     * Verifica se o drone foi perdido durante a subida.
     * Se sim: itens voltam ao fundo, grelha → escuridão.
     * fuelEmpty / allDronesLost → IDLE ou GAME_OVER.
     */
    private void checkDroneLost() {
        if (!data.isActiveDroneLost()) return;

        data.handleActiveDroneLost();
        context.addLog("Drone perdido durante a subida. Carga devolvida ao fundo.");

        if (data.getAvailableDronesCount() > 0) {
            context.addLog("Restam " + data.getAvailableDronesCount() + " drone(s).");
            changeState(DeepSeaState.IDLE_STATE);
        } else {
            context.addLog("GAME OVER: todos os drones foram perdidos.");
            changeState(DeepSeaState.GAME_OVER_STATE);
        }
    }

    @Override
    public DeepSeaState getState() { return DeepSeaState.ASCEND_STATE; }
}