package ai2016;

import negotiator.Bid;
import negotiator.utility.AbstractUtilitySpace;

/**
 * Created by archah on 19/10/2016.
 */
public class AcceptanceStrategy {

    private AbstractUtilitySpace utilSpace;
    private MinimumUtility minimumUtility;
    private OpponentList opponents;


    public AcceptanceStrategy(AbstractUtilitySpace utilSpace, MinimumUtility minimumUtility, OpponentList opponents) {
        this.utilSpace = utilSpace;
        this.minimumUtility = minimumUtility;
        this.opponents = opponents;
    }

    public boolean accept(Bid bid){

        if ( utilSpace.getUtility(bid) > minimumUtility.get() ){
            System.out.println("The util is : " + utilSpace.getUtility(bid) + ", the min util is : " + minimumUtility.get());
        }

        return utilSpace.getUtility(bid) > minimumUtility.get();


    }
}
