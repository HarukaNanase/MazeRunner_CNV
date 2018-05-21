package Heuristics;

public class LogarithmicHeuristic implements IHeuristic {

    public double getWorkload(long bbls){
        double x = bbls;
        return 2.25499*Math.log(3.11316*Math.pow(10,-7)*x);
    }
}
