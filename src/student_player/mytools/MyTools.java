package student_player.mytools;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielPlayer;


import java.util.Arrays;

public class MyTools {

    public static double getSomething(){
        return Math.random();
    }

    public static double evaluateBoard(BohnenspielBoardState boardState, BohnenspielPlayer me){
        int[] evenNums = {0, 2, 4, 6};
        int[] oddNums = {1, 3, 5};

        // Get the contents of the pits so we can use it to make decisions.
        int[][] pits = boardState.getPits();

        int player = me.getColor();
        int opponent = (player + 1) % 2;

        // Use ``player_id`` and ``opponent_id`` to get my pits and opponent pits.
        int[] my_pits = pits[player];
        int[] op_pits = pits[opponent];

        double my_score = boardState.getScore(player);
        double op_score = boardState.getScore(opponent);

        //ideally you want to make moves that increases your score at faster rate than your opponents
        double score_factor = my_score - op_score;

        double my_seeds = 0;
        for(int i : my_pits){
            my_seeds += i;
        }

        double op_seeds = 0;
        for(int i : op_pits){
            op_seeds += i;
        }

        //game ends if opponent can't make move, take advantage of that
        if(op_seeds == 0 ) {
            if (my_score > op_score) {
                return Integer.MAX_VALUE;
            } else if (op_score > my_score) {
                return Integer.MIN_VALUE;
            }
        }

        //ideally you want more seeds on your side
        double pit_factor = my_seeds - op_seeds;

        //now you also want to be in a position where your opponent is left with pits with the maximum amount of
        //0's, 2's, 4's, or 6's
        int evens = 0;
        int odds = 0;

        for(int i = 0; i < my_pits.length; i++){
            if(contains(evenNums, my_pits[i]) || contains(evenNums, op_pits[i])){
                evens++;
            }
            else if(contains(oddNums, my_pits[i]) || contains(oddNums, op_pits[i])){
                odds++;
            }
        }

        double distribution_factor = evens - odds;

        return 1 * score_factor + 0 * pit_factor + 0 * distribution_factor;

    }

    private static boolean contains(final int[] array, final int key) {
        return Arrays.asList(array).contains(key);
    }

}
