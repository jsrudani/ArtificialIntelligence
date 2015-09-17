package ai.mp.search.strategy;

/**
 * It holds all the constant used in the Maze search strategy
 * 
 * @author rudani2
 *
 */
public class MazeConstant {

    public static final String SEARCH_STRATEGY = "Please select the search strategy from following list by "
            + "typing the number corresponding to it\n"
            + "1. BFS (Breath-first search)\n"
            + "2. DFS (Depth-first search)\n"
            + "3. Greedy best-first search\n"
            + "4. A* search";

    public static final String SEARCH_WITH_ADDITIONAL_OPTION = "Please select additional option from following list by "
        + "typing the number corresponding to it\n"
        + "1. Include PENALTY turns \n"
        + "2. Play with GHOST \n"
        + "3. Multiple GOAL state \n"
        + "4. Suboptimal search \n"
        + "5. PATH finding";

    public static final int EXPECTED_USER_SEARCH_TYPE_INPUT_COUNT = 4;
    public static final int EXPECTED_USER_ADDITIONAL_INPUT_COUNT = 5;
    public static final int WALL_MARKER = -1;
    public static final int START_POSITION_MARKER = 0;
    public static final int GOAL_POSITION_MARKER = 1;
    public static final int GHOST_POSITION_MARKER = 2;
    public static final int PATH_MARKER = 3;
    public static final int VISITED = 4;
    public static final long DEFAULT_COST = Long.MAX_VALUE;
    public static final int QUEUE_INITIAL_CAPACITY = 20;
    public static final int DEFAULT_DIRECTION = 0;
    public static final int UP_DIRECTION = 1;
    public static final int DOWN_DIRECTION = 2;
    public static final int LEFT_DIRECTION = 3;
    public static final int RIGHT_DIRECTION = 4;
    public static final int FORWARD_COST = 2;
    public static final int TURN_COST = 1;
    public static final int DEFAULT_PENALTY = 0;

}
