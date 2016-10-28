package ai2016;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by archah on 08/10/2016.
 */
public class OpponentModelDiscrete extends OpponentModel{

    private AdditiveUtilitySpace additiveUtilitySpace;

    public OpponentModelDiscrete(AgentID agentID){
        super(agentID);
    }

    @Override
    public void init(AbstractUtilitySpace utilSpace) {
        super.init(utilSpace);
        this.additiveUtilitySpace = (AdditiveUtilitySpace) utilSpace;

        // Reset the weights and the values for the utility space.
        for (int n=1; n<=this.additiveUtilitySpace.getNrOfEvaluators() ; n++) {
            Evaluator evaluator = this.additiveUtilitySpace.getEvaluator(n);
            evaluator.setWeight(1.0/this.additiveUtilitySpace.getNrOfEvaluators());

            if (evaluator instanceof EvaluatorDiscrete){
                EvaluatorDiscrete deval = (EvaluatorDiscrete)evaluator;

                for(ValueDiscrete value : deval.getValues()) {
                    try {
                        deval.setEvaluationDouble(value, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }



    @Override
    public void pushBid(Bid bid){
        super.pushBid(bid);
        if(modeling){
            // Update the utility space
            updateModel(bid);
        }
    }

    public AdditiveUtilitySpace getUtilSpace(){
        return additiveUtilitySpace;
    }

    private void updateModel(Bid bid){

        Iterator it = bid.getValues().entrySet().iterator();

        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry)it.next();
            EvaluatorDiscrete evaluator = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator((int) pair.getKey());
            ValueDiscrete value = (ValueDiscrete) pair.getValue();
            try {
                evaluator.setEvaluationDouble(value, evaluator.getEvaluationNotNormalized(value) + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            it.remove(); // avoids a ConcurrentModificationException
        }

        if(bids.size() > 1) {

            Bid b_p = bids.get(bids.size() - 2);
            Iterator it1 = bid.getValues().entrySet().iterator();
            double w_s = 0;

            while (it1.hasNext()) {

                Map.Entry pair = (Map.Entry) it1.next();
                EvaluatorDiscrete evaluator1 = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator((int) pair.getKey());
                ValueDiscrete value_c = (ValueDiscrete) pair.getValue();
                ValueDiscrete value_p = (ValueDiscrete) b_p.getValue((int) pair.getKey());

                if(value_c == value_p){
                    double n_w = evaluator1.getWeight() + 0.1;
                    evaluator1.setWeight( n_w );
                }

                w_s = w_s + evaluator1.getWeight();
            }

            it1 = bid.getValues().entrySet().iterator();

            while (it1.hasNext()){

                Map.Entry pair1 = (Map.Entry) it1.next();
                EvaluatorDiscrete evaluator1 = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator((int) pair1.getKey());
                double o_w = evaluator1.getWeight();
                evaluator1.setWeight(o_w/w_s);

            }
        }
    }
}
