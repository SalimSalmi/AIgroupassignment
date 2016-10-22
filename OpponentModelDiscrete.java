package ai2016;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by archah on 08/10/2016.
 */
public class OpponentModelDiscrete extends OpponentModel{

    private AdditiveUtilitySpace utilSpace;

    private ArrayList<Bid> bids = new ArrayList<>();
    private int MAX_NUM_BIDS = 100;

    private ArrayList<ArrayList<ValueDiscrete>> issues = new ArrayList<>();


    public OpponentModelDiscrete(AgentID agentID){
        super(agentID);
    }

    @Override
    public void init(AbstractUtilitySpace utilSpace) {
        this.utilSpace = (AdditiveUtilitySpace) utilSpace;

        for (int n=1; n<=this.utilSpace.getNrOfEvaluators() ; n++) {
            Evaluator evaluator = this.utilSpace.getEvaluator(n);
            evaluator.setWeight(1.0/this.utilSpace.getNrOfEvaluators());

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
        if(bids.size() < MAX_NUM_BIDS){
            bids.add(bid);
            updateModel(bid);

            for (int n=1; n<=utilSpace.getNrOfEvaluators() ; n++          ) {
                Evaluator evaluator = utilSpace.getEvaluator(n);
                System.out.println("evaluator: "+evaluator);
                System.out.println("weight="+ evaluator.getWeight());
                System.out.println();
            }
        }
    }

    private void updateModel(Bid bid){



        Iterator it = bid.getValues().entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            EvaluatorDiscrete evaluator = (EvaluatorDiscrete) utilSpace.getEvaluator((int) pair.getKey());

            ValueDiscrete value = (ValueDiscrete) pair.getValue();

            try {
                evaluator.setEvaluationDouble(value, evaluator.getEvaluationNotNormalized(value) + 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
