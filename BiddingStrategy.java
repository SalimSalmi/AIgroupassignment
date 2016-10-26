package ai2016;

import negotiator.Bid;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.*;

/**
 * Created by archah on 19/10/2016.
 */
public class BiddingStrategy {

    private AbstractUtilitySpace utilSpace;
    private MinimumUtility minimumUtility;
    private OpponentList opponents;
    private float hardHeadedDeadline;

    private HashMap<Integer, HashMap<Value, Double>> values = new HashMap<>();
    private HashMap<Integer, Double> weights = new HashMap<>();
    private Bid currentBid;
    private Bid targetBid;
    private OpponentModel averageOpponent;
    private ArrayList<Bid> topBids;

    public BiddingStrategy(AbstractUtilitySpace utilSpace, MinimumUtility minimumUtility, OpponentList opponents, float hardHeadedDeadline) {
        this.utilSpace = utilSpace;
        this.minimumUtility = minimumUtility;
        this.opponents = opponents;
        this.hardHeadedDeadline = hardHeadedDeadline;

        setEvalValues();


        try {
            TreeMap<Double, Bid> treeMap = new TreeMap<>();
            recursiveTopBids(utilSpace.getMaxUtilityBid(), treeMap);
            topBids = new ArrayList<>(treeMap.values());
            Collections.reverse(topBids);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bid getNextBid(NegotiationState STATE, double time){
        Bid bid;
        switch(STATE) {
            case MEAN_MODELING:
                opponents.stopModeling();
            case OPPONENT_MODELING:
                bid = getNextHardHeaded(time);
                break;
            case CONCEDING:
                bid = getNextConcessionBid();
                break;
            case DEADLINE:
                bid = getNextConcessionBid();
                break;
            default:
                bid = getNextHardHeaded(time);
        }

        return bid;
    }

    private Bid getNextHardHeaded(double time) {
        if(time / hardHeadedDeadline <=1) {
            int power = 2;
            double adjustedTime = (Math.pow(Math.E, power * time) - 1)/(Math.pow(Math.E, power) - 1);
            //return topBids.get((int) Math.floor(adjustedTime * topBids.size()));
            return topBids.get((int) Math.floor(time / hardHeadedDeadline * topBids.size()));
            //return topBids.get((int) Math.floor(Math.pow(2, -Math.ceil(time / hardHeadedDeadline * topBids.size()))*topBids.size()));
        } else {
            return topBids.get(0);
        }

    }

    private Bid getNextConcessionBid() {

        try {
            if(currentBid == null) {
                currentBid = utilSpace.getMaxUtilityBid();
            }
            if(targetBid == null) {
                if(averageOpponent == null) {
                    updateAverageOpponent();
                }
                targetBid = averageOpponent.getUtilSpace().getMaxUtilityBid();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bid nextBid = getReducedBid(targetBid, currentBid);


        if(utilSpace.getUtility(nextBid) > minimumUtility.get()) {
            System.out.println("Lowering, minimum util: " + minimumUtility.get() + ", next bid: " + utilSpace.getUtility(nextBid));
            currentBid = nextBid;
        }

        return currentBid;
    }

    public void updateAverageOpponent(){
        averageOpponent = opponents.getAverageOpponentModel(utilSpace);
        try {
            targetBid = averageOpponent.getUtilSpace().getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recursiveTopBids(Bid bid, TreeMap<Double, Bid> treeMap) {

        treeMap.put(utilSpace.getUtility(bid), bid);

        for (Map.Entry<Integer, Value> issue : bid.getValues().entrySet()) {

            double currentIssue = values.get(issue.getKey()).get(issue.getValue());
            double maxValue = 0;

            Bid newBid = new Bid(bid);

            for (Map.Entry<Value, Double> value : values.get(issue.getKey()).entrySet()) {

                double current = value.getValue();

                if(current < currentIssue && current >= maxValue) {
                    maxValue = current;
                    newBid = newBid.putValue(issue.getKey(), value.getKey());
                }

            }

            if (utilSpace.getUtility(newBid) < minimumUtility.get(hardHeadedDeadline) ) {
                System.out.println("Stop at utility: " + utilSpace.getUtility(bid));
            } else if(treeMap.containsValue(newBid)) {
                System.out.println("Already contains this bid");
            } else {
                recursiveTopBids(newBid, treeMap);
            }
        }
    }

    private void setEvalValues() {

        if (utilSpace instanceof AdditiveUtilitySpace) {
            AdditiveUtilitySpace myspace = (AdditiveUtilitySpace) utilSpace;

            for (int n=1; n<=myspace.getNrOfEvaluators() ; n++) {
                Evaluator evaluator = myspace.getEvaluator(n);
                weights.put(n, evaluator.getWeight());

                if (evaluator instanceof EvaluatorDiscrete){
                    HashMap<Value, Double> map = new HashMap<>();
                    EvaluatorDiscrete dEval = (EvaluatorDiscrete) evaluator;

                    for(Iterator<ValueDiscrete> i = dEval.getValues().iterator(); i.hasNext(); ) {
                        ValueDiscrete v = i.next();

                        try {
                            map.put(v, dEval.getEvaluation(v));
                        } catch (Exception e) {
                            System.out.println("Can't get value.");
                            e.printStackTrace();
                        }
                    }
                    values.put(n, map);
                }
            }
        }
    }

    private Bid getReducedBid(Bid target, Bid current) {

        // If the bids are already the same utility return the target.
        if(utilSpace.getUtility(target) == utilSpace.getUtility(current)) {
            return target;
        } if(utilSpace.getUtility(target) > utilSpace.getUtility(current)) {
            return getIncreasedBid(target, current);
        }

        // Calculate on which issue the values of the bids are furthest away.
        double maxValue = 0;
        int index = -1;
        for (Map.Entry<Integer, Value> pair : target.getValues().entrySet()) {

            double vTarget = values.get(pair.getKey()).get(pair.getValue());
            double vCurrent = values.get(pair.getKey()).get(current.getValue(pair.getKey()));
            double d = (vCurrent - vTarget) / weights.get(pair.getKey());

            if(d > maxValue){
                maxValue = d;
                index = pair.getKey();
            }
        }

        // For the found issue move the value of the current bid one step closer to the target bid.
        if(index >= 0) {
            Bid newBid = new Bid(current);
            double vCurrent = values.get(index).get(current.getValue(index));

            if(maxValue == 0) {
                return newBid.putValue(index, target.getValue(index));
            }

            maxValue = 0;
            for (Map.Entry<Value, Double> pair : values.get(index).entrySet()) {

                double value = values.get(index).get(pair.getKey());

                if(value < vCurrent && value >= maxValue) {
                    maxValue = value;
                    newBid = newBid.putValue(index, pair.getKey());
                }

            }

            return newBid;
        }

        return current;
    }

    private Bid getIncreasedBid(Bid target, Bid current) {

        // Calculate on which issue the values of the bids are furthest away.
        double maxValue = 0;
        int index = -1;
        for (Map.Entry<Integer, Value> pair : target.getValues().entrySet()) {

            double vTarget = values.get(pair.getKey()).get(pair.getValue());
            double vCurrent = values.get(pair.getKey()).get(current.getValue(pair.getKey()));
            double d = (vTarget - vCurrent) * weights.get(pair.getKey());

            if(d > maxValue){
                maxValue = d;
                index = pair.getKey();
            }
        }

        // For the found issue move the value of the current bid one step closer to the target bid.
        if(index >= 0) {
            Bid newBid = new Bid(current);
            double vCurrent = values.get(index).get(current.getValue(index));

            if(maxValue == 0) {
                return newBid.putValue(index, target.getValue(index));
            }

            maxValue = Integer.MAX_VALUE;
            for (Map.Entry<Value, Double> pair : values.get(index).entrySet()) {

                double value = values.get(index).get(pair.getKey());

                if(value > vCurrent && value < maxValue) {
                    maxValue = value;
                    newBid = newBid.putValue(index, pair.getKey());
                }
            }
            return newBid;
        }

        return current;
    }
}
