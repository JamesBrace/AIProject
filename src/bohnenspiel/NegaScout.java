package bohnenspiel;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.*;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;

/**
 * NegaScout, a subclass of MoveAlgorithm, is a negascout search algorithm.
 * It is a variation of the "alfa-beta pruning" technique.
 *
 * @author Arvydas Bancewicz
 *
 */
public class NegaScout extends BohnenspielPlayer{

    /**
     * Create an negaScout algorithm object.
     */
    private boolean isFirstMove = true;

    public NegaScout() { super("NegaScout") ;}

    public class Decision {

        double returnValue;
        BohnenspielMove returnMove;

        Decision() {
            returnValue = 0;
        }

        Decision(double returnValue) {
            this.returnValue = returnValue;
        }

        public Decision(double returnValue, BohnenspielMove returnMove) {
            this.returnValue = returnValue;
            this.returnMove = returnMove;
        }

    }

    public BohnenspielMove chooseMove(BohnenspielBoardState board_state)
    {

        long endTime = (isFirstMove)? System.nanoTime() + 29_000_000_000L : System.nanoTime() + 600_000_000L;

        if(isFirstMove && player_id == 0){
            isFirstMove = false;
            ArrayList<BohnenspielMove> moves = board_state.getLegalMoves();
            return new BohnenspielMove(2);
        }

        isFirstMove = false;

        final ExecutorService service = Executors.newSingleThreadExecutor();

        Decision currentBest = new Decision(Integer.MIN_VALUE);

        Callable<BohnenspielMove> task = () -> {

            int maxDepth = (board_state.getTurnNumber() == 0)? 13 : 9;

            Decision decision= new Decision();

            while(maxDepth < 25){
                decision = negaScout(board_state, Integer.MIN_VALUE, Integer.MAX_VALUE, maxDepth, 1);

                currentBest.returnValue = decision.returnValue;
                currentBest.returnMove = decision.returnMove;

                System.out.println(maxDepth);
                maxDepth++;

            }
            return decision.returnMove;
        };

        try {
            final Future<BohnenspielMove> f = service.submit(task);
            return f.get(endTime - System.nanoTime(), TimeUnit.NANOSECONDS);
        } catch (final TimeoutException e) {
            System.out.println(currentBest.returnMove);
            return currentBest.returnMove;
        } catch (final Exception e) {
            return currentBest.returnMove;
        } finally {
            service.shutdown();
        }
    }

    public Decision negaScout(BohnenspielBoardState boardState, double alpha, double beta, int depth, int isMax) {
        double value;

        //we have reached our max depth so we need to return
        if (depth == 0) {
            value = evaluateBoard(boardState, this);
            return new Decision(isMax * value);
        }

        ArrayList<BohnenspielMove> moves = boardState.getLegalMoves();

        //if there are no more legal moves
        if (moves.size() == 0) {
            value = evaluateBoard(boardState, this);
            return new Decision(isMax * value);
        }

        Decision returnMove;
        Decision bestMove = null;

        for (BohnenspielMove currentMove : moves) {

            //inspect the outcome of each legal move
            BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) boardState.clone();

            //check to make sure it is not an infinite move
            if(!cloned_board_state.move(currentMove)) continue;

            //you have won by default
            if(boardState.getScore(player_id) > 36){
                return new Decision(Integer.MAX_VALUE * isMax, currentMove);
            }

            //the other player wins by default
            if(boardState.getScore(opponent_id) > 36){
                return new Decision(Integer.MIN_VALUE * isMax, currentMove);
            }

            //if not first child...
            if(!(moves.indexOf(currentMove)==0)){
                //recursive call to continue DFS from opponent's perspective
                returnMove = negaScout(cloned_board_state, (alpha * -1) - 1, alpha * -1, depth - 1, isMax * -1);

                if (alpha < returnMove.returnValue && returnMove.returnValue < beta){
                    returnMove = negaScout(cloned_board_state, -beta, -returnMove.returnValue, depth - 1, isMax * -1);
                }
            }
            else{

                System.out.println("analyzing first move");
                returnMove = negaScout(cloned_board_state, -beta, -alpha, depth - 1, isMax * -1);
            }

            if(returnMove.returnValue > alpha){
                System.out.println("return move was better than alpha");
                bestMove = returnMove;
                alpha = returnMove.returnValue;
            }

            if(alpha >= beta){
                System.out.println(bestMove);
                return bestMove;
            }

        }
        System.out.println(bestMove);
        return bestMove;
    }

    /**
     * Get the name of this algorithm
     */
    public String toString() {
        return "NegaScout";
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