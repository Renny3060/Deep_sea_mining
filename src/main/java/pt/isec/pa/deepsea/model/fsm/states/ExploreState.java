package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;

/**
 * EXPLORE_STATE — Drone a explorar o fundo do mar.
 */
public class ExploreState extends DeepSeaStateAdapter {

    public ExploreState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
    }

    /**
     * Move o drone para uma célula adjacente no fundo do mar.
     * A célula é revelada (sai da escuridão).
     * Pode encontrar: vazio, minério, monstro ou artefacto.
     * Consome combustível; se acabar → fuelEmpty() → IDLE ou GAME_OVER.
     */
    @Override
    public boolean moveDrone(int dx, int dy) {
        DeepSeaMining.MoveResult result = data.moveDroneSeabed(dx, dy);

        switch (result) {
            case MOVED_EMPTY:
                context.addLog("Drone moveu-se. Célula vazia.");
                break;
            case MOVED_MINERAL:
                context.addLog("Drone encontrou minério. Use collectMineral() para recolher.");
                break;
            case MOVED_MONSTER:
                context.addLog("Monstro! Integridade do casco reduzida.");
                checkDroneLost();
                break;
            case MOVED_ARTIFACT:
                context.addLog("Artefacto detetado! Use collectArtifact() para tentar recolher.");
                break;
            case FUEL_EMPTY:
                context.addLog("Drone sem combustível no fundo do mar.");
                checkDroneLost();
                return false;
            case BLOCKED:
                return false;
        }
        return result != DeepSeaMining.MoveResult.FUEL_EMPTY
                && result != DeepSeaMining.MoveResult.BLOCKED;
    }

    /**
     * Recolhe o minério da célula atual.
     * Ação SEPARADA de collectArtifact — consome ≈1% de combustível extra.
     */
    @Override
    public boolean collectMineral() {
        boolean ok = data.collectMineral();
        if (ok) context.addLog("Minério recolhido.");
        return ok;
    }

    /**
     * Tenta recolher o artefacto da célula atual → inicia o puzzle.
     * EXPLORE_STATE → PUZZLE_STATE.
     */
    @Override
    public boolean collectArtifact() {
        boolean ok = data.initMinigame(); // prepara o puzzle no modelo
        if (ok) {
            context.addLog("Puzzle iniciado para recolher o artefacto.");
            changeState(DeepSeaState.PUZZLE_STATE);
        } else {
            context.addLog("Sem artefacto nesta célula.");
        }
        return ok;
    }

    /**
     * Ordena ao drone que inicie a subida de volta ao navio.
     * EXPLORE_STATE → ASCEND_STATE.
     */
    @Override
    public boolean ascendDrone(int droneId) {
        boolean ok = data.startAscent(droneId);
        if (ok) {
            context.addLog("Drone " + droneId + " a subir.");
            changeState(DeepSeaState.ASCEND_STATE);
        }
        return ok;
    }

    /**
     * Verifica se o drone ativo foi perdido (combustível ou casco a zero).
     * fuelEmpty / allDronesLost → IDLE ou GAME_OVER.
     */
    private void checkDroneLost() {
        if (!data.isActiveDroneLost()) return;

        data.handleActiveDroneLost(); // itens voltam ao fundo; grelha → escuridão
        context.addLog("Drone perdido no fundo do mar.");

        if (data.getAvailableDronesCount() > 0) {
            context.addLog("Restam " + data.getAvailableDronesCount() + " drone(s).");
            changeState(DeepSeaState.IDLE_STATE);
        } else {
            context.addLog("GAME OVER: todos os drones foram perdidos.");
            changeState(DeepSeaState.GAME_OVER_STATE);
        }
    }

    @Override
    public DeepSeaState getState() { return DeepSeaState.EXPLORE_STATE; }
}