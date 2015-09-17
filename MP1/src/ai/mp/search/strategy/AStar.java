package ai.mp.search.strategy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * It is used to implement A* Search strategy. 
 * It used Manhattan distance as a heuristic function.
 * 
 * @author rudani2
 *
 */
public class AStar extends SearchOperation {

    private static boolean isGoalReached = false;

    private final int [][] inputMaze;
    private final char[][] solutionMaze;
    private long nodesExpanded = 0L;
    private long stepCost = 0L;
    private long solutionCost = 0L;

    AStar(int [][] inputMaze, char[][] solutionMaze) {
        this.inputMaze = inputMaze;
        this.solutionMaze = solutionMaze;
    }

    @Override
    public void findPath() {
        /**
         * It holds all the available positions which are yet to be expanded.
         * It is sorted based on cost.
         */
        TreeMap<Position, Long> openPosition = new TreeMap<Position,Long>(new CostComparator());
        /**
         * It holds all the expanded positions.
         */
        Map<Position,Long> expandedPosition = new HashMap<Position,Long>();

        //Initialize the cost of start node
        Preprocessing.getStartPosition().setCost( (0 + getHeuristicValue(Preprocessing.getGoalPosition(), Preprocessing.getStartPosition()) ) );

        // Make default direction and facing of start node to right
        Position rightFacing = new Position(Preprocessing.getStartPosition().getX()
                , (Preprocessing.getStartPosition().getY()+1), Preprocessing.getStartPosition()
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        Preprocessing.getStartPosition().setFacing(rightFacing);

        // Add the start position into open position
        openPosition.put(Preprocessing.getStartPosition(),Preprocessing.getStartPosition().getApproachableCost());

        // Check for additional options
        if (Preprocessing.isPathFind()) {
            findPathUsingAStar(openPosition, expandedPosition);
        } else if (Preprocessing.isPenalty()) {
            findPathUsingPenalty(openPosition, expandedPosition, MazeConstant.TURN_COST, MazeConstant.FORWARD_COST);
        }
    }

    /**
     * It is used to find the path from start position to goal state using A * approach.
     * It uses manhattan as heuristic function.
     * 
     * @param openPosition
     */
    private void findPathUsingAStar(TreeMap<Position, Long> openPosition, Map<Position,Long> expandedPosition) {
        Position currentPosition = null;
        int [][] visited = new int [inputMaze.length][inputMaze[0].length];

        // Traverse till openPosition is not empty and get the position with lowest heuristic value.
        while (!openPosition.isEmpty()) {
            currentPosition = openPosition.pollFirstEntry().getKey();
            if (inputMaze[currentPosition.getX()][currentPosition.getY()] == MazeConstant.GOAL_POSITION_MARKER) {
                isGoalReached = true;
                break;
            } else {
                visited[currentPosition.getX()][currentPosition.getY()] = 1;

                // Mark as visited by inserting into expanded list
                expandedPosition.put(currentPosition,currentPosition.getApproachableCost());

                // Increment the nodes expanded
                nodesExpanded += 1;

                // Get the successor node
                getSuccessorNode(currentPosition, openPosition, expandedPosition, MazeConstant.DEFAULT_PENALTY);
            }
        }
        // Check if solution exist
        if (isGoalReached) {
            MazeMetrics metrics = drawSolutionPath(this.getSolutionMaze(), currentPosition);
            this.stepCost = metrics.getStepCost();
            this.solutionCost = metrics.getSolutionCost();
            getVisitedNode(visited);
        }
    }

    /**
     * It is used to find the path from start position to goal state using A * approach.
     * It uses manhattan as heuristic function and also consider the penalty
     * for forward direction and left/right turn
     * 
     * @param openPosition
     * @param expandedPosition
     */
    private void findPathUsingPenalty(TreeMap<Position, Long> openPosition, Map<Position,Long> expandedPosition
                                    , int turnCost, int forwardCost) {
        Position currentPosition = null;
        int [][] visited = new int [inputMaze.length][inputMaze[0].length];
        // Traverse till openPosition is not empty and get the position with lowest heuristic value.
        while (!openPosition.isEmpty()) {
            currentPosition = openPosition.pollFirstEntry().getKey();
            if (inputMaze[currentPosition.getX()][currentPosition.getY()] == MazeConstant.GOAL_POSITION_MARKER) {
                isGoalReached = true;
                break;
            } else {
                visited[currentPosition.getX()][currentPosition.getY()] = 1;

                // Calculate penalty
                long penalty = calculatePenalty(currentPosition, turnCost, forwardCost);

                // Mark as visited by inserting into expanded list
                expandedPosition.put(currentPosition,currentPosition.getApproachableCost());

                // Increment the nodes expanded
                nodesExpanded += 1;

                // Get the successor node
                getSuccessorNode(currentPosition, openPosition, expandedPosition, penalty);
            }
        }
        // Check if solution exist
        if (isGoalReached) {
            MazeMetrics metrics = drawSolutionPath(this.getSolutionMaze(), currentPosition);
            this.stepCost = metrics.getStepCost();
            this.solutionCost = metrics.getSolutionCost();
        }
    }

    /**
     * It is used to find successor for given node. It excludes if it is wall or already visited node.
     * If the child is valid, its heuristic value is calculated and added to openPosition.
     * 
     * @param node
     * @return Collection
     */
    private void getSuccessorNode(Position currentPosition, TreeMap<Position, Long> openPosition, Map<Position,Long> expandedPosition
                          , long penalty) {
        Position child = null;

        // Get the upper node
        child = new Position((currentPosition.getX()-1), currentPosition.getY(), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.UP_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            // Check if child is already in expanded/open position and if exist compare the overall cost
            if (!expandedPosition.containsKey(child)) {
                checkExistanceWithLowerCostAndUpdateMap(currentPosition, child
                        , openPosition, expandedPosition, penalty);
            }
        }

        // Get the lower node
        child = new Position((currentPosition.getX()+1), currentPosition.getY(), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.DOWN_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            // Check if child is already in expanded/open position and if exist compare the overall cost
            if (!expandedPosition.containsKey(child)) {
                checkExistanceWithLowerCostAndUpdateMap(currentPosition, child
                        , openPosition, expandedPosition, penalty);
            }
        }

        // Get the left node
        child = new Position(currentPosition.getX(), (currentPosition.getY()-1), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.LEFT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            // Check if child is already in expanded/open position and if exist compare the overall cost
            if (!expandedPosition.containsKey(child)) {
                checkExistanceWithLowerCostAndUpdateMap(currentPosition, child
                        , openPosition, expandedPosition, penalty);
            }
        }

        // Get the right node
        child = new Position(currentPosition.getX(), (currentPosition.getY()+1), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            // Check if child is already in expanded/open position and if exist compare the overall cost
            if (!expandedPosition.containsKey(child)) {
                checkExistanceWithLowerCostAndUpdateMap(currentPosition, child
                        , openPosition, expandedPosition, penalty);
            }
        }
    }

    /**
     * It is used to check if child node exist in expanded or open position list. If exist then update the Map accordingly.
     * 
     */
    private void checkExistanceWithLowerCostAndUpdateMap(Position parentNode, Position childNode
            , TreeMap<Position, Long> openPosition, Map<Position,Long> expandedPosition, long penalty) {

        // Add the penalty as current node approached cost. Since it will include penalty to reach current node
        long approachedCost = penalty + (parentNode.getApproachableCost() + 1);
        long heuristicCost = getHeuristicValue(Preprocessing.getGoalPosition(), childNode);
        if (!openPosition.containsKey(childNode) || approachedCost < openPosition.get(childNode)) {
            childNode.setParent(parentNode);
            childNode.setApproachableCost(approachedCost);
            childNode.setCost( (childNode.getApproachableCost() + heuristicCost) );
            if (!openPosition.containsKey(childNode)) {
                openPosition.put(childNode, childNode.getApproachableCost());
            }
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

    /**
     * It is used to calculate the penalty for given position. 
     * It checks with its parent facing and decide which direction to face.
     * 
     * @return long
     */
    private long calculatePenalty(Position currentPosition, int turnPenalty, int forwardPenalty) {

        long penalty = forwardPenalty;
        // Check for parent existence
        if (currentPosition.getParent() != null) {
            penalty = 0;
            Position parentFacing = currentPosition.getParent().getFacing();
            if (!parentFacing.equals(currentPosition)) {
                // If facing in wrong direction add the turn cost
                penalty += turnPenalty;
            }
            // If facing in same direction or incorrect direction add the forward cost
            penalty += forwardPenalty;
            Position facingPosition = null;

            // No need to check for boundary condition for new facing node as boundary is surrounded by wall and wall is considered as invalid 
            // position. If it is not a wall then check for boundary condition
            switch (currentPosition.getDirection()) {
                case 1: // UP
                    facingPosition = new Position((currentPosition.getX()-1), currentPosition.getY(), currentPosition
                            , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.UP_DIRECTION);
                    break;
                case 2: // DOWN
                    facingPosition = new Position((currentPosition.getX()+1), currentPosition.getY(), currentPosition
                            , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.DOWN_DIRECTION);
                    break;
                case 3: // LEFT
                    facingPosition = new Position(currentPosition.getX(), (currentPosition.getY()-1), currentPosition
                            , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.LEFT_DIRECTION);
                    break;
                case 4: // RIGHT
                    facingPosition = new Position(currentPosition.getX(), (currentPosition.getY()+1), currentPosition
                            , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
                    break;
            }
            currentPosition.setFacing(facingPosition);
            return penalty;
        }
        return penalty;
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
        return "A *";
    }

    private void getVisitedNode(int [][] array) {
        int row = array.length;
        int col = array[0].length;
        int count = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (array[i][j] == 1) {
                    count += 1;
                }
            }
        }
        //System.out.println("visited " + count);
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
class CostComparator implements Comparator<Position> {

    @Override
    public int compare(Position o1, Position o2) {
        return (o1.getCost() <= o2.getCost() ? -1 : 1);
    }
}
