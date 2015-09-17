package ai.mp.search.strategy;

import java.util.Stack;

/**
 * It is used to implement Depth-First-Search strategy.
 * DFS works like LIFO (Last In First Out).
 * The node which is visited first is expanded last.
 * 
 * @author rudani2
 *
 */
public class DFS extends SearchOperation {

    private static boolean isGoalReached = false;

    private final int [][] inputMaze;
    private final char[][] solutionMaze;
    private long nodesExpanded = 0L;
    private long stepCost = 0L;
    private long solutionCost = 0L;

    DFS(int [][] inputMaze, char[][] solutionMaze) {
        this.inputMaze = inputMaze;
        this.solutionMaze = solutionMaze;
    }

    @Override
    public void findPath() {
        Stack<Position> successor = new Stack<Position>();
        // Initialize the frontier queue with start position
        successor.push(Preprocessing.getStartPosition());
        // Recursively call find path from start position
        findPathUsingDFS(successor);
    }

    private void findPathUsingDFS(Stack<Position> successor) {
        Position childNode = null;
        while (!successor.isEmpty()) {
            childNode = successor.pop();
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
    private void getSuccessorNode(Position node, Stack<Position> successor) {
        Position child = null;
        // Get the upper node
        child = new Position((node.getX()-1), node.getY(), node, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            successor.push(child);
        }
        // Get the lower node
        child = new Position((node.getX()+1), node.getY(), node, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            successor.push(child);
        }
        // Get the left node
        child = new Position(node.getX(), (node.getY()-1), node, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            successor.push(child);
        }
        // Get the right node
        child = new Position(node.getX(), (node.getY()+1), node, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            successor.push(child);
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
        return "DFS (Depth-First-Search)";
    }

    @Override
    public long getSolutionCost() {
        return this.solutionCost;
    }
}
