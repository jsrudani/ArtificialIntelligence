package ai.mp.search.strategy;

/**
 * It holds the metrics collected during traversing the maze using search strategy
 * 
 * @author rudani2
 *
 */
public class MazeMetrics {

    private final long stepCost;
    private final long solutionCost;

    MazeMetrics(long stepCost, long solutionCost) {
        this.stepCost = stepCost;
        this.solutionCost = solutionCost;
    }

    @Override
    public String toString() {
        return "MazeMetrics [stepCost=" + stepCost + ", solutionCost="
                + solutionCost + "]";
    }

    public long getStepCost() {
        return stepCost;
    }

    public long getSolutionCost() {
        return solutionCost;
    }

}
