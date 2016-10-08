package ai2016;

import negotiator.AgentID;
import negotiator.utility.*;
import negotiator.issue.*;
import java.util.List;

/**
 * Created by archah on 08/10/2016.
 */
public class OpponentModel {

    AgentID agentID;

    AbstractUtilitySpace utilSpace;

    public OpponentModel(AgentID agentID){
        this.agentID = agentID;
    }

    public void init(AbstractUtilitySpace utilSpace){
        System.out.println("OpponentModel");
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
}
