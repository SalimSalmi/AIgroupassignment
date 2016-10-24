package ai2016;

import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

import java.util.ArrayList;

/**
 * Created by archah on 19/10/2016.
 */
public class AcceptanceStrategy {

    private AbstractUtilitySpace utilSpace;
    private double MINIMUM_UTILITY;
    private ArrayList<OpponentModel> opponents;


    public AcceptanceStrategy(AbstractUtilitySpace utilSpace, double minUtil, ArrayList<OpponentModel> opponents) {
        this.utilSpace = utilSpace;
        this.MINIMUM_UTILITY = minUtil;
        this.opponents = opponents;
    }

    public boolean accept(Bid bid){

        return utilSpace.getUtility(bid) > MINIMUM_UTILITY;



    }
}
