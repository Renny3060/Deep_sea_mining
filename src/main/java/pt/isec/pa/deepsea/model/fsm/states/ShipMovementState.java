package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;
import pt.isec.pa.deepsea.model.fsm.DeepSeaLog; // Importar o Logger

public class ShipMovementState extends DeepSeaStateAdapter {

    public ShipMovementState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
    }

    @Override
    public boolean moveShip(int dx, int dy) {
        boolean moved = data.moveShip(dx, dy);

        // Se o combustível acabar, o jogo termina
        if (data.isGameTerminated()) {
            changeState(DeepSeaState.GAME_OVER_STATE);
            DeepSeaLog.getInstance().addLog("GAME OVER: O navio ficou sem combustível!");
        }
        return moved;
    }

    @Override
    public boolean descendDrone(int droneId) {
        DeepSeaLog.getInstance().addLog("Drone " + droneId + " lançado.");
        changeState(DeepSeaState.DESCEND_STATE);
        return true;
    }

    @Override
    public DeepSeaState getState() {
        return DeepSeaState.IDLE_STATE;
    }
}