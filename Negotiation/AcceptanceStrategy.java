package ai2016;

import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

/**
 * Created by archah on 19/10/2016.
 */
public class AcceptanceStrategy {

    private AbstractUtilitySpace utilSpace;
    private MinimumUtility minimumUtility;

    private Bid previousAccepted;

    /**
     * Create an acceptance strategy which judges bids to accept
     *
     * @param utilSpace The utility space of the agent
     * @param minimumUtility The minimum utility curve
     */
    public AcceptanceStrategy(AbstractUtilitySpace utilSpace, MinimumUtility minimumUtility) {
        this.utilSpace = utilSpace;
        this.minimumUtility = minimumUtility;
    }

    /**
     * Judges if a bid should be accepted or not.
     *
     * @param bid The bid which should be judged
     * @param nextBid The bid which the agent will give next
     * @param state The state the negotiation is in
     *
     * @return True if the bid is accepted, false otherwise.
     */
    public boolean accept(Bid bid, Bid nextBid, NegotiationState state){

        // Set a ratio to the minimum utility we will accept.
        float deadlineRatio = 1;

        if(state == NegotiationState.DEADLINE) {
            // Lower the ratio to allow for more acceptable bids.
            deadlineRatio = 0.75f;
        }

        boolean accept;

        // If the utility of our upcoming bid is lower than the bid we received then accept;
        if(utilSpace.getUtility(nextBid) < utilSpace.getUtility(bid)) {
            accept = true;
        } else {
            accept = utilSpace.getUtility(bid) > minimumUtility.get() * deadlineRatio;
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
