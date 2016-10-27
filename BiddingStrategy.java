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

    public BiddingStrategy(AbstractUtilitySpace utilSpace, OpponentList opponents) {
        this.utilSpace = utilSpace;
        this.opponents = opponents;

        try {
            maxBid = utilSpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bid getNextBid(NegotiationState STATE, ArrayList<Bid> samples) {
        Bid bid;
        switch (STATE) {
            case MEAN_MODELING:
                opponents.stopModeling();
            case OPPONENT_MODELING:
                bid = getNextHardHeaded(samples);
                break;
            case CONCEDING:
                bid = getNextConcessionBid(samples);
                break;
            case DEADLINE:
                bid = getNextConcessionBid(samples);
                break;
            default:
                bid = getNextHardHeaded(samples);
        }

        return bid;
    }

    private Bid getNextHardHeaded(ArrayList<Bid> samples) {
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

    public void updateAverageOpponent() {
        averageOpponent = opponents.getAverageOpponentModel(utilSpace);
    }
}