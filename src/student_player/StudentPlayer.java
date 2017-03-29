package student_player;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;
import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class StudentPlayer extends BohnenspielPlayer {

    private boolean isFirstMove = true;


    public StudentPlayer() { super("260654858"); }

    public BohnenspielMove chooseMove(BohnenspielBoardState board_state)
    {
        long endtime;
        long time = new Date().getTime();

//        first move... take advantage of it
        if(isFirstMove){
            isFirstMove = false;
            endtime = time + 15000;
        }
        else{
            endtime = time + 200;
        }

        Decision decision= new Decision();

        int maxDepth = 1;
        while(new Date().getTime() < endtime){
            decision = minMax(board_state, Integer.MIN_VALUE, Integer.MAX_VALUE, maxDepth, true);
            maxDepth++;
        }


        // But since this is a placeholder algorithm, we won't act on that information.
        return decision.returnMove;
    }

    private Decision minMax(BohnenspielBoardState boardState, double alpha, double beta, int maxDepth, boolean isMax) {

        double value;

        //we have reached our max depth so we need to return
        if (maxDepth == 0) {
            value = MyTools.evaluateBoard(boardState, this);
            return new Decision(value);
        }

        ArrayList<BohnenspielMove> moves = boardState.getLegalMoves();

        //we have reached our max depth so we need to return
        if (moves.size() == 0) {
            value = MyTools.evaluateBoard(boardState, this);
            return new Decision(value);
        }

        //Need to iterate through all legal moves (with some trimming of course)
        Iterator<BohnenspielMove> movesIterator = moves.iterator();

        Decision returnMove;
        Decision bestMove = null;

        //if you are trying to get the maximum value...
        if (isMax) {
            while (movesIterator.hasNext()) {

                //grab the current move for analysis
                BohnenspielMove currentMove = movesIterator.next();

                //inspect the outcome of each legal move
                BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) boardState.clone();

                //inspect the outcome of the move
                if(!cloned_board_state.move(currentMove)) continue;

                //recursive call to continue DFS from opponent's perspective
                returnMove = minMax(cloned_board_state, alpha, beta, maxDepth - 1, false);

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

                //inspect the outcome of each legal move
                BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) boardState.clone();

                //inspect the outcome of the move
                cloned_board_state.move(currentMove);
                returnMove = minMax(cloned_board_state, alpha, beta, maxDepth - 1, true);

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




