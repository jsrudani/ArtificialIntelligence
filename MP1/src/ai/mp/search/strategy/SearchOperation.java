package ai.mp.search.strategy;

/**
 * It specify search operation contract. Every search strategy has to implement
 * methods declared in this abstract.
 * 
 * @author jsrudani
 *
 */
abstract class SearchOperation {

    /**
     * It is used to find the path in a maze
     */
    public abstract void findPath();

    /**
     * It is used to return the solution maze
     * @return Solution Maze
     */
    public abstract char[][] getSolutionMaze();

    /**
     * It is used to get the step count of the algorithm
     * @return long
     */
    public abstract long getStepCost();

    /**
     * It is used to get the total path cost of the algorithm
     * @return long
     */
    public abstract long getSolutionCost();

    /**
     * It is used to get the number of node expanded
     * @return
     */
    public abstract long getNodesExpanded();

    /**
     * It is used to get the name of search strategy
     */
    public abstract String getSearchStrategyName();

    /**
     * It is used to check if child is valid or not. It checks whether child position
     * is a Wall or already visited node. Wall is represented as -1, visited node as 4
     * 
     * @param inputMaze
     * @param child
     * @return boolean
     */
    public boolean isChildValid(int[][] inputMaze, Position child) {
        // Check if it is Wall or Visited
        if (inputMaze[child.getX()][child.getY()] != MazeConstant.WALL_MARKER
                && inputMaze[child.getX()][child.getY()] != MazeConstant.VISITED) {
            return true;
        }
        return false;
    }

    /**
     * It is used to draw solution path from goal state to start state.
     * 
     * @param childNode
     * @return long
     */
    public MazeMetrics drawSolutionPath(char[][] solutionMaze, Position childNode) {
        long stepCost = 0L;
        long solutionCost = 0L;
        while (childNode != null && childNode.getParent() != null) {
            if (solutionMaze[childNode.getX()][childNode.getY()] < 48) {
                solutionMaze[childNode.getX()][childNode.getY()] = '.';
            }
            solutionCost += childNode.getCost();
            //System.out.println("child node " + childNode + "approach cost " + childNode.getApproachableCost());
            childNode = childNode.getParent();
            stepCost += 1;
        }
        return new MazeMetrics(stepCost, solutionCost);
    }
}
