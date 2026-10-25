package pt.isec.pa.deepsea.model.fsm.states;

import pt.isec.pa.deepsea.model.DeepSeaMining;
import pt.isec.pa.deepsea.model.fsm.DeepSeaContext;
import pt.isec.pa.deepsea.model.fsm.DeepSeaState;
import pt.isec.pa.deepsea.model.fsm.DeepSeaStateAdapter;

/**
 * PUZZLE_STATE — Minijogo de Puzzle 15 peças para recolha de artefactos.
 */
public class PuzzleState extends DeepSeaStateAdapter {

    public PuzzleState(DeepSeaContext context, DeepSeaMining data) {
        super(context, data);
    }

    /**
     * Desloca a peça na posição indicada para o espaço vazio adjacente.
     *
     * Após o movimento verifica INTERNAMENTE:
     *  – Puzzle resolvido → artefacto recolhido → EXPLORE_STATE (ou WIN_STATE)
     *  – Movimentos esgotados → ASCEND_STATE (drone sobe sem artefacto)
     *  – Movimento inválido (peça não adjacente ao vazio) → devolve false
     *
     * @param piecePosition índice (0-14) da peça a mover no array do puzzle
     */
    @Override
    public boolean movePiece(int piecePosition) {
        DeepSeaMining.PuzzleResult result = data.movePuzzlePiece(piecePosition);

        switch (result) {
            case MOVED:
                // Movimento válido; puzzle ainda por resolver e há movimentos restantes.
                context.addLog("Peça movida. Movimentos restantes: "
                        + data.getMiniGame().getRemainingMoves());
                return true;

            case SOLVED:
                // Puzzle resolvido! — verificação interna, não é uma transição exposta.
                context.addLog("Puzzle resolvido! Artefacto recolhido.");
                if (data.allArtifactsCollected()) {
                    context.addLog("Todos os artefactos recolhidos. VITÓRIA!");
                    changeState(DeepSeaState.WIN_STATE);
                } else {
                    changeState(DeepSeaState.EXPLORE_STATE);
                }
                return true;

            case MOVES_EXHAUSTED:
                // Movimentos esgotados → drone sobe imediatamente sem o artefacto.
                context.addLog("Movimentos esgotados! Drone a subir sem artefacto.");
                data.startAscent(data.getActiveDroneId());
                changeState(DeepSeaState.ASCEND_STATE);
                return false;

            case INVALID_MOVE:
                // Peça não é adjacente ao espaço vazio.
                context.addLog("Movimento inválido: peça não pode mover para aí.");
                return false;

            default:
                return false;
        }
    }

    @Override
    public DeepSeaState getState() { return DeepSeaState.PUZZLE_STATE; }
}