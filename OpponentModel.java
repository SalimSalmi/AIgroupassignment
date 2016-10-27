package ai2016;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.utility.AbstractUtilitySpace;

import java.util.List;

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

        System.out.println("OpponentModel");

        List<Issue> issues = utilSpace.getDomain().getIssues();

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
