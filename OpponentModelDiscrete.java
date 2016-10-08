package ai2016;

import negotiator.AgentID;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;

/**
 * Created by archah on 08/10/2016.
 */
public class OpponentModelDiscrete extends OpponentModel{

    private AdditiveUtilitySpace utilSpace;

    public OpponentModelDiscrete(AgentID agentID){
        super(agentID);
    }

    @Override
    public void init(AbstractUtilitySpace utilSpace) {
        this.utilSpace = (AdditiveUtilitySpace) utilSpace;
        System.out.println("OpponentModelDiscrete");

    }
}
