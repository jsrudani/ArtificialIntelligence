package ai.mp.search.strategy;

import java.util.LinkedList;
import java.util.Queue;

/**
 * It is used to implement Breath-First-Search strategy.
 * BFS works like FIFO (First In First Out).
 * The node which is visited first is expanded first.
 * 
 * @author rudani2
 *
 */
public class BFS extends SearchOperation {

    private static boolean isGoalReached = false;

    private final int [][] inputMaze;
    private final char[][] solutionMaze;
    private long nodesExpanded = 0L;
    private long stepCost = 0L;
    private long solutionCost = 0L;

    BFS(int [][] inputMaze, char[][] solutionMaze) {
        this.inputMaze = inputMaze;
        this.solutionMaze = solutionMaze;
    }

    @Override
    public void findPath() {
        Queue<Position> successor = new LinkedList<Position>();
        // Initialize the frontier queue with start position
        successor.add(Preprocessing.getStartPosition());
        // Recursively call find path from start position
        findPathUsingBFS(successor);
    }

    private void findPathUsingBFS(Queue<Position> successor) {
        Position childNode = null;
        long level = 1L;
        while (!successor.isEmpty()) {
            childNode = successor.poll();
            level -= 1;
            if (inputMaze[childNode.getX()][childNode.getY()] == MazeConstant.GOAL_POSITION_MARKER) {
                isGoalReached = true;
                break;
            } else {
                // Mark the node as visited
                inputMaze[childNode.getX()][childNode.getY()] = MazeConstant.VISITED;
                // Increment the node expanded count
                nodesExpanded += 1;
                // Get the successor node
                getSuccessorNode(childNode, successor);
            }
            // Check if level == 0
            if (level == 0) {
                stepCost += 1;
                level = successor.size();
            }
        }
        // Check if solution exist
        if (isGoalReached) {
            this.stepCost = drawSolutionPath(this.getSolutionMaze(), childNode).getStepCost();
        }
    }

    /**
     * It is used to find successor for given node. 
     * It excludes if it is wall or already visited node
     * 
     * @param node
     * @return Collection
     */
    private void getSuccessorNode(Position node, Queue<Position> successor) {
        Position child = null;
        // Get the upper node
        child = new Position((node.getX()-1), node.getY(), node, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            successor.add(child);
        }
        // Get the lower node
        child = new Position((node.getX()+1), node.getY(), node, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            successor.add(child);
        }
        // Get the left node
        child = new Position(node.getX(), (node.getY()-1), node, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            successor.add(child);
        }
        // Get the right node
        child = new Position(node.getX(), (node.getY()+1), node, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            successor.add(child);
        }
    }

    @Override
    public long getNodesExpanded() {
        return nodesExpanded;
    }

    @Override
    public char[][] getSolutionMaze() {
        return solutionMaze;
    }

    @Override
    public long getStepCost() {
        return stepCost;
    }

    @Override
    public String getSearchStrategyName() {
        return "BFS (Breadth-First-Search)";
    }

    @Override
    public long getSolutionCost() {
        return this.solutionCost;
    }
}
