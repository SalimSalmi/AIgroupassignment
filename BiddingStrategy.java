package ai2016;

import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

import java.util.ArrayList;

/**
 * Created by archah on 19/10/2016.
 */
public class BiddingStrategy {

    private AbstractUtilitySpace utilSpace;
    private OpponentList opponents;

    private Bid maxBid;
    private OpponentModel averageOpponent;
    private MinimumUtility minimumUtility;


    /**
     * Create a bidding strategy which decides which bids to give
     *
     * @param utilSpace The utility space of the agent
     * @param minimumUtility The minimum utility curve
     * @param opponents The list of opponents
     */
    public BiddingStrategy(AbstractUtilitySpace utilSpace, MinimumUtility minimumUtility, OpponentList opponents) {
        this.utilSpace = utilSpace;
        this.opponents = opponents;
        this.minimumUtility = minimumUtility;

        try {
            maxBid = utilSpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the next bid the agent will give according to the state of the negotiation.
     *
     * @param STATE The state the negotiation is in.
     * @param samples List of bid samples above the minimum utility.
     *
     * @return The next bid to give.
     */
    public Bid getNextBid(NegotiationState STATE, ArrayList<Bid> samples) {
        Bid bid;
        switch (STATE) {
            case MEAN_MODELING:
                opponents.stopModeling();
            case OPPONENT_MODELING:
                bid = getNextNaiveBid(samples);
                break;
            case CONCEDING:
                bid = getNextConcessionBid(samples);
                break;
            case DEADLINE:
                bid = getNextConcessionBid(samples);
                break;
            default:
                bid = getNextNaiveBid(samples);
        }

        return bid;
    }

    private Bid getNextNaiveBid(ArrayList<Bid> samples) {
        // Get the lowest bid (closest to reservation curve).

        if (samples.size() == 0) {
            return maxBid;
        }
        Bid nextBid = samples.get(0);
        double utilNext = utilSpace.getUtility(nextBid);
        for (Bid bid : samples) {
            double util = utilSpace.getUtility(bid);
            if (util < utilNext) {
                utilNext = util;
                nextBid = bid;
            }
        }

        return nextBid;
    }

    private Bid getNextConcessionBid(ArrayList<Bid> samples) {
        // Get the bid that maximizes the average opponent utility.

        if (samples.size() == 0) {
            return maxBid;
        }
        Bid nextBid = maxBid;
        double utilNext = averageOpponent.getUtilSpace().getUtility(nextBid);
        for (Bid bid : samples) {
            double util = averageOpponent.getUtilSpace().getUtility(bid);
            if (util > utilNext) {
                utilNext = util;
                nextBid = bid;
            }
        }

        return nextBid;

    }

    /**
     * Recalculates the average opponent model.
     */
    public void updateAverageOpponent() {
        averageOpponent = opponents.getAverageOpponentModel(utilSpace);

        try {
            minimumUtility.goal(utilSpace.getUtility(averageOpponent.getUtilSpace().getMaxUtilityBid()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}