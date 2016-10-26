package ai2016;

import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

/**
 * Created by archah on 19/10/2016.
 */
public class AcceptanceStrategy {

    private AbstractUtilitySpace utilSpace;
    private MinimumUtility minimumUtility;
    private OpponentList opponents;

    private Bid previousAccepted;


    public AcceptanceStrategy(AbstractUtilitySpace utilSpace, MinimumUtility minimumUtility, OpponentList opponents) {
        this.utilSpace = utilSpace;
        this.minimumUtility = minimumUtility;
        this.opponents = opponents;
    }

    public boolean accept(Bid bid, Bid nextBid){

        boolean accept = false;

        if ( utilSpace.getUtility(bid) > minimumUtility.get() ){
            System.out.println("The util is : " + utilSpace.getUtility(bid) + ", the min util is : " + minimumUtility.get());
        }

        // If the utility of our upcoming bid is lower than the bid we received then accept;
        if(utilSpace.getUtility(nextBid) < utilSpace.getUtility(bid)) {
            accept = true;
        } else {
            accept = utilSpace.getUtility(bid) > minimumUtility.get();
        }

        // If we get the same bid again that we accepted last round, give our own bid.
        if(accept && previousAccepted != null) {
            if(previousAccepted.equals(bid)) {
                accept = false;
                previousAccepted = null;
            } else {
                previousAccepted = bid;
            }
        }

        return accept;


    }
}
