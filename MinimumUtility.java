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

        System.out.println("Concession rate: " + concession);

        utility = minimum_end + (1 - Math.pow(time, curve))*(minimum_start-minimum_end);

        utility += (1 - Math.pow(time, 150))*(concession);

        if (utility > 1) {
            utility = 1;
        }

    }

    public double get(){
        return utility;
    }

    public double get(double time) {
        return minimum_end + (1 - Math.pow(time, curve))*(minimum_start-minimum_end);
    }
}
