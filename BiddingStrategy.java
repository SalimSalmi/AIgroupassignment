package ai2016;

import negotiator.Bid;
import negotiator.issue.*;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by archah on 19/10/2016.
 */
public class BiddingStrategy {

    private AbstractUtilitySpace utilSpace;
    private double MINIMUM_UTILITY;
    private OpponentList opponents;

    private HashMap<Integer, HashMap<Value, Double>> values = new HashMap<>();
    private HashMap<Integer, Double> weights = new HashMap<>();
    private Bid currentBid;
    private Bid targetBid;
    private OpponentModel averageOpponent;

    public BiddingStrategy(AbstractUtilitySpace utilSpace, double minUtil, OpponentList opponents) {
        this.utilSpace = utilSpace;
        this.MINIMUM_UTILITY = minUtil;
        this.opponents = opponents;

        setEvalValues();
    }

    public Bid getNextBid() {

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

        currentBid = getReducedBid(targetBid, currentBid);

        return currentBid;
    }

    public void updateAverageOpponent(){
        averageOpponent = opponents.getAverageOpponentModel(utilSpace);
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
