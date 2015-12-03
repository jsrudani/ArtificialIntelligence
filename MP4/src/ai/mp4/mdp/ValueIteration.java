package ai.mp4.mdp;

import java.util.ArrayList;
import java.util.List;

/**
 * It is used to find optimal policy using value iteration method.
 * <pre>
 * Pseudocode
 * 
 * repeat
        U <- U'
        for each state s in S do
            U'[s] <— R(s) + (gamma) MAX ( P(s'|s,a)U[s'] )
        if | U'[s] — U[s] | > delta then delta <- | U'[s] — U[s] |
    until delta < e(1 - gamma)/gamma
    return U
    
    </pre>
 * @author rudani2
 *
 */
public class ValueIteration {

    private final float [][] mapGrid;
    private final Position startPosition;
    private char[][] optimalPolicy;
    private List<float [][]> utility_per_iteration;
    private int iterationCount = 0;
    private float[][] uPrime;
    private float[][] u;

    ValueIteration (float [][] mapGrid, Position startPosition) {
        this.mapGrid = mapGrid;
        this.startPosition = startPosition;
        this.optimalPolicy = new char[mapGrid.length][mapGrid[0].length];
        this.uPrime = new float[mapGrid.length][mapGrid[0].length];
        this.u = new float[mapGrid.length][mapGrid[0].length];
        this.utility_per_iteration = new ArrayList<float [][]>();
    }

    public void findOptimalPolicy() {
        float threshold = MDPConstant.EPSILON * (1 - MDPConstant.GAMMA)/MDPConstant.GAMMA;
        float delta = 0.0f;
        Position state = startPosition;
        int rowSize = optimalPolicy.length;
        int colSize = optimalPolicy[0].length;
        do {
            delta = 0.0f;
            // Clone uprime into u
            cloneUPrime(u,uPrime);
            // For each state in grid
            for (int row = 0; row < rowSize; row++) {
                for (int col = 0; col < colSize; col++) {
                    state = new Position(row,col);
                    if (!isValid(state)) {
                        optimalPolicy[state.getX()][state.getY()] = 'W';
                        continue;
                    }
                        
                    // Get the max expected utility
                    Position optimalState = getMaxExpectedUtility(state,optimalPolicy,u);
                    // Set the utility for current state
                    uPrime[state.getX()][state.getY()] = mapGrid[state.getX()][state.getY()]
                                            + (MDPConstant.GAMMA * optimalState.getUtility());
                    // Check the difference for convergence
                    if (Math.abs(uPrime[state.getX()][state.getY()] - u[state.getX()][state.getY()]) > delta) {
                        delta = Math.abs(uPrime[state.getX()][state.getY()] - u[state.getX()][state.getY()]);
                    }
                }
            }
            /*// Get the max expected utility
            Position optimalState = getMaxExpectedUtility(state,optimalPolicy,u);
            // Set the utility for current state
            uPrime[state.getX()][state.getY()] = mapGrid[state.getX()][state.getY()]
                                  + (MDPConstant.GAMMA * optimalState.getUtility());
            // Check the difference for convergence
            if (Math.abs(uPrime[state.getX()][state.getY()] - u[state.getX()][state.getY()]) > delta) {
               delta = Math.abs(uPrime[state.getX()][state.getY()] - u[state.getX()][state.getY()]);
            }*/
            // Store the utility per iteration
            utility_per_iteration.add(uPrime);
            // Increment the iteration count
            iterationCount += 1;
            // Update state
            //state = optimalState;
        } while (delta >= threshold);
    }

