package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;

import java.io.Serializable;

/**
 * IDLE_STATE — Navio à superfície.
 * Substitui o antigo ShipMovementState.
 * Serializable necessário para que o save/load funcione.
 */
public class IdleState extends DeepSeaStateAdapter implements Serializable {

    public IdleState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
    }

    @Override
    public boolean moveShip(int dx, int dy) {
        boolean moved = data.moveShip(dx, dy);
        if (moved) {
            context.addLog("Navio movido para ("
                    + data.getShip().getPosX() + "," + data.getShip().getPosY() + ").");
        }
        // fuelEmpty → GAME_OVER
        if (data.getShip().getFuel() <= 0) {
            context.addLog("GAME OVER: combustível do navio esgotado.");
            changeState(DeepSeaState.GAME_OVER_STATE);
        }
        return moved;
    }

    @Override
    public boolean refuelDrone(int droneId, int volume) {
        boolean ok = data.refuelDrone(droneId, volume);
        if (ok) context.addLog("Drone " + droneId + " abastecido: +" + volume + " combustível.");
        return ok;
    }

    @Override
    public boolean repairDrone(int droneId) {
        boolean ok = data.repairDrone(droneId);
        if (ok) {
            context.addLog("Drone " + droneId + " reparado.");
            // reparação pode esgotar o combustível do navio
            if (data.getShip().getFuel() <= 0) {
                context.addLog("GAME OVER: combustível esgotado após reparação.");
                changeState(DeepSeaState.GAME_OVER_STATE);
            }
        }
        return ok;
    }

    @Override
    public boolean upgradeDrone(int droneId, String type) {
        boolean ok = data.upgradeDrone(droneId, type);
        if (ok) context.addLog("Drone " + droneId + " melhorado: " + type + ".");
        return ok;
    }

    @Override
    public boolean descendDrone(int droneId) {
        boolean ok = data.launchDrone(droneId);
        if (ok) {
            context.addLog("Drone " + droneId + " lançado para a descida.");
            changeState(DeepSeaState.DESCEND_STATE);
        }
        return ok;
    }

    @Override
    public DeepSeaState getState() { return DeepSeaState.IDLE_STATE; }
}