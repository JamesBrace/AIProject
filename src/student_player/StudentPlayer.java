package student_player;

import java.util.ArrayList;
import java.util.Iterator;

import boardgame.Board;
import boardgame.Player;
import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;
import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class StudentPlayer extends BohnenspielPlayer {

    public StudentPlayer() { super("260654858"); }

    public BohnenspielMove chooseMove(BohnenspielBoardState board_state)
    {
        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits = board_state.getPits();

        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        int[] my_pits = pits[player_id];
        int[] op_pits = pits[opponent_id];

        // Use code stored in ``mytools`` package.
//        MyTools.getSomething();

        //inspect the outcome of each legal move
        BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) board_state.clone();
        Decision decision = minMax(cloned_board_state, 0, 0, 6, this);

        // But since this is a placeholder algorithm, we won't act on that information.
        return decision.returnMove;
    }

    protected Decision minMax(BohnenspielBoardState boardState, double alpha, double beta, int maxDepth, Player player) {
        //TODO: Need to implement this based on timing and memory requirements
//        if (!canContinue()) {
//            return new Decision();
//        }

        ArrayList<BohnenspielMove> moves = boardState.getLegalMoves();

        //Need to iterate through all legal moves (with some trimming of course)
        Iterator<BohnenspielMove> movesIterator = moves.iterator();

        double value = 0;

        //figure out which perspective you should be playing from (ie. you or opponent)
        boolean isMax = (player.equals(this));

        //we have reached the bottom of the tree or the game has already been won??
        if (maxDepth == 0 || boardState.gameOver()) {

            value = MyTools.evaluateBoard(boardState, this);
            return new Decision(value);
        }

        Decision returnMove;
        Decision bestMove = null;

        //if you are trying to get the maximum value...
        if (isMax) {
            while (movesIterator.hasNext()) {

                //grab the current move for analysis
                BohnenspielMove currentMove = movesIterator.next();

                //inspect the outcome of the move
                boardState.move(currentMove);

                //recursive call to continue DFS from opponent's perspective
                returnMove = minMax(boardState, alpha, beta, maxDepth - 1, null);

                //TODO: Need to implement this
//                cloned_board_state.undoLastMove();

                if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue > alpha) {
                    alpha = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    bestMove.returnValue = beta;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        } else {
            while (movesIterator.hasNext()) {

                BohnenspielMove currentMove = movesIterator.next();

                //inspect the outcome of the move
                boardState.move(currentMove);
                returnMove = minMax(boardState, alpha, beta, maxDepth - 1, this);

                //TODO: Need to implement this
//                boardState.undoLastMove();

                if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue < beta) {
                    beta = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
                    bestMove.returnValue = alpha;
                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        }
    }
}




