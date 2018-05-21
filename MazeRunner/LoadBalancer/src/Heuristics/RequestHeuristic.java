package Heuristics;

public class RequestHeuristic implements IHeuristic {

    private double MAXIMUM_DIFFICULTY = 0.9*2571786718L+0.1*13497394;
    private int SCALE_SIZE = 20;
    @Override
    public double getWorkload(long bbls){
        return SCALE_SIZE * (bbls) / MAXIMUM_DIFFICULTY;
    }

}
