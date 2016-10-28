package ai2016;

/**
 * Created by archah on 26/10/2016.
 */
public class MinimumUtility {

    private float minimum_start;
    private float minimum_end;
    private float curve;

    private double utility;
    private double minimum_dis = 0;


    /**
     * Creates a utility curve. The curve is based on 3 separate curves from which
     * the maximum is chosen. Each curve represents a scenario and changes based
     * on the actions of opponents.
     *
     * @param minimum_start The value at which to start bidding
     * @param minimum_end The value the utility curve should approach
     * @param curve The value of how fast the curve approaches minimum_end, higher value means fast approach
     */
    public MinimumUtility(float minimum_start, float minimum_end, float curve){
        this.minimum_start = minimum_start;
        this.minimum_end = minimum_end;
        this.curve = curve;

        set(0);
    }

    /**
     * Sets the maximum utility value for the current time based on the current values of the curves.
     *
     * @param time The current time
     */
    public void set(double time) {

        // Curve to allow for correct naive bids
        double utilityModeling = (1 - 2*Math.pow(time, 2));

        // Curve that concedes towards opponent
        double utilityConceding = minimum_end + (1 - Math.pow(time, curve))*(minimum_start-minimum_end);

        // Curve that prevents other agents to exploit our conceding.
        double utilityDistance = (1 - Math.pow(time, 120)) * (minimum_end + (1-minimum_end) * minimum_dis);

        // select the maximum value
        if(utilityModeling > utilityConceding && utilityModeling > utilityDistance)
            utility = utilityModeling;
        else if (utilityConceding > utilityDistance)
            utility = utilityConceding;
        else
            utility = utilityDistance;
    }

    public void goal(double goal) {
        minimum_end = new Float(goal);
    }

    public void minDistance(double distance){
        minimum_dis = distance;
    }

    public double get(){
        return utility;
    }

    public double get(double time) {
        return minimum_end + (1 - Math.pow(time, curve))*(minimum_start-minimum_end);
    }
}
