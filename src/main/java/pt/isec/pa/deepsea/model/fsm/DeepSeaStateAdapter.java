package pt.isec.pa.deepsea.model.fsm;

import pt.isec.pa.deepsea.model.DeepSeaMining;

import java.io.Serializable;

/**
 * Classe abstrata base de todos os estados concretos da FSM.
 *
 * Implementa IDeepSeaState com comportamento por omissão:
 *   – Cada método devolve false → ação não permitida neste estado.
 *   – Os estados concretos fazem @Override APENAS do que lhes pertence.
 *     Ex: moveDrone(dx,dy) só tem override em ExploreState;
 *         moveDrone(dx) só tem override em DescendState e AscendState.
 */
public abstract class DeepSeaStateAdapter implements IDeepSeaState, Serializable {

    // Contexto da FSM — para mudar de estado e aceder ao log. */
    protected DeepSeaContext context;

    // Modelo de dados — para executar a lógica do jogo (combustível, drones, etc.). */
    protected DeepSeaMining data;

    //Construtor base. Chamado por todos os estados concretos via super().
    protected DeepSeaStateAdapter(DeepSeaContext context, DeepSeaMining data) {
        this.context = context;
        this.data = data;
    }

    //Método para facilitar a troca de estado
    //Ele vai ao Enum e cria o novo objeto de estado
    protected void changeState(DeepSeaState newState) {
        context.changeState(newState.createState(context, data));
    }

    // Cada estado será obrigado a dizer quem é por ex: return DeepSeaState.MINING
    @Override
    public abstract DeepSeaState getState();

    // ── Implementações por omissão ─────────────────────────────────
    // Devolvem false: ação bloqueada neste estado.

    // IDLE_STATE
    @Override public boolean moveShip(int dx, int dy)             { return false; }
    @Override public boolean refuelDrone(int droneId, int volume)  { return false; }
    @Override public boolean repairDrone(int droneId)              { return false; }
    @Override public boolean upgradeDrone(int droneId, String type){ return false; }
    @Override public boolean descendDrone(int droneId)             { return false; }

    // DESCEND_STATE / ASCEND_STATE
    @Override public boolean moveDrone(int dx)                     { return false; }
    @Override public boolean exploreSeabed()                       { return false; }

    // EXPLORE_STATE
    @Override public boolean moveDrone(int dx, int dy)             { return false; }
    @Override public boolean collectMineral()                      { return false; }
    @Override public boolean collectArtifact()                     { return false; }
    @Override public boolean ascendDrone(int droneId)              { return false; }

    // PUZZLE_STATE
    @Override public boolean movePiece(int piecePosition)          { return false; }

    // ASCEND_STATE
    @Override public boolean returnToShip()                        { return false; }
}