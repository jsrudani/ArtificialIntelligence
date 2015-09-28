package ai.mp.search.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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
        } else if (Preprocessing.isMultipleGoal() && !Preprocessing.isSuboptimalSearch()) {
            findPathThroughMultipleGoals(Preprocessing.getStartPosition(), Preprocessing.getGoalSet());
        } else if (Preprocessing.isSuboptimalSearch()) {
            /*findPathForSuboptimalSearch(Preprocessing.getStartPosition(), Preprocessing.getGoalSet(), openPosition
                    , expandedPosition);*/
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
            System.out.println(openPosition);
            currentPosition = openPosition.pollFirstEntry().getKey();
            System.out.println("Current child " + currentPosition);
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

        /**
         * It holds all the available positions which are yet to be expanded.
         * It is sorted based on cost.
         */
        TreeMap<MazeState, Long> openMazeState = new TreeMap<MazeState,Long>(new MazeStateComparator());
        /**
         * It holds all the expanded positions.
         */
        Map<MazeState,Long> expandedMazeState = new HashMap<MazeState,Long>();
        MazeState currentMazeState = null;

        // Initialize and Add the start position into maze state
        currentMazeState = new MazeState(startPosition);
        currentMazeState.setGoalSet(goalSet);
        //currentMazeState.setExpandedMazeState(expandedMazeState);
        openMazeState.put(currentMazeState, currentMazeState.getApproachableCost());

        // Loop till open maze state is not empty
        while (!openMazeState.isEmpty()) {
            //System.out.println("Open Set"  + openMazeState + "\n");
            // Get the least edge cost maze state
            currentMazeState = openMazeState.pollFirstEntry().getKey();
            //System.out.println("Current position " + currentMazeState);
            // Check if current position in maze is goal then decrement the goal set in that maze set
            if (currentMazeState.getGoalSet().contains(currentMazeState.getPosition())) {
                //System.out.println("Goal reached --> " + currentMazeState.getPosition());
                Set<Position> mazeGoalSet = currentMazeState.getGoalSet();
                mazeGoalSet.remove(currentMazeState.getPosition());
                currentMazeState.setGoalSet(mazeGoalSet);
                //System.out.println("Updated goal size " + currentMazeState.getGoalSet().size());
                if (currentMazeState.getGoalSet().isEmpty()) {
                    // All the goals are covered. so just print the maze state following the parent pointer
                    isGoalReached = true;
                    break;
                }
            } //else {
                //expandedMazeState = currentMazeState.getExpandedMazeState();
                //System.out.println(expandedMazeState);
                // Mark the state as visited
                expandedMazeState.put(currentMazeState,currentMazeState.getApproachableCost());
                // Update the expanded set
                //currentMazeState.setExpandedMazeState(expandedMazeState);
                // increment the nodes expanded
                nodesExpanded += 1;
                // Get the successor maze state
                List<MazeState> children = getValidChildPosition(currentMazeState, expandedMazeState);
                Iterator<MazeState> child = children.iterator();
                // Build MST for each child
                while (child.hasNext()) {
                    MazeState eachMazeChild = child.next();
                    // Build the MST for child and goal set
                    MST spanningTree = new MST(eachMazeChild.getPosition(), eachMazeChild.getGoalSet());
                    long edgeCost = spanningTree.buildMST();
                    // Set the cost of each child to edge cost
                    calculateHeuristicAndUpdateCost(currentMazeState, eachMazeChild
                            , openMazeState, expandedMazeState, (edgeCost));
                    //System.out.println("eachMazeChild " + eachMazeChild);
                }
                //System.out.println("===========================================");
            //}
        }
        //System.out.println("open set size " + openMazeState.size());
        // Check if solution exist
        Stack<Position> positionStack = new Stack<Position>();
        if (isGoalReached) {
            while (currentMazeState != null && currentMazeState.getParent() != null) {
                //System.out.println(currentMazeState);
                // Check if it is part of goal
                if (goalSet.contains(currentMazeState.getPosition())) {
                    positionStack.push(currentMazeState.getPosition());
                    goalSet.remove(currentMazeState.getPosition());
                }
                currentMazeState = currentMazeState.getParent();
                stepCost += 1;
            }
            System.out.println("Total step cost " + stepCost);
        }
        // Print the path
        int index = 49;
        System.out.println(positionStack);
        while (!positionStack.isEmpty()) {
            char sol = (char) index;
            Position goal = positionStack.pop();
            this.solutionMaze[goal.getX()][goal.getY()] = sol;
            index += 1;
            if (index == 58) {
                index = 97;
            } else if (index == 123) {
                index = 65;
            }
        }
    }

    /**
     * It is used to clone the goal set from parent maze to child maze
     * 
     * @param parentState
     * @param childState
     */
    private void cloneGoalSet(MazeState parentState, MazeState childState) {
        Set<Position> parentGoalSet = parentState.getGoalSet();
        Set<Position> childGoalSet = new LinkedHashSet<Position>();
        Iterator<Position> parentGoalSetItr = parentGoalSet.iterator();
        while (parentGoalSetItr.hasNext()) {
            childGoalSet.add(parentGoalSetItr.next());
        }
        childState.setGoalSet(childGoalSet);
    }

    /**
     * It is used to find sub-optimal path through all search dots.
     * 
     * @param goalSet
     */
