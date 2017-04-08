package student_player;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.*;

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

        long endTime = (isFirstMove)? System.nanoTime() + 29_800_000_000L : System.nanoTime() + 600_000_000L;

        if(isFirstMove && player_id == 0){
            isFirstMove = false;
            ArrayList<BohnenspielMove> moves = board_state.getLegalMoves();
            return new BohnenspielMove(2);
        }

        final ExecutorService service = Executors.newSingleThreadExecutor();

        Decision currentBest = new Decision(Integer.MIN_VALUE);

        Callable<BohnenspielMove> task = () -> {

            int maxDepth = 10;
            Decision decision= new Decision();

            while(maxDepth < 25){
                decision = minMax(board_state, Integer.MIN_VALUE, Integer.MAX_VALUE, maxDepth, true);

                currentBest.returnValue = decision.returnValue;
                currentBest.returnMove = decision.returnMove;

                maxDepth++;

                System.out.println(maxDepth);
            }


            return decision.returnMove;
        };

        try {
            final Future<BohnenspielMove> f = service.submit(task);
           return f.get(endTime - System.nanoTime(), TimeUnit.NANOSECONDS);
        } catch (final TimeoutException e) {
            return currentBest.returnMove;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            service.shutdown();
        }
    }

    public Decision minMax(BohnenspielBoardState boardState, double alpha, double beta, int maxDepth, boolean isMax) {

        double value;

        //we have reached our max depth so we need to return
        if (maxDepth == 0) {
            value = evaluateBoard(boardState, this);
            return new Decision(value);
        }

        ArrayList<BohnenspielMove> moves = boardState.getLegalMoves();

        //we have reached our max depth so we need to return
        if (moves.size() == 0) {
            value = evaluateBoard(boardState, this);
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

                //you have won by default
                if(boardState.getScore(player_id) > 36){
                    return new Decision(Integer.MAX_VALUE, currentMove);
                }

                //the other player wins by default
                if(boardState.getScore(opponent_id) > 36){
                    return new Decision(Integer.MIN_VALUE, currentMove);
                }

                //recursive call to continue DFS from opponent's perspective
                returnMove = minMax(cloned_board_state, alpha, beta, maxDepth - 1, false);

                if(maxDepth == 15){
                    System.out.println("return score: " + returnMove.returnValue);
                }

                if ((bestMove == null) || (bestMove.returnValue < returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue > alpha) {
                    alpha = returnMove.returnValue;
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (beta <= alpha) {
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

                if ((bestMove == null) || (bestMove.returnValue > returnMove.returnValue)) {
                    bestMove = returnMove;
                    bestMove.returnMove = currentMove;
                }
                if (returnMove.returnValue < beta) {
                    beta = returnMove.returnValue;
                    bestMove = returnMove;
                }
                if (beta <= alpha) {
//                    bestMove.returnValue = alpha;
//                    bestMove.returnMove = null;
                    return bestMove; // pruning
                }
            }
            return bestMove;
        }
    }

    private double evaluateBoard(BohnenspielBoardState boardState, BohnenspielPlayer me){

        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits = boardState.getPits();

        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        int[] my_pits = pits[player_id];

        double my_score = boardState.getScore(player_id);
        double op_score = boardState.getScore(opponent_id);

        //ideally you want to make moves that increases your score at faster rate than your opponents
        double score_factor = my_score - op_score;

        double my_seeds = 0;
        for(int i : my_pits){
            my_seeds += i;
        }

        return 1 * score_factor + 1 * my_seeds;

    }
}




