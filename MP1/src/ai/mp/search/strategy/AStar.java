package ai.mp.search.strategy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
        // Debug
        char [][] debugMatrix = this.solutionMaze.clone();
        /**
         * It holds all the available positions which are yet to be expanded.
         * It is sorted based on cost.
         */
        TreeMap<Position, Long> openPosition = new TreeMap<Position,Long>(new CostComparator());
        /**
         * It holds all the expanded positions.
         */
        Map<Position,Long> expandedPosition = new HashMap<Position,Long>();

        //Initialize the cost of start node if there is only one goal state
        if (!Preprocessing.isMultipleGoal()) {
            Preprocessing.getStartPosition().setCost( (0 + getHeuristicValue(Preprocessing.getGoalPosition(), Preprocessing.getStartPosition()) ) );
        }

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
        } else if (Preprocessing.isGhost()) {
            checkAndInitializeGhostDirection(Preprocessing.getGhostPosition());
            findPathAvoidGhost(openPosition, expandedPosition, Preprocessing.getGhostPosition(), debugMatrix);
        } else if (Preprocessing.isMultipleGoal()) {
            findPathThroughMultipleGoals(Preprocessing.getStartPosition(), Preprocessing.getGoalSet());
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
     * It is used to find the path from start to goal state with Ghost in the maze.
     * 
     * @param openPosition
     * @param expandedPosition
     * @param ghostPosition
     */
    private void findPathAvoidGhost(TreeMap<Position, Long> openPosition, Map<Position,Long> expandedPosition
                                , Position ghostPosition, char [][] debugMatrix) {
        Position currentPosition = null;
        int [][] visited = new int [inputMaze.length][inputMaze[0].length];
        // Traverse till openPosition is not empty and get the position with lowest heuristic value.
        while (!openPosition.isEmpty()) {
            ghostPosition = Preprocessing.getGhostPosition();
            currentPosition = openPosition.pollFirstEntry().getKey();
            displayCharArray(debugMatrix, currentPosition, ghostPosition);
            if (inputMaze[currentPosition.getX()][currentPosition.getY()] == MazeConstant.GOAL_POSITION_MARKER) {
                isGoalReached = true;
                break;
            } else {
                visited[currentPosition.getX()][currentPosition.getY()] = 1;

                // Check if there is ghost or not
                if (checkForGhostAndMoveGhost(currentPosition, ghostPosition)
                        || currentPosition.equals(Preprocessing.getGhostPosition())) {
                    //System.out.println("Pacman " + currentPosition + " Direction " + currentPosition.getDirection());
                    //System.out.println("Ghost " + Preprocessing.getGhostPosition() + " Direction " + Preprocessing.getGhostPosition().getDirection());
                    continue;
                }

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
        }
    }

    /**
     * It is used to find cheapest path which will cover all dots in a maze.
     * 
     * @param goalSet
     */
    private void findPathThroughMultipleGoals(Position startPosition, Set<Position> goalSet) {
        long bestPathCost = Long.MAX_VALUE;
        // It holds all the explored state
        Set<Position> exploredSet = new HashSet<Position>();
        // Iterate through all goals and build a Minimum spanning tree for each goal position
        Iterator<Position> goalIterator = goalSet.iterator();
        while (goalIterator.hasNext()) {
            Position goal = goalIterator.next();
            // Empty the explored set for new goal position
            exploredSet.clear();
            // Set the edge cost as Infinity
            goal.setEdgeCost(Long.MAX_VALUE);
            // Build Minimum Spanning Tree
            Position bestNeighbor = MST(goal, goalSet, exploredSet, bestPathCost);
            long minimumCost = bestNeighbor.getEdgeCost() + getHeuristicValue(goal, startPosition);
            // Compare the cost with previous minimum and update if less than previous
            if (bestPathCost > minimumCost) {
                bestPathCost = minimumCost;
                startPosition.setNextNeighborNode(bestNeighbor);
            }
        }
        // Debug print the path
        while (startPosition != null) {
            System.out.println(startPosition);
            startPosition = startPosition.getNextNeighborNode();
        }
        // Now heuristic has given best possible path. Run the A* to build path along it
    }

    /**
     * It is used to build a Minimum Spanning Tree for given node.
     * It builds cheapest path from given node to all other nodes. The cheapest path has less cost compared to other.
     * 
     * @param goal
     * @return Position
     */
    private Position MST(Position node, final Set<Position> goalSet, Set<Position> exploredSet, long bestPathCost) {
        Iterator<Position> goalIterator = goalSet.iterator();
        Position bestNextGoal = null;
        // Get the total count of goal set
        long goalCount = goalSet.size();
        // Add it to Explored set
        exploredSet.add(node);
        // Decrement the goal count
        long edgeCost = 0L;
        while (goalIterator.hasNext()) {
            // Clone the next goal
            Position nextGoal = Position.clone(goalIterator.next());

            // If next goal is not current goal and not already explored
            if (!node.equals(nextGoal) && !exploredSet.contains(nextGoal)) {
                // Prune the growing tree if distance between current node to next goal exceeds minimum cost
                if (getHeuristicValue(nextGoal, node) < bestPathCost) {
                    goalCount -= 1;
                    // Recursively call MST for next goal
                    bestNextGoal = MST(nextGoal,goalSet, exploredSet, bestPathCost);
                    long bestNextGoalCost = bestNextGoal.getEdgeCost() != Long.MAX_VALUE ? bestNextGoal.getEdgeCost() : 0;
                    edgeCost = ( bestNextGoalCost + getHeuristicValue(bestNextGoal, node) );
                    if (edgeCost != 0 && edgeCost < node.getEdgeCost()) {
                        node.setEdgeCost(edgeCost);
                        node.setNextNeighborNode(bestNextGoal);
                    }
                } else {
                    // Since the cost is much higher than best part cost. so no need to grow further
                    continue;
                }
            } else {
                goalCount -= 1;
                // If node already explored then continue to other node
                continue;
            }
        }
        // Check if all nodes are explored
        if (goalCount == 0 && edgeCost != 0 && edgeCost < node.getEdgeCost()) {
            node.setEdgeCost(edgeCost);
            node.setNextNeighborNode(bestNextGoal);
        }
        // Remove from explored set
        exploredSet.remove(node);
        // All paths are explored set the best edge cost and best next neighbor
        return node;
    }

    /**
     * It is used to move the ghost in appropriate direction. If there is a wall then ghost direction is changed and
     * move in opposite direction. It is also used to indicate whether the new position of ghost is danger for
     * Pacman.
     * 
     * @param ghost
     * @return boolean
     */
    private boolean checkForGhostAndMoveGhost(Position pacman, Position ghost) {
        boolean dangerPosition = false;
        // Check whether next move of ghost is valid or not and change direction accordingly
        checkAndInitializeGhostDirection(ghost);
        ghost = Preprocessing.getGhostPosition();
        if (pacman.equals(ghost) && pacman.getDirection() != ghost.getDirection()) {
            dangerPosition = true;
        }
        // Move the ghost even if pacman knows that its not a valid position
        moveGhost(ghost);
        return dangerPosition;
    }

    /**
     * It is used to move the ghost to next position in appropriate direction
     * 
     * @param ghost
     */
    private void moveGhost(Position ghost) {
        Position newGhostPosition = null;
        switch (ghost.getDirection()) {
            case MazeConstant.LEFT_DIRECTION :
                newGhostPosition = new Position(ghost.getX(), (ghost.getY() - 1), null
                            , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.LEFT_DIRECTION);
                break;
            case MazeConstant.RIGHT_DIRECTION :
                newGhostPosition = new Position(ghost.getX(), (ghost.getY() + 1), null
                            , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
                break;
        }
        if (newGhostPosition != null) {
            Preprocessing.setGhostPosition(newGhostPosition);
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
        /*long x = (goalState.getX() - currentPosition.getX());
        long y = (goalState.getY() - currentPosition.getY());
        System.out.println("double value " + Math.ceil( ( Math.sqrt( (double)(Math.pow(x, 2) + Math.pow(y, 2)) ) ) ));
        System.out.println("Long value " + (long) ( Math.sqrt( (double)(Math.pow(x, 2) + Math.pow(y, 2)) ) ));
        return (long) Math.ceil( ( Math.sqrt( (double)(Math.pow(x, 2) + Math.pow(y, 2)) ) ) );*/
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

    /**
     * It is used to ghost direction. If there is a wall left/right immediately to current position then
     * direction is changed to opposite direction.
     *  
     * @param ghost
     */
    private void checkAndInitializeGhostDirection(Position ghost) {
        Position newGhostPosition = ghost;
        boolean isGhostPositionUpdated = false;
        switch (ghost.getDirection()) {
            case MazeConstant.LEFT_DIRECTION :
                if (inputMaze[ghost.getX()][(ghost.getY() - 1)] == MazeConstant.WALL_MARKER) {
                    isGhostPositionUpdated = true;
                    newGhostPosition = new Position(ghost.getX(), ghost.getY(), null
                            , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
                }
                break;
            case MazeConstant.RIGHT_DIRECTION :
                if (inputMaze[ghost.getX()][(ghost.getY() + 1)] == MazeConstant.WALL_MARKER) {
                    isGhostPositionUpdated = true;
                    newGhostPosition = new Position(ghost.getX(), ghost.getY(), null
                            , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.LEFT_DIRECTION);
                }
                break;
        }
        if (isGhostPositionUpdated) {
            Preprocessing.setGhostPosition(newGhostPosition);
        }
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

    // Debug purpose then delete it
    private void displayCharArray(char [][] array, Position pacman, Position ghost) {
        array[pacman.getX()][pacman.getY()] = 'P';
        array[ghost.getX()][ghost.getY()] = 'g';
        int row = array.length;
        int col = array[0].length;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                System.out.print(array[i][j]);
            }
            System.out.println();
        }
        array[pacman.getX()][pacman.getY()] = ' ';
        array[ghost.getX()][ghost.getY()] = ' ';
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
