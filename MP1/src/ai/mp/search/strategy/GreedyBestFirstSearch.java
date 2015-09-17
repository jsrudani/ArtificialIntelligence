package ai.mp.search.strategy;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * It is used to implement Greedy Best First Search strategy. 
 * It used Manhattan distance as a heuristic function.
 * 
 * @author rudani2
 *
 */
public class GreedyBestFirstSearch extends SearchOperation {

    private static boolean isGoalReached = false;

    private final int [][] inputMaze;
    private final char[][] solutionMaze;
    private long nodesExpanded = 0L;
    private long stepCost = 0L;
    private long solutionCost = 0L;

    GreedyBestFirstSearch(int [][] inputMaze, char[][] solutionMaze) {
        this.inputMaze = inputMaze;
        this.solutionMaze = solutionMaze;
    }

    @Override
    public void findPath() {
        /**
         * It holds all the available positions which are yet to be expanded.
         * It is sorted based on cost.
         */
        PriorityQueue<Position> openPosition = new PriorityQueue<Position>(MazeConstant.QUEUE_INITIAL_CAPACITY, new PositionComparator());
        // Add the start position into open position
        openPosition.add(Preprocessing.getStartPosition());
        findPathUsingGreedy(openPosition);
    }

    /**
     * It is used to find the path from start position to goal state using Greedy based approach.
     * It uses manhattan as heuristic function.
     * 
     * @param openPosition
     */
    private void findPathUsingGreedy(PriorityQueue<Position> openPosition) {
        Position currentPosition = null;
        // Traverse till openPosition is not empty and get the position with lowest heuristic value.
        while (!openPosition.isEmpty()) {
            currentPosition =  openPosition.poll();
            if (inputMaze[currentPosition.getX()][currentPosition.getY()] == MazeConstant.GOAL_POSITION_MARKER) {
                isGoalReached = true;
                break;
            } else {
                // Mark as visited
                inputMaze[currentPosition.getX()][currentPosition.getY()] = MazeConstant.VISITED;
                // Increment the nodes expanded
                nodesExpanded += 1;
                // Get the successor node
                getSuccessorNode(currentPosition, openPosition);
            }
        }
        // Check if solution exist
        if (isGoalReached) {
            this.stepCost = drawSolutionPath(this.getSolutionMaze(), currentPosition).getStepCost();
        }
    }

    /**
     * It is used to find successor for given node. It excludes if it is wall or already visited node.
     * If the child is valid, its heuristic value is calculated and added to openPosition.
     * 
     * @param node
     * @return Collection
     */
    private void getSuccessorNode(Position currentPosition, PriorityQueue<Position> openPosition) {
        Position child = null;
        // Get the upper node
        child = new Position((currentPosition.getX()-1), currentPosition.getY(), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            child.setCost( getHeuristicValue(Preprocessing.getGoalPosition(), child) );
            openPosition.add(child);
        }
        // Get the lower node
        child = new Position((currentPosition.getX()+1), currentPosition.getY(), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            child.setCost( getHeuristicValue(Preprocessing.getGoalPosition(), child) );
            openPosition.add(child);
        }
        // Get the left node
        child = new Position(currentPosition.getX(), (currentPosition.getY()-1), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            child.setCost( getHeuristicValue(Preprocessing.getGoalPosition(), child) );
            openPosition.add(child);
        }
        // Get the right node
        child = new Position(currentPosition.getX(), (currentPosition.getY()+1), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            child.setCost( getHeuristicValue(Preprocessing.getGoalPosition(), child) );
            openPosition.add(child);
        }
    }

    /**
     * It is used to calculate heuristic value based on Manhattan distance between goal state and current state.
     * Manhattan distance formula is |x1 - x2| + |y1 - y2|
     * 
     * @param goalState
     * @param currentPosition
     * @return long
     */
    private long getHeuristicValue(Position goalState, Position currentPosition) {
        return ( ( Math.abs(goalState.getX() - currentPosition.getX()) ) + ( Math.abs(goalState.getY() - currentPosition.getY()) ) );
    }

    @Override
    public char[][] getSolutionMaze() {
        return this.solutionMaze;
    }

    @Override
    public long getStepCost() {
        return this.stepCost;
    }

    @Override
    public long getNodesExpanded() {
        return this.nodesExpanded;
    }

    @Override
    public String getSearchStrategyName() {
        return "Greedy Best First Search";
    }

    @Override
    public long getSolutionCost() {
        return this.solutionCost;
    }

}

/**
 * It is used to compare two position based on heuristic function.
 *
 */
class PositionComparator implements Comparator<Position> {

    @Override
    public int compare(Position o1, Position o2) {
        return (int) (o1.getCost() - o2.getCost());
    }
}