    private Position getMaxExpectedUtility(Position state, char [][] optimalPolicy, float [][] u) {
        Position optimalState = null;
        int currentX = state.getX();
        int currentY = state.getY();
        float maxUtility = - Float.MAX_VALUE;
        float utility = 0.0f;

        // For all children UP, Right, Down, Left and calculate expected utility
        // UP
        utility += MDPConstant.INTENDED_OUTCOME_PROBABILITY * getUtility((currentX - 1), currentY, state, u);
        utility += MDPConstant.INTENDED_DIRECTION_PROBABILITY * getUtility(currentX, (currentY - 1), state, u);
        utility += MDPConstant.INTENDED_DIRECTION_PROBABILITY * getUtility(currentX, (currentY + 1), state, u);
        if (utility > maxUtility) {
            maxUtility = utility;
            if (!isValid((currentX - 1), currentY)) {
                optimalState = state;
            } else {
                optimalState = new Position((currentX - 1), currentY);
            }
            optimalPolicy[state.getX()][state.getY()] = 'U';
        }

        // Right
        utility = 0.0f;
        utility += MDPConstant.INTENDED_OUTCOME_PROBABILITY * getUtility(currentX, (currentY + 1), state, u);
        utility += MDPConstant.INTENDED_DIRECTION_PROBABILITY * getUtility((currentX - 1), currentY, state, u);
        utility += MDPConstant.INTENDED_DIRECTION_PROBABILITY * getUtility((currentX + 1), currentY, state, u);
        if (utility > maxUtility) {
            maxUtility = utility;
            if (!isValid(currentX, (currentY + 1))) {
                optimalState = state;
            } else {
                optimalState = new Position(currentX, (currentY + 1));
            }
            optimalPolicy[state.getX()][state.getY()] = 'R';
        }

        // Down
        utility = 0.0f;
        utility += MDPConstant.INTENDED_OUTCOME_PROBABILITY * getUtility((currentX + 1), currentY, state, u);
        utility += MDPConstant.INTENDED_DIRECTION_PROBABILITY * getUtility(currentX, (currentY - 1), state, u);
        utility += MDPConstant.INTENDED_DIRECTION_PROBABILITY * getUtility(currentX, (currentY + 1), state, u);
        if (utility > maxUtility) {
            maxUtility = utility;
            if (!isValid((currentX + 1), currentY)) {
                optimalState = state;
            } else {
                optimalState = new Position((currentX + 1), currentY);
            }
            optimalPolicy[state.getX()][state.getY()] = 'D';
        }

        // Left
        utility = 0.0f;
        utility += MDPConstant.INTENDED_OUTCOME_PROBABILITY * getUtility(currentX, (currentY - 1), state, u);
        utility += MDPConstant.INTENDED_DIRECTION_PROBABILITY * getUtility((currentX - 1), currentY, state, u);
        utility += MDPConstant.INTENDED_DIRECTION_PROBABILITY * getUtility((currentX + 1), currentY, state, u);
        if (utility > maxUtility) {
            maxUtility = utility;
            if (!isValid(currentX, (currentY - 1))) {
                optimalState = state;
            } else {
                optimalState = new Position(currentX, (currentY - 1));
            }
            optimalPolicy[state.getX()][state.getY()] = 'L';
        }
        // Set the max utility
        optimalState.setUtility(maxUtility);
        return optimalState;
    }

    private float getUtility(Position child, Position parent, float [][] u) {
        //System.out.println("Child " + child);
        //System.out.println("Parent " + parent);
        return (isValid(child) ? u[child.getX()][child.getY()] : u[parent.getX()][parent.getY()]);
    }

    private float getUtility(int child_x, int child_y, Position parent, float [][] u) {
        //System.out.println("Child " + child);
        //System.out.println("Parent " + parent);
        return (isValid(child_x,child_y) ? u[child_x][child_y] : u[parent.getX()][parent.getY()]);
    }

    private boolean isValid(Position child) {
        int rows = mapGrid.length;
        int cols = mapGrid[0].length;
        // Check if it is valid position
        if ( (child.getX() >= 0 && child.getX() < rows)
            && (child.getY() >= 0 && child.getY() < cols) ) {
            // Check if it is Wall
            if (mapGrid[child.getX()][child.getY()] == 0) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isValid(int x, int y) {
        int rows = mapGrid.length;
        int cols = mapGrid[0].length;
        // Check if it is valid position
        if ( (x >= 0 && x < rows) && (y >= 0 && y < cols) ) {
            // Check if it is Wall
            if (mapGrid[x][y] == 0) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void cloneUPrime(float [][]u, float [][] uPrime) {
        int rowSize = uPrime.length;
        int colSize = uPrime[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                u[row][col] = uPrime[row][col];
            }
        }
    }

    public char[][] getOptimalPolicy() {
        return optimalPolicy;
    }

    public float[][] getuPrime() {
        return uPrime;
    }

    public List<float[][]> getUtility_per_iteration() {
        return utility_per_iteration;
    }

    public int getIterationCount() {
        return iterationCount;
    }
}
