package ai2016;

import negotiator.AgentID;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by archah on 24/10/2016.
 */
public class OpponentList extends ArrayList<OpponentModel> {

    public OpponentModel getOpponent(AgentID sender, AbstractUtilitySpace utilSpace) {

        OpponentModel opponent;

        if (utilSpace instanceof AdditiveUtilitySpace)
            opponent = new OpponentModelDiscrete(sender);
        else
            opponent = new OpponentModel(sender);

        if(this.contains(opponent)) {
            opponent = this.get(this.indexOf(opponent));
        } else {
            this.add(opponent);
            opponent.init((AbstractUtilitySpace) utilSpace.copy());
        }

        return opponent;

    }

    private HashMap<OpponentModelDiscrete, Double> getAlphas(){

        HashMap<OpponentModelDiscrete, Double> alphas = new HashMap<>();

        double dropsum = 0;

        //System.out.println("size of opponentlist " + this.size());


        for(int j = 0; j < this.size(); j++){

            OpponentModelDiscrete opponent = (OpponentModelDiscrete) this.get(j);
            //double numer = entry.getValue();

            System.out.println("The decreasing rate for opponent" + j + opponent.getDropRate());

            alphas.put(opponent, opponent.getDropRate());

            dropsum += alphas.get(opponent);

        }

        for(Map.Entry<OpponentModelDiscrete, Double> entry : alphas.entrySet()){

            OpponentModelDiscrete opponent = (OpponentModelDiscrete) entry.getKey();

            double number = entry.getValue();

            double inverse_average_rate = (1 - number/dropsum);

            alphas.put(opponent, inverse_average_rate);

            //System.out.println("The normalized decreasing rate for"+ inverse_average_rate);

        }


        return alphas;
    }

    public OpponentModel getAverageOpponentModel(AbstractUtilitySpace utilSpace) {

        OpponentModelDiscrete opponentAvg = new OpponentModelDiscrete(null);

        opponentAvg.init(utilSpace);

        HashMap<OpponentModelDiscrete, Double> alpha = getAlphas();


        for(int i=1;i<= opponentAvg.getUtilSpace().getNrOfEvaluators();i++){

            EvaluatorDiscrete evaluator = (EvaluatorDiscrete) opponentAvg.getUtilSpace().getEvaluator(i);


            for(ValueDiscrete value : evaluator.getValues()) {

                double valueAvg = 0;
                double weightAvg = 0;

                int k =1;

                for(Map.Entry<OpponentModelDiscrete, Double> entry : alpha.entrySet()){


                    OpponentModelDiscrete oppNew = (OpponentModelDiscrete) entry.getKey();

                    EvaluatorDiscrete evaluatorIndex = (EvaluatorDiscrete) oppNew.getUtilSpace().getEvaluator(i);

                    System.out.println("The weight for opponent " + k  + " some evaluator "+ i +"is"+ evaluatorIndex.getWeight());

                    valueAvg = valueAvg + alpha.get(oppNew) * evaluatorIndex.getValue(value);

                    System.out.println("The rate for opponent" + k + "is" + alpha.get(oppNew));

                    weightAvg = weightAvg + alpha.get(oppNew) * evaluatorIndex.getWeight();//Put the alpha value here

                    k=k+1;

                }

                try {
                    //System.out.print("The value average for evaluator "+ i +"is "+valueAvg);
                    evaluator.setEvaluationDouble(value, valueAvg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("The average weight for agent for some evaluator "+ i +"is"+weightAvg);

                evaluator.setWeight(weightAvg);


            }


        }

        return opponentAvg;
    }
}

