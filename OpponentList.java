package ai2016;

import negotiator.AgentID;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;

import java.util.ArrayList;

/**
 * Created by archah on 24/10/2016.
 */
public class OpponentList extends ArrayList<OpponentModel> {

    public OpponentModel getOpponent(AgentID sender, AbstractUtilitySpace utilSpace) {

        OpponentModel opponent;

        if (utilSpace instanceof AdditiveUtilitySpace)
            opponent = new OpponentModelDiscrete(sender);
        else
            opponent = new OpponentModel(sender);

        if(this.contains(opponent)) {
            opponent = this.get(this.indexOf(opponent));
        } else {
            this.add(opponent);
            opponent.init(utilSpace);
        }

        return opponent;
    }


    public OpponentModel getAvarageOpponentModel() {
        return null;
    }
}

