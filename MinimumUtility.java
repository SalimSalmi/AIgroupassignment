package ai2016;

/**
 * Created by archah on 26/10/2016.
 */
public class MinimumUtility {

    private float minimum_start;
    private float minimum_end;
    private float curve;

    private double utility;

    public MinimumUtility(float minimum_start, float minimum_end, float curve){
        this.minimum_start = minimum_start;
        this.minimum_end = minimum_end;
        this.curve = curve;

        set(0, 0);
    }

    public void set(double time, double concession) {

        double utilityModeling = (1 - 2*Math.pow(time, 2));

        double utilityConceding = minimum_end + (1 - Math.pow(time, curve))*(minimum_start-minimum_end);

        utilityConceding += (1 - Math.pow(time, 150))*(concession);

        if (utilityConceding > 1) {
            utilityConceding = 1;
        }

        if(utilityModeling > utilityConceding) {
            System.out.println("Modeling utility: " + utilityModeling);
            utility = utilityModeling;
        } else {
            System.out.println("Conceding utility: " + utilityConceding + " with concession rate:" + concession);
            utility = utilityConceding;
        }
    }

    public double get(){
        return utility;
    }

    public double get(double time) {
        return minimum_end + (1 - Math.pow(time, curve))*(minimum_start-minimum_end);
    }
}
