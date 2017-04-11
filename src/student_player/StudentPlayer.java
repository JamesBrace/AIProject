package student_player;

import java.util.ArrayList;
import java.util.concurrent.*;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;


/** A Hus player submitted by a student. */
public class StudentPlayer extends BohnenspielPlayer {

    private boolean isFirstMove = true;
    private ArrayList<BohnenspielMove> bestMoves = new ArrayList<>();
    private int turn = 1;

    public StudentPlayer() { super("260654858"); }


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

            int maxDepth = (board_state.getTurnNumber() == 0)? 16 : 10;

            Decision decision= new Decision();

            while(maxDepth < 25){
                decision = minMax(board_state, Integer.MIN_VALUE, Integer.MAX_VALUE, maxDepth, true);

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
            throw new RuntimeException(e);
        } finally {
            service.shutdown();
        }
    }

    private Decision minMax(BohnenspielBoardState boardState, double alpha, double beta, int maxDepth, boolean isMax) {

        double value;

        //we have reached our max depth so we need to return
        if (maxDepth == 0) {
            value = evaluateBoard(boardState, this);
            return new Decision(value);
        }

        ArrayList<BohnenspielMove> moves = boardState.getLegalMoves();

        //if there are no more legal moves
        if (moves.size() == 0) {
            value = evaluateBoard(boardState, this);
            return new Decision(value);
        }

        Decision returnMove;
        Decision bestMove = null;

        //if you are trying to get the maximum value...
        if (isMax) {
            for (BohnenspielMove currentMove : moves) {

                //inspect the outcome of each legal move
                BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) boardState.clone();

                //check to make sure it is not an infinite move
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
                    return bestMove;
                }
            }

            return bestMove;

        } else {
            for(BohnenspielMove currentMove : moves) {

                //inspect the outcome of each legal move
                BohnenspielBoardState cloned_board_state = (BohnenspielBoardState) boardState.clone();

                //inspect the outcome of the move
                cloned_board_state.move(currentMove);

                //you have won by default
                if(boardState.getScore(player_id) > 36){
                    return new Decision(Integer.MAX_VALUE, currentMove);
                }

                //the other player wins by default
                if(boardState.getScore(opponent_id) > 36){
                    return new Decision(Integer.MIN_VALUE, currentMove);
                }

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
                    return bestMove;
                }
            }
            return bestMove;
        }
    }

    private double evaluateBoard(BohnenspielBoardState boardState, BohnenspielPlayer me){

        int winner = boardState.getWinner();

        if (winner == BohnenspielBoardState.NOBODY) {

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
        else if (winner == player_id) {
            return Integer.MAX_VALUE;
        }
        else if (winner == opponent_id) {
            return Integer.MIN_VALUE;
        }

        return 0;
    }



}




