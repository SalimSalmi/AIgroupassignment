package ai2016;

import negotiator.AgentID;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

import java.util.ArrayList;
import java.util.HashMap;

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

        for(int j=0; j < this.size(); j++){
            OpponentModelDiscrete opponent = (OpponentModelDiscrete) this.get(j);

            alphas.put(opponent, opponent.getDropRate());
        }

        return alphas;
    }

    public OpponentModel getAverageOpponentModel() {

        OpponentModelDiscrete opponentAvg = new OpponentModelDiscrete(null);

        HashMap<OpponentModelDiscrete, Double> alpha = getAlphas();

        for(int i=1;i<= opponentAvg.getUtilSpace().getNrOfEvaluators();i++){

            EvaluatorDiscrete evaluator = (EvaluatorDiscrete) opponentAvg.getUtilSpace().getEvaluator(i);

            double weightAvg = 0;


            for(int j=0; j < this.size(); j++) {

                OpponentModelDiscrete oppNew = (OpponentModelDiscrete) this.get(j);

                EvaluatorDiscrete evaluatorIndex = (EvaluatorDiscrete) oppNew.getUtilSpace().getEvaluator(i);

                System.out.println("The opponent weight is :" +evaluatorIndex.getWeight());
                System.out.println("The calculated weightAvg is"+weightAvg);

                weightAvg = weightAvg + alpha.get(oppNew) * evaluatorIndex.getWeight();



                evaluator.setWeight(weightAvg);

                for(ValueDiscrete value : evaluator.getValues()) {

                    double valueAvg = 0;

                    valueAvg = valueAvg + alpha.get(oppNew) * evaluatorIndex.getValue(value);

                    try {
                        evaluator.setEvaluationDouble(value, valueAvg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        }


        return opponentAvg;
    }
}

