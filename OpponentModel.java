package ai2016;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by archah on 08/10/2016.
 */
public class OpponentModel {

    private AgentID agentID;

    private AbstractUtilitySpace utilSpace;

    protected ArrayList<Bid> bids = new ArrayList<>();
    protected boolean modeling = true;

    public OpponentModel(AgentID agentID){
        this.agentID = agentID;
    }

    public void init(AbstractUtilitySpace utilSpace){
        this.utilSpace = utilSpace;

    }

    public void pushBid(Bid bid) {
        bids.add(bid);
    }

    public void stopModeling(){
        this.modeling = false;
    }

    public AgentID getAgentID() {
        return agentID;
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof OpponentModel) {
            OpponentModel opponent = (OpponentModel) other;

            return agentID.equals(opponent.getAgentID());
        }

        return false;
    }

    public AbstractUtilitySpace getUtilSpace(){

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
