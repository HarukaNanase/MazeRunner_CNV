package Heuristics;

public class CubicHeuristic implements IHeuristic {

    @Override
    public double getWorkload(long bbls) {
        double x = bbls;
        return 3.67317*Math.pow(10, -28) *Math.pow(x,3) - 3.22925*Math.pow(10,-18)*Math.pow(x, 2) + 1.28621*Math.pow(10,-8)*x + 2.04972;
    }
}
