package ai2016;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

/**
 * Created by archah on 08/10/2016.
 */
public class OpponentModel {

    private AgentID agentID;

    private AbstractUtilitySpace utilSpace;

    public OpponentModel(AgentID agentID){
        this.agentID = agentID;
    }

    public void init(AbstractUtilitySpace utilSpace){
        this.utilSpace = utilSpace;

    }

    public void pushBid(Bid bid, double ownUtil) {}

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
}
