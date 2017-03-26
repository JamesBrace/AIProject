package student_player;

import boardgame.Move;
import bohnenspiel.BohnenspielMove;

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
