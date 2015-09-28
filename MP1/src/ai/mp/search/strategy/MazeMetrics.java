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
    private final long penalty;

    MazeMetrics(long stepCost, long solutionCost, long penalty) {
        this.stepCost = stepCost;
        this.solutionCost = solutionCost;
        this.penalty = penalty;
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

    public long getPenalty() {
        return penalty;
    }

}
