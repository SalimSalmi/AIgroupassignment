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

    public MinimumUtility(float minimum_start, float minimum_end, float curve){
        this.minimum_start = minimum_start;
        this.minimum_end = minimum_end;
        this.curve = curve;

        set(0);
    }

    public void set(double time) {

        double utilityModeling = (1 - 2*Math.pow(time, 2));

        double utilityConceding = minimum_end + (1 - Math.pow(time, curve))*(minimum_start-minimum_end);

        double utilityDistance = (1 - Math.pow(time, 120)) * (minimum_end + (1-minimum_end) * minimum_dis);

        if(utilityModeling > utilityConceding && utilityModeling > utilityDistance) {
//            System.out.println("Modeling utility: " + utilityModeling);
            utility = utilityModeling;
        } else if (utilityConceding > utilityDistance){
//            System.out.println("Conceding utility: " + utilityConceding );
            utility = utilityConceding;
        } else {
//            System.out.println("Conceding utility: " + utilityConceding );
            utility = utilityDistance;
        }
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