/*    private void findPathForSuboptimalSearch(Position startPosition, Set<Position> goalSet, TreeMap<Position, Long> openPosition, Map<Position,Long> expandedPosition) {

        Position currentPosition;
        // Add to open position
        openPosition.put(startPosition, startPosition.getCost());
        // Loop till all dots are covered
        while (!goalSet.isEmpty()) {
            currentPosition = openPosition.pollFirstEntry().getKey();
            // Check if best next neighbor is goal
            if (goalSet.contains(currentPosition)) {
                goalSet.remove(currentPosition);
                // Draw solution path from current goal to start position
                this.solutionMaze[currentPosition.getX()][currentPosition.getY()] = 'P';
                MazeMetrics metrics = drawSolutionPath(this.getSolutionMaze(), currentPosition);
                //System.out.println("Before updating step cost " + this.stepCost);
                //System.out.println("metrics.getStepCost() " + metrics.getStepCost());
                this.stepCost += metrics.getStepCost();
                //System.out.println("After updating step cost " + this.getStepCost());
                this.solutionCost += metrics.getSolutionCost();
                MazeSearch.displayCharArray(this.getSolutionMaze());
                //System.out.println("Step-----> " + this.getStepCost());
                // Clear the expanded set and open set to start fresh from new start point
                expandedPosition.clear();
                openPosition.clear();
                // Add the current goal as new start point in open position and also reset
                // its parent pointer else it would be count twice
                currentPosition.setParent(null);
                currentPosition.setApproachableCost(0L);
                openPosition.put(currentPosition, currentPosition.getApproachableCost());
                if (goalSet.isEmpty())
                    break;
            } else {
                // Expand the current node
                expandedPosition.put(currentPosition, currentPosition.getApproachableCost());
                // Increment nodes expanded
                nodesExpanded += 1;
                // Find the child position
                List<Position> children = getValidChildPosition(currentPosition, expandedPosition);
                Iterator<Position> child = children.iterator();
                // Build MST for each child
                while (child.hasNext()) {
                    Position eachChild = child.next();
                    // Build the MST for child and goal set
                    System.out.println("Child " + eachChild);
                    MST spanningTree = new MST(eachChild, goalSet);
                    long edgeCost = spanningTree.buildMST();
                    // Set the cost of each child to edge cost
                    calculateHeuristicAndUpdateCost(currentPosition, eachChild
                            , openPosition, expandedPosition, (edgeCost * 2));
                }
                System.out.println("----------------------------");
            }
        }
    }*/

    /**
     * It is used to return all valid position possible from given position
     * 
     * @param currentPosition
     * @return Collection
     */
    private List<MazeState> getValidChildPosition(MazeState parentState, Map<MazeState,Long> expandedMazeState) {

        List<MazeState> children = new ArrayList<MazeState>();
        Position child = null;
        MazeState childMazeState = null;
        Position currentPosition = parentState.getPosition();
        // Get the upper node
        /*child = new Position((currentPosition.getX()-1), currentPosition.getY(), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.UP_DIRECTION);*/
        if (inputMaze[(currentPosition.getX()-1)][currentPosition.getY()] != MazeConstant.WALL_MARKER) {
            child = new Position((currentPosition.getX()-1), currentPosition.getY(), currentPosition
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.UP_DIRECTION);
            childMazeState = new MazeState(child);
            cloneGoalSet(parentState, childMazeState);
            if (!expandedMazeState.containsKey(childMazeState)) {
                children.add(childMazeState);
            }
        }

        // Get the lower node
        /*child = new Position((currentPosition.getX()+1), currentPosition.getY(), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.DOWN_DIRECTION);*/
        if (inputMaze[(currentPosition.getX()+1)][currentPosition.getY()] != MazeConstant.WALL_MARKER) {
            child = new Position((currentPosition.getX()+1), currentPosition.getY(), currentPosition
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.DOWN_DIRECTION);
            childMazeState = new MazeState(child);
            cloneGoalSet(parentState, childMazeState);
            if (!expandedMazeState.containsKey(childMazeState)) {
                children.add(childMazeState);
            }
        }

        // Get the left node
        /*child = new Position(currentPosition.getX(), (currentPosition.getY()-1), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.LEFT_DIRECTION);*/
        if (inputMaze[currentPosition.getX()][(currentPosition.getY()-1)] != MazeConstant.WALL_MARKER) {
            child = new Position(currentPosition.getX(), (currentPosition.getY()-1), currentPosition
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.LEFT_DIRECTION);
            childMazeState = new MazeState(child);
            cloneGoalSet(parentState, childMazeState);
            if (!expandedMazeState.containsKey(childMazeState)) {
                children.add(childMazeState);
            }
        }

        // Get the right node
        /*child = new Position(currentPosition.getX(), (currentPosition.getY()+1), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);*/
        if (inputMaze[currentPosition.getX()][(currentPosition.getY()+1)] != MazeConstant.WALL_MARKER) {
            child = new Position(currentPosition.getX(), (currentPosition.getY()+1), currentPosition
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
            childMazeState = new MazeState(child);
            cloneGoalSet(parentState, childMazeState);
            if (!expandedMazeState.containsKey(childMazeState)) {
                children.add(childMazeState);
            }
        }
        return children;
    }

    /**
     * It is used to check if child node exist in expanded or open position list. 
     * If exist then update the Map accordingly.
     * 
     * @param parentNode
     * @param childNode
     * @param openPosition
     * @param expandedPosition
     * @param heuristicCost
     */
    private void calculateHeuristicAndUpdateCost(MazeState parentMaze, MazeState childMaze
            , TreeMap<MazeState, Long> openMazeState, Map<MazeState,Long> expandedMazeState
            , long heuristicCost) {
        long approachedCost = (parentMaze.getApproachableCost() + 1);
        //long totalHeuristicCost = (approachedCost + heuristicCost);
        // Initialize default field
        childMaze.setParent(parentMaze);
        childMaze.setApproachableCost(approachedCost);
        childMaze.setEdgeCost( (childMaze.getApproachableCost() + heuristicCost) );
        // Check if it is in open list
        if (!openMazeState.containsKey(childMaze)) {
            openMazeState.put(childMaze, childMaze.getApproachableCost());
        } else if (openMazeState.containsKey(childMaze)) {
            //System.out.println("Matches " + childMaze);
            //System.out.println("approachedCost" + approachedCost);
            //System.out.println("openMazeState.get(childMaze) " + openMazeState.get(childMaze));
            if (approachedCost < openMazeState.get(childMaze)) {
                //System.out.println("B4 remove ");
                // Remove old child maze
                openMazeState.remove(childMaze);
                //System.out.println("After remove " + openMazeState);
                openMazeState.put(childMaze, childMaze.getApproachableCost());
                //System.out.println("After adding new " + openMazeState);
            }
        }
        /* //long totalHeuristicCost = approachedCost + heuristicCost;
        if (!openMazeState.containsKey(childMaze) || approachedCost < openMazeState.get(childMaze)) {
            childMaze.setParent(parentMaze);
            childMaze.setApproachableCost(approachedCost);
            childMaze.setEdgeCost( (childMaze.getApproachableCost() + heuristicCost) );
            if (approachedCost < openMazeState.get(childMaze)) {
                childMaze.setGoalSet(parentMaze.getGoalSet());
            }
            if (!openMazeState.containsKey(childMaze)) {
                openMazeState.put(childMaze, childMaze.getApproachableCost());
            }
        }*/
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
/**
 * It is used to compare two Maze state based on heuristic function.
 *
 */
class MazeStateComparator implements Comparator<MazeState> {

    @Override
    public int compare(MazeState o1, MazeState o2) {
        if (o1.equals(o2)) {
            return 0;
        }
        return (o1.getEdgeCost() < o2.getEdgeCost() ? -1 : 1);
    }
}
