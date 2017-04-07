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

        long endTime;
        long time = new Date().getTime();

        //first move... take advantage of it
        if(isFirstMove){
            isFirstMove = false;
            endTime = time + 20000;
        }
        else{
            endTime = time + 500;
        }

        final ExecutorService service = Executors.newSingleThreadExecutor();

        final Decision currentBest = new Decision(Integer.MIN_VALUE);

        Callable<BohnenspielMove> task = () -> {

            int maxDepth = 8;
            Decision decision= new Decision();


            while(maxDepth < 25){
                decision = minMax(board_state, Integer.MIN_VALUE, Integer.MAX_VALUE, maxDepth, true);

                if(decision.returnValue > currentBest.returnValue){
                    currentBest.returnValue = decision.returnValue;
                    currentBest.returnMove = decision.returnMove;
                }

                maxDepth++;
            }

            System.out.println("return move: " + decision.returnMove);
            return decision.returnMove;
        };

        try {
            final Future<BohnenspielMove> f = service.submit(task);

           return f.get(endTime - new Date().getTime(), TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            System.out.println("returning current best" + currentBest.returnMove);
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

    private double evaluateBoard(BohnenspielBoardState boardState, BohnenspielPlayer me){
//        int[] evenNums = {0, 2, 4, 6};
//        int[] oddNums = {1, 3, 5};
//
        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits = boardState.getPits();

//        int player = me.getColor();
//        int opponent = (player + 1) % 2;
//
        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        int[] my_pits = pits[player_id];
//        int[] op_pits = pits[opponent];

        double my_score = boardState.getScore(player_id);
        double op_score = boardState.getScore(opponent_id);

        //ideally you want to make moves that increases your score at faster rate than your opponents
        double score_factor = my_score - op_score;
//
        double my_seeds = 0;
        for(int i : my_pits){
            my_seeds += i;
        }
//
//        double op_seeds = 0;
//        for(int i : op_pits){
//            op_seeds += i;
//        }
//
//        //game ends if opponent can't make move, take advantage of that
//        if(op_seeds == 0 ) {
//            if (my_score > op_score) {
//                return Integer.MAX_VALUE;
//            } else if (op_score > my_score) {
//                return Integer.MIN_VALUE;
//            }
//        }
//
//        //ideally you want more seeds on your side
//        double pit_factor = my_seeds - op_seeds;
//
//        //now you also want to be in a position where your opponent is left with pits with the maximum amount of
//        //0's, 2's, 4's, or 6's
//        int evens = 0;
//        int odds = 0;
//
//        for(int i = 0; i < my_pits.length; i++){
//            if(contains(evenNums, my_pits[i]) || contains(evenNums, op_pits[i])){
//                evens++;
//            }
//            else if(contains(oddNums, my_pits[i]) || contains(oddNums, op_pits[i])){
//                odds++;
//            }
//        }
//
//        double distribution_factor = evens - odds;

        return 1 * score_factor + 1 * my_seeds; //+ 0 * pit_factor + 0 * distribution_factor;

    }
}




