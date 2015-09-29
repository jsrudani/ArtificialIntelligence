package ai.mp.search.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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

        // Check and initialize ghost starting direction
        if (Preprocessing.isGhost()) {
            checkAndInitializeGhostDirection(Preprocessing.getGhostPosition());
        }

        // Add the start position into open position
        openPosition.put(Preprocessing.getStartPosition(),Preprocessing.getStartPosition().getApproachableCost());

        // Check for additional options
        if (Preprocessing.isPathFind()) {
            findPathUsingAStar(openPosition, expandedPosition);
        } else if (Preprocessing.isPenalty()) {
            findPathUsingPenalty(openPosition, expandedPosition, MazeConstant.TURN_COST, MazeConstant.FORWARD_COST);
        } else if (Preprocessing.isGhost()) {
            findPathAvoidGhost(Preprocessing.getStartPosition(), Preprocessing.getGhostPosition(), debugMatrix);
        } else if (Preprocessing.isMultipleGoal()) {
            findPathThroughMultipleGoals(Preprocessing.getStartPosition(), Preprocessing.getGoalSet());
        } else if (Preprocessing.isOurHeuristic()) {
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

        // Traverse till openPosition is not empty and get the position with lowest heuristic value.
        while (!openPosition.isEmpty()) {
            currentPosition = openPosition.pollFirstEntry().getKey();
            if (inputMaze[currentPosition.getX()][currentPosition.getY()] == MazeConstant.GOAL_POSITION_MARKER) {
                isGoalReached = true;
                break;
            } else {
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
        // Traverse till openPosition is not empty and get the position with lowest heuristic value.
        while (!openPosition.isEmpty()) {
            currentPosition = openPosition.pollFirstEntry().getKey();
            if (inputMaze[currentPosition.getX()][currentPosition.getY()] == MazeConstant.GOAL_POSITION_MARKER) {
                isGoalReached = true;
                break;
            } else {
                // Calculate penalty
                long penalty = calculatePenalty(currentPosition, turnCost, forwardCost);
                currentPosition.setPenalty(penalty);

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
            System.out.println("Penalty cost " + metrics.getPenalty());
        }
    }

    /**
     * It is used to find the path from start to goal state with Ghost in the maze.
     * 
     * @param openPosition
     * @param expandedPosition
     * @param ghostPosition
     */
    private void findPathAvoidGhost(Position startPosition, Position ghostPosition, char [][] debugMatrix) {

        /**
         * It holds all the available positions which are yet to be expanded.
         * It is sorted based on cost.
         */
        TreeMap<MazeStateWithGhost, Long> openMazeState = new TreeMap<MazeStateWithGhost,Long>(new MazeStateWithGhostComparator());
        /**
         * It holds all the expanded maze state.
         */
        Map<MazeStateWithGhost,Long> expandedMazeState = new HashMap<MazeStateWithGhost,Long>();
        MazeStateWithGhost currentMazeState = null;

        // Initialize and Add the start position into maze state
        currentMazeState = new MazeStateWithGhost(startPosition);
        currentMazeState.setGhost(ghostPosition);

        // Add it to frontier
        openMazeState.put(currentMazeState, 0L);

        // Traverse till openPosition is not empty and get the position with lowest heuristic value.
        while (!openMazeState.isEmpty()) {

            currentMazeState = openMazeState.pollFirstEntry().getKey();
            // Get the ghost position associated with pacman
            ghostPosition = currentMazeState.getGhost();
            displayCharArray(debugMatrix, currentMazeState.getPosition(), ghostPosition);

            if (inputMaze[currentMazeState.getPosition().getX()][currentMazeState.getPosition().getY()]
                    == MazeConstant.GOAL_POSITION_MARKER) {
                isGoalReached = true;
                break;
            } else {

                // Mark as visited by inserting into expanded list
                expandedMazeState.put(currentMazeState,currentMazeState.getApproachableCost());
                // Increment the nodes expanded
                nodesExpanded += 1;

                // Check if there is ghost or not
                if (checkForGhostAndMoveGhost(currentMazeState) ) {
                    continue;
                }
                // Get the successor node
                getSuccessorNode(currentMazeState, openMazeState, expandedMazeState);
            }
        }
        // Check if solution exist
        if (isGoalReached) {
            while (currentMazeState != null && currentMazeState.getParent() != null) {
                System.out.println(currentMazeState.getPosition());
                this.solutionMaze[currentMazeState.getPosition().getX()][currentMazeState.getPosition().getY()] = '.';
                currentMazeState = currentMazeState.getParent();
                stepCost += 1;
            }
            System.out.println("Total step cost " + stepCost);
        }
    }

    /**
     * It is used to find all valid successor for given maze state. It builds new maze state with
     * new ghost position and update if already present in open state
     * 
     * @param parentMazeState
     * @param openMazeState
     * @param expandedMazeState
     */
    private void getSuccessorNode(MazeStateWithGhost parentMazeState
            , TreeMap<MazeStateWithGhost, Long> openMazeState
            , Map<MazeStateWithGhost,Long> expandedMazeState) {
        Position child = null;
        MazeStateWithGhost childMazeState = null;
        Position currentPosition = parentMazeState.getPosition();
        // Get the new ghost position and set for each valid successor node
        Position ghost = Preprocessing.getGhostPosition();

        // Get upper node
        if (inputMaze[(currentPosition.getX()-1)][currentPosition.getY()] != MazeConstant.WALL_MARKER) {
            child = new Position((currentPosition.getX()-1), currentPosition.getY(), currentPosition
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.UP_DIRECTION);
            childMazeState = new MazeStateWithGhost(child);
            childMazeState.setGhost(ghost);
            if (!expandedMazeState.containsKey(childMazeState)) {
                calculateHeuristicForChildMazeWithGhost(parentMazeState, childMazeState, openMazeState);
            }
        }
        // Get down node
        if (inputMaze[(currentPosition.getX()+1)][currentPosition.getY()] != MazeConstant.WALL_MARKER) {
            child = new Position((currentPosition.getX()+1), currentPosition.getY(), currentPosition
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.DOWN_DIRECTION);
            childMazeState = new MazeStateWithGhost(child);
            childMazeState.setGhost(ghost);
            if (!expandedMazeState.containsKey(childMazeState)) {
                calculateHeuristicForChildMazeWithGhost(parentMazeState, childMazeState, openMazeState);
            }
        }
        // Get left node
        if (inputMaze[(currentPosition.getX())][currentPosition.getY() - 1] != MazeConstant.WALL_MARKER) {
            child = new Position(currentPosition.getX(), (currentPosition.getY() - 1), currentPosition
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.LEFT_DIRECTION);
            childMazeState = new MazeStateWithGhost(child);
            childMazeState.setGhost(ghost);
            if (!expandedMazeState.containsKey(childMazeState)) {
                calculateHeuristicForChildMazeWithGhost(parentMazeState, childMazeState, openMazeState);
            }
        }
        // Get right node
        if (inputMaze[(currentPosition.getX())][currentPosition.getY() + 1] != MazeConstant.WALL_MARKER) {
            child = new Position(currentPosition.getX(), (currentPosition.getY() + 1), currentPosition
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
            childMazeState = new MazeStateWithGhost(child);
            childMazeState.setGhost(ghost);
            if (!expandedMazeState.containsKey(childMazeState)) {
                calculateHeuristicForChildMazeWithGhost(parentMazeState, childMazeState, openMazeState);
            }
        }
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
    private void calculateHeuristicForChildMazeWithGhost(MazeStateWithGhost parentMaze
            , MazeStateWithGhost childMaze
            , TreeMap<MazeStateWithGhost, Long> openMazeState) {
        long approachedCost = (parentMaze.getApproachableCost() + 1);
        long heuristicCost = getHeuristicValue(Preprocessing.getGoalPosition(), childMaze.getPosition());
        long totalHeuristicCost = (approachedCost + heuristicCost);
        // Initialize default field
        childMaze.setParent(parentMaze);
        childMaze.setApproachableCost(approachedCost);
        childMaze.setEdgeCost( totalHeuristicCost );
        // Check if it is in open list
        if (!openMazeState.containsKey(childMaze)) {
            openMazeState.put(childMaze, childMaze.getEdgeCost());
        } else if (openMazeState.containsKey(childMaze)) {
            if (totalHeuristicCost < openMazeState.get(childMaze)) {
                // Remove old child maze
                openMazeState.remove(childMaze);
                openMazeState.put(childMaze, childMaze.getEdgeCost());
            }
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
        MST startSpanningTree = new MST(currentMazeState.getPosition(), currentMazeState.getGoalSet());
        long startEdgeCost = startSpanningTree.buildMST();
        currentMazeState.setEdgeCost(startEdgeCost);
        openMazeState.put(currentMazeState, currentMazeState.getEdgeCost());

        // Loop till open maze state is not empty
        while (!openMazeState.isEmpty()) {
            // Get the least edge cost maze state
            currentMazeState = openMazeState.pollFirstEntry().getKey();
            // Check if current position in maze is goal then decrement the goal set in that maze set
            if (currentMazeState.getGoalSet().contains(currentMazeState.getPosition())) {
                Set<Position> mazeGoalSet = currentMazeState.getGoalSet();
                mazeGoalSet.remove(currentMazeState.getPosition());
                currentMazeState.setGoalSet(mazeGoalSet);
                if (currentMazeState.getGoalSet().isEmpty()) {
                    // All the goals are covered. so just print the maze state following the parent pointer
                    isGoalReached = true;
                    break;
                }
            } 
            // Mark the state as visited
            expandedMazeState.put(currentMazeState,
                    currentMazeState.getApproachableCost());
            // Increment the nodes expanded
            nodesExpanded += 1;
            // Get the successor maze state
            List<MazeState> children = getValidChildPosition(currentMazeState,
                    expandedMazeState);
            Iterator<MazeState> child = children.iterator();
            // Build MST for each child
            while (child.hasNext()) {
                MazeState eachMazeChild = child.next();
                // Build the MST for child and goal set
                MST spanningTree = new MST(eachMazeChild.getPosition(),
                        eachMazeChild.getGoalSet());
                long edgeCost = spanningTree.buildMST();
                // Set the cost of each child to edge cost
                calculateHeuristicAndUpdateCost(currentMazeState,
                        eachMazeChild, openMazeState, expandedMazeState,
                        (edgeCost * 2));
            }
         }

        // Check if solution exist
        Stack<Position> positionStack = new Stack<Position>();
        if (isGoalReached) {
            while (currentMazeState != null && currentMazeState.getParent() != null) {
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
        long totalHeuristicCost = (approachedCost + heuristicCost);
        // Initialize default field
        childMaze.setParent(parentMaze);
        childMaze.setApproachableCost(approachedCost);
        childMaze.setEdgeCost( (approachedCost + heuristicCost) );
        // Check if it is in open list
        if (!openMazeState.containsKey(childMaze)) {
            openMazeState.put(childMaze, childMaze.getEdgeCost());
        } else if (openMazeState.containsKey(childMaze)) {
            if (totalHeuristicCost < openMazeState.get(childMaze)) {
                // Remove old child maze
                openMazeState.remove(childMaze);
                openMazeState.put(childMaze, childMaze.getEdgeCost());
            }
        }
    }

    /**
     * It is used to move the ghost in appropriate direction. If there is a wall then ghost direction is changed and
     * move in opposite direction. It is also used to indicate whether the new position of ghost is danger for
     * Pacman.
     * 
     * @param ghost
     * @return boolean
     */
    private boolean checkForGhostAndMoveGhost(MazeStateWithGhost pacman) {
        boolean dangerPosition = false;
        Position ghost = pacman.getGhost();
        // Check whether next move of ghost is valid or not and change direction accordingly
        if (checkAndInitializeGhostDirection(ghost)) {
            ghost = Preprocessing.getGhostPosition();
        }
        // Add condition to check whether successor of parent is possible or not
        if ( (pacman.getPosition().equals(ghost) && pacman.getPosition().getDirection() != ghost.getDirection())
                ||(pacman.getParent() != null && pacman.getParent().getGhost().equals(pacman.getPosition()) && isDirectionOpposite(pacman))) {
            dangerPosition = true;
        }
        // Move the ghost even if pacman knows that its not a valid position
        moveGhost(ghost);
        return dangerPosition;
    }

    /**
     * It is used to check whether pacman and ghost are in opposite direction or not
     * 
     * @param pacman
     * @return boolean
     */
    private boolean isDirectionOpposite(MazeStateWithGhost pacman) {
        boolean isDirectionOpposite = false;
        // Check if my and my ghost direction are opposite i.e left <-> right or up <-> down
        switch(pacman.getPosition().getDirection()) {
            case MazeConstant.LEFT_DIRECTION :
                if (pacman.getGhost().getDirection() == MazeConstant.RIGHT_DIRECTION) {
                    isDirectionOpposite = true;
                }
                break;
            case MazeConstant.RIGHT_DIRECTION :
                if (pacman.getGhost().getDirection() == MazeConstant.LEFT_DIRECTION) {
                    isDirectionOpposite = true;
                }
                break;
            case MazeConstant.UP_DIRECTION :
                if (pacman.getGhost().getDirection() == MazeConstant.DOWN_DIRECTION) {
                    isDirectionOpposite = true;
                }
                break;
            case MazeConstant.DOWN_DIRECTION :
                if (pacman.getGhost().getDirection() == MazeConstant.UP_DIRECTION) {
                    isDirectionOpposite = true;
                }
                break;
        }
        return isDirectionOpposite;
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
        long turns = 0L;
        // Get the new ghost position and set for each valid successor node
        Position ghost = Preprocessing.getGhostPosition();

        // Get the upper node
        child = new Position((currentPosition.getX()-1), currentPosition.getY(), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.UP_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            // Check if child is already in expanded/open position and if exist compare the overall cost
            if (!expandedPosition.containsKey(child)) {
                if (Preprocessing.isOurHeuristic()) {
                    // Get the number of turns
                    turns = countAndSetNumberOfTurns(child);
                }
                checkExistanceWithLowerCostAndUpdateMap(currentPosition, child
                        , openPosition, expandedPosition, penalty, turns);
            }
        }

        // Get the lower node
        child = new Position((currentPosition.getX()+1), currentPosition.getY(), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.DOWN_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            // Check if child is already in expanded/open position and if exist compare the overall cost
            if (!expandedPosition.containsKey(child)) {
                if (Preprocessing.isOurHeuristic()) {
                    // Get the number of turns
                    turns = countAndSetNumberOfTurns(child);
                }
                checkExistanceWithLowerCostAndUpdateMap(currentPosition, child
                        , openPosition, expandedPosition, penalty, turns);
            }
        }

        // Get the left node
        child = new Position(currentPosition.getX(), (currentPosition.getY()-1), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.LEFT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            // Check if child is already in expanded/open position and if exist compare the overall cost
            if (!expandedPosition.containsKey(child)) {
                if (Preprocessing.isOurHeuristic()) {
                    // Get the number of turns
                    turns = countAndSetNumberOfTurns(child);
                }
                checkExistanceWithLowerCostAndUpdateMap(currentPosition, child
                        , openPosition, expandedPosition, penalty, turns);
            }
        }

        // Get the right node
        child = new Position(currentPosition.getX(), (currentPosition.getY()+1), currentPosition
                , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
        if (isChildValid(inputMaze, child)) {
            // Check if child is already in expanded/open position and if exist compare the overall cost
            if (!expandedPosition.containsKey(child)) {
                if (Preprocessing.isOurHeuristic()) {
                    // Get the number of turns
                    turns = countAndSetNumberOfTurns(child);
                }
                checkExistanceWithLowerCostAndUpdateMap(currentPosition, child
                        , openPosition, expandedPosition, penalty, turns);
            }
        }
    }

    /**
     * It is used to predict the number of turn in future
     * 
     * @param currentPosition
     * @return long
     */
    private long countAndSetNumberOfTurns(Position currentPosition) {
        long turnCount = 0L;
        int xPos = 0;
        int yPos = 0;
        switch(currentPosition.getDirection()) {
            case MazeConstant.LEFT_DIRECTION :
                xPos = currentPosition.getX();
                yPos = (currentPosition.getY() - 1);
                while (inputMaze[xPos][yPos] != MazeConstant.WALL_MARKER) {
                    turnCount += 1;
                    yPos -= 1;
                }
                break;
            case MazeConstant.RIGHT_DIRECTION :
                xPos = currentPosition.getX();
                yPos = (currentPosition.getY() + 1);
                while (inputMaze[xPos][yPos] != MazeConstant.WALL_MARKER) {
                    turnCount += 1;
                    yPos += 1;
                }
                break;
            case MazeConstant.UP_DIRECTION :
                xPos = (currentPosition.getX() - 1);
                yPos = currentPosition.getY();
                while (inputMaze[xPos][yPos] != MazeConstant.WALL_MARKER) {
                    turnCount += 1;
                    xPos -= 1;
                }
                break;
            case MazeConstant.DOWN_DIRECTION :
                xPos = (currentPosition.getX() + 1);
                yPos = currentPosition.getY();
                while (inputMaze[xPos][yPos] != MazeConstant.WALL_MARKER) {
                    turnCount += 1;
                    xPos += 1;
                }
                break;
        }
        return turnCount;
    }

    /**
     * It is used to check if child node exist in expanded or open position list. If exist then update the Map accordingly.
     * 
     */
    private void checkExistanceWithLowerCostAndUpdateMap(Position parentNode, Position childNode
            , TreeMap<Position, Long> openPosition, Map<Position,Long> expandedPosition
            , long penalty, long turns) {

        // Add the penalty as current node approached cost. Since it will include penalty to reach current node
        long approachedCost = penalty + (parentNode.getApproachableCost() + 1);
        long heuristicCost = getHeuristicValue(Preprocessing.getGoalPosition(), childNode);
        if (!openPosition.containsKey(childNode) || approachedCost < openPosition.get(childNode)) {
            childNode.setParent(parentNode);
            childNode.setApproachableCost(approachedCost);
            childNode.setCost( (childNode.getApproachableCost() + heuristicCost + turns) );
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

    /**
     * It is used to ghost direction. If there is a wall left/right immediately to current position then
     * direction is changed to opposite direction.
     *  
     * @param ghost
     * @return boolean
     */
    private boolean checkAndInitializeGhostDirection(Position ghost) {
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
        return isGhostPositionUpdated;
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
/**
 * It is used to compare two Maze state with ghost based on heuristic function.
 *
 */
class MazeStateWithGhostComparator implements Comparator<MazeStateWithGhost> {

    @Override
    public int compare(MazeStateWithGhost o1, MazeStateWithGhost o2) {
        if (o1.equals(o2)) {
            return 0;
        }
        return (o1.getEdgeCost() < o2.getEdgeCost() ? -1 : 1);
    }
}
