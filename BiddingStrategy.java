package ai2016;

import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

/**
 * Created by archah on 19/10/2016.
 */
public class BiddingStrategy {

    private AbstractUtilitySpace utilSpace;
    private double MINIMUM_UTILITY;
    private OpponentList opponents;


    public BiddingStrategy(AbstractUtilitySpace utilSpace, double minUtil, OpponentList opponents) {
        this.utilSpace = utilSpace;
        this.MINIMUM_UTILITY = minUtil;
        this.opponents = opponents;
    }

    public Bid getNextBid() throws Exception {


        OpponentModel oppAvg = opponents.getAverageOpponentModel(utilSpace);


        try {
            return oppAvg.getUtilSpace().getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
