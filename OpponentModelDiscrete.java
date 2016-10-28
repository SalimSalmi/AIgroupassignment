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
import java.util.List;
import java.util.Map;

/**
 * Created by archah on 08/10/2016.
 */
public class OpponentModelDiscrete extends OpponentModel{

    private AdditiveUtilitySpace utilSpace;
    private int BLOCK_SIZE;

    private ArrayList<Bid> bids = new ArrayList<>();
    private ArrayList<Double> ownUtils = new ArrayList<>();
    private boolean modeling = true;

    public OpponentModelDiscrete(AgentID agentID, int BLOCK_SIZE){
        super(agentID);
        this.BLOCK_SIZE = BLOCK_SIZE;
    }

    @Override
    public void init(AbstractUtilitySpace utilSpace) {
        super.init(utilSpace);
        this.utilSpace = (AdditiveUtilitySpace) utilSpace;

        // Reset the weights and the values for the utility space.
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

    public void stopModeling(){
        this.modeling = false;
    }


    @Override
    public void pushBid(Bid bid, double ownUtil){

        // Add bid to bid history
        bids.add(bid);
        // Add utils of our own Agent for the bid to history
        ownUtils.add(ownUtil);

        if(modeling){

            // Update the utility space
            updateModel(bid);

        }
    }

    public AdditiveUtilitySpace getUtilSpace(){

        return utilSpace;

    }

    public double getRelativeDistance(Bid bid){
        double bidUtil = utilSpace.getUtility(bid);
        double max = 1;
        int block = 3;

        int start = bids.size()-block < 0 ? 0 : bids.size() -block;

        List<Bid> sublist = bids.subList(start, bids.size());

        double average = 0;

        for(Bid b : sublist) {
            average += utilSpace.getUtility(b);
        }

        average = average / block;

        return (average - bidUtil)/(max - bidUtil);
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

        if(bids.size() > 1) {

            Bid b_p = bids.get(bids.size() - 2);

            Iterator it1 = bid.getValues().entrySet().iterator();

            double w_s = 0;

            while (it1.hasNext()) {

                Map.Entry pair = (Map.Entry) it1.next();

                EvaluatorDiscrete evaluator1 = (EvaluatorDiscrete) utilSpace.getEvaluator((int) pair.getKey());

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

                EvaluatorDiscrete evaluator1 = (EvaluatorDiscrete) utilSpace.getEvaluator((int) pair1.getKey());


                double o_w = evaluator1.getWeight();

                evaluator1.setWeight(o_w/w_s);


            }

        }

    }

    public double getDropRate(){

        double average = 0;
        int interval = 10;///Set it later
        int count = 0;
        double avg_final = 0;

        for(int i = 1; i < (bids.size()-interval); i++) {

            double avg_temp = 0;

            double prev = utilSpace.getUtility(bids.get(i-1));


            for(int j=i; j< (i+interval);j++ ){

                double curr = utilSpace.getUtility(bids.get(j));

                avg_temp += (curr - prev)*(curr - prev);
                prev = curr;

            }

            average = average + avg_temp;

            count++;
        }

        avg_final = (average / count);
        //System.out.println("The decreasing rate is"+ avg_final);

        return avg_final;

        //return 1;

    }



}
