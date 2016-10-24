package ai2016;

import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

/**
 * Created by archah on 19/10/2016.
 */
public class AcceptanceStrategy {

    private AbstractUtilitySpace utilSpace;
    private double MINIMUM_UTILITY;
    private OpponentList opponents;


    public AcceptanceStrategy(AbstractUtilitySpace utilSpace, double minUtil, OpponentList opponents) {
        this.utilSpace = utilSpace;
        this.MINIMUM_UTILITY = minUtil;
        this.opponents = opponents;
    }

    public boolean accept(Bid bid){

        //System.out.println("The util is : "+utilSpace.getUtility(bid));
        return utilSpace.getUtility(bid) > MINIMUM_UTILITY;



    }
}
