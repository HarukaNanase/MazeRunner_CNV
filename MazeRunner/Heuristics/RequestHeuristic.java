package Heuristics;

public class RequestHeuristic implements IHeuristic {

    private double MAXIMUM_DIFFICULTY = 0.9*2571786718L+0.1*13497394;
    private int SCALE_SIZE = 20;
    @Override
    public double getWorkload(long branches, long bbls){
        return SCALE_SIZE * (0.9*branches + 0.1*bbls) / MAXIMUM_DIFFICULTY;
    }

    public double getDistance(int x0, int y0, int x1, int y1){
        return Math.sqrt((x0-x1)*(x0-x1) + (y0-y1)*(y0-y1));
    }

    public double getVelocityImpact(int velocity){
        return velocity;
    }
}
