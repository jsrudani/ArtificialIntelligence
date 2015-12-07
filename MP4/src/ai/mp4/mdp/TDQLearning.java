package ai.mp4.mdp;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * It is used to perform TD (Temporal Difference) Q learning on given GRID and
 * find optimal policy. Q learning is Model free approach where we don't calculate
 * transition model (P(s'|s,a)) instead we learn Q factor with selected action 'a'
 * on state 's'. The action 'a' is selected based on exploration function (F(u,n)).
 * All the constant value used in learning like Ne, alpha, gamma are based on various
 * run.
 * 
 * @author rudani2
 *
 */
public class TDQLearning {

    private final float [][] mapGrid;
    private final Position startPosition;
    private final Set<Position> terminalState;
    private final boolean isRewardTerminal;
    // It holds the true utility calculated using MDP value iteration
    private final float [][] U;
    private char[][] optimalPolicy;
    private int iterationCount = 0;
    // qPrime is used to hold new calculated q values
    private float [][][] qPrime;
    // qPrime is used to hold previous calculated q values
    private float [][][] q;
    // n is used to hold number of times action 'a' is performed on state 's'
    private int [][][] n;
    // It holds the expected utility calculated based on Q
    private float [][] uPrime;

    TDQLearning (float [][] mapGrid
            , Position startPosition
            , Set<Position> terminalState
            , boolean isRewardTerminal
            , float[][] U) {
        this.mapGrid = mapGrid;
        this.startPosition = startPosition;
        this.terminalState = terminalState;
        this.isRewardTerminal = isRewardTerminal;
        this.U = U;
        this.optimalPolicy = new char[mapGrid.length][mapGrid[0].length];
        initializeOptimalPolicy(terminalState);
        this.qPrime = new float [mapGrid.length][mapGrid[0].length][MDPConstant.MAXIMUM_NEXT_STATE];
        this.uPrime = new float [mapGrid.length][mapGrid[0].length];
        this.q = new float [mapGrid.length][mapGrid[0].length][MDPConstant.MAXIMUM_NEXT_STATE];
        this.n = new int [mapGrid.length][mapGrid[0].length][MDPConstant.MAXIMUM_NEXT_STATE];
    }

    public void findOptimalPolicy() {
        float threshold = MDPConstant.EPSILON * (1 - MDPConstant.GAMMA)/MDPConstant.GAMMA;
        float delta = 0.0f;
        float alpha = 0.0f;
        Position state = startPosition;
        int rowSize = optimalPolicy.length;
        int colSize = optimalPolicy[0].length;

        do {

            delta = 0.0f;
            Direction a = null;
            // Clone qPrime into q
            cloneQPrime(q,qPrime);
            // Randomly select the valid state and non terminal state
            state = getRandomStateFromGrid(rowSize, colSize);
            //state = startPosition;

            // Learn the model for number of trails
            for (int trailNo = 0; trailNo < MDPConstant.NUMBER_OF_TRIAL; trailNo++) {

                if (a == null) {
                    // Update alpha as per trail
                    alpha = (float) (60.0/(59.0 + 0.0));
                } else {
                    // Update alpha as per optimal action
                    alpha = (float) (60.0/(59.0 + (float)n[state.getX()][state.getY()][a.ordinal()]));
                }

                // Check if it is terminal state, if yes then add reward and start again
                if (isRewardTerminal && isTerminalState(state)) {
                    // Set the optimal action for current reward as -
                    this.optimalPolicy[state.getX()][state.getY()] = '-';
                    // Store the reward of terminal state in desired direction
                    if (a != null) {
                        qPrime[state.getX()][state.getY()][a.ordinal()]
                                = mapGrid[state.getX()][state.getY()];
                    }
                    /*// Store the reward of terminal state in all direction
                    for (Direction direction : Direction.values()) {
                        qPrime[state.getX()][state.getY()][direction.ordinal()]
                                = mapGrid[state.getX()][state.getY()];
                    }*/
                    // break;
                    // Randomly select the valid state and non terminal state
                    state = startPosition;
                    //state = getRandomStateFromGrid(rowSize, colSize);
                    // Set action as null
                    a = null;
                } else {
                    // Select action 'a'
                    a = getAction(state);
                    Position nextState = getNextState(state, a);
                    // Debug
                    //System.out.println("Action " + a + " State " + state + " nextState " + nextState);
                    // Update TD-Q for current state
                    qPrime[state.getX()][state.getY()][a.ordinal()]
                            = qPrime[state.getX()][state.getY()][a.ordinal()]
                              + alpha * ( mapGrid[state.getX()][state.getY()]
                                         + ( MDPConstant.GAMMA * getMaxQAPrime(nextState) )
                                         - qPrime[state.getX()][state.getY()][a.ordinal()]
                                        );
                    // Increment count for action for current state
                    n[state.getX()][state.getY()][a.ordinal()] += 1;
                    // Update the variable
                    state = nextState;
                }
            } // End of trail

            // Calculate the convergence value
            delta = getConvergenceValue(q,qPrime);
            // Calculate UPrime value using qPrime
            calculateUPrime();
            //displayUtilityPerIteration(this.uPrime);
            // Calculate RMSE between expected and true utility
            calculateRMSE(iterationCount);
            // Increment the iteration count
            iterationCount += 1;

        } while (delta >= threshold);
    }

    private Position getRandomStateFromGrid(int rowSize, int colSize) {
        Random randomState = new Random();
        Position state = new Position(randomState.nextInt(rowSize), randomState.nextInt(colSize));
        while(!isValid(state) || isTerminalState(state)) {
            state = new Position(randomState.nextInt(rowSize), randomState.nextInt(colSize));
        }
        return state;
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

    private boolean isTerminalState(Position child) {
        return terminalState.contains(child);
    }

    private void cloneQPrime(float [][][]q, float [][][] qPrime) {
        int rowSize = uPrime.length;
        int colSize = uPrime[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                for (int direction = 0; direction < MDPConstant.MAXIMUM_NEXT_STATE; direction++) {
                    q[row][col][direction] = qPrime[row][col][direction];
                }
            }
        }
    }

    private float getConvergenceValue(float [][][]q, float [][][] qPrime) {
        int rowSize = uPrime.length;
        int colSize = uPrime[0].length;
        float maxDiff = - Float.MAX_VALUE;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                for (int direction = 0; direction < MDPConstant.MAXIMUM_NEXT_STATE; direction++) {
                    float diff = Math.abs(qPrime[row][col][direction] - q[row][col][direction]);
                    if (diff > maxDiff) {
                        maxDiff = diff;
                    }
                } // End Direction
            } // End Column
        } // End Row
        return maxDiff;
    }

    private Direction getAction(Position state) {
        Direction optimalAction = null;
        float maxValue = - Float.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            float currentValue = getExplorationFunctionValue(
                                    qPrime[state.getX()][state.getY()][direction.ordinal()]
                                    , n[state.getX()][state.getY()][direction.ordinal()] );
            if (currentValue > maxValue) {
                maxValue = currentValue;
                optimalAction = direction;
            }
        }
        return optimalAction;
    }

    private float getExplorationFunctionValue(float u, float n) {
        return (n < MDPConstant.Ne) ? (float)Math.random() : u;
        //return (n < MDPConstant.Ne) ? 10 : u;
    }

    private float getMaxQAPrime(Position nextState) {
        // Return the maximum q value of next state for given action a
        float maxValue = - Float.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            maxValue = Math.max(qPrime[nextState.getX()][nextState.getY()][direction.ordinal()]
                                , maxValue);
        }
        return maxValue;
    }

    private Position getNextState(Position currState, Direction a) {
        Position nextState = null;
        switch(a) {
            case UP :
                // Set the optimal action for current state as UP
                this.optimalPolicy[currState.getX()][currState.getY()] = 'U';
                nextState = isValid((currState.getX() - 1),currState.getY())
                                ? new Position((currState.getX() - 1),currState.getY())
                                : currState;
                break;
            case RIGHT :
                // Set the optimal action for current state as RIGHT
                this.optimalPolicy[currState.getX()][currState.getY()] = 'R';
                nextState = isValid(currState.getX(),(currState.getY() + 1))
                                ? new Position(currState.getX(),(currState.getY() + 1))
                                : currState;
                break;
            case DOWN :
                // Set the optimal action for current state as DOWN
                this.optimalPolicy[currState.getX()][currState.getY()] = 'D';
                nextState = isValid((currState.getX() + 1),currState.getY())
                                ? new Position((currState.getX() + 1),currState.getY())
                                : currState;
                break;
            case LEFT :
                // Set the optimal action for current state as LEFT
                this.optimalPolicy[currState.getX()][currState.getY()] = 'L';
                nextState = isValid(currState.getX(),(currState.getY() - 1))
                                ? new Position(currState.getX(),(currState.getY() - 1))
                                : currState;
                break;
        }
        return nextState;
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

    private void initializeOptimalPolicy(Set<Position> terminalState) {
        int rowSize = this.optimalPolicy.length;
        int colSize = this.optimalPolicy[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                if (terminalState.contains(new Position(row, col))) {
                    this.optimalPolicy[row][col] = '-';
                } else if (mapGrid[row][col] == 0) {
                    this.optimalPolicy[row][col] = 'W';
                } else {
                    this.optimalPolicy[row][col] = 'I';
                }
            }
        }
    }

    private void displayPolicy(char[][] optimalPolicy) {
        int rowSize = optimalPolicy.length;
        int colSize = optimalPolicy[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                System.out.print(optimalPolicy[row][col] + "\t");
            }
            System.out.println();
        }
    }

    private void calculateUPrime() {
        for (int row = 0; row < uPrime.length; row++) {
            for (int col = 0; col < uPrime[0].length; col++) {
                if (isValid(row,col) && !isTerminalState(new Position(row,col))) {
                    float maxQPrime = - Float.MAX_VALUE;
                    for (Direction direction : Direction.values()) {
                        if (qPrime[row][col][direction.ordinal()] > maxQPrime) {
                            maxQPrime = qPrime[row][col][direction.ordinal()];
                            this.optimalPolicy[row][col] = getDirectionCharacter(direction);
                        }
                    }
                    // Set the uPrime as max Q(up,right,down,left)
                    this.uPrime[row][col] = maxQPrime;
                }
            }
        }
    }

    private void calculateRMSE(int iteration) {
        int numOfStates = 0;
        float utilityDiff = 0.0f;
        for (int row = 0; row < this.uPrime.length; row++) {
            for (int col = 0; col < this.uPrime[0].length; col++) {
                if (isValid(row,col) && !isTerminalState(new Position(row,col))) {
                    utilityDiff += (float) Math.pow( (this.uPrime[row][col] - this.U[row][col]), 2);
                }
                numOfStates += 1;
            }
        }
        double rmse = Math.sqrt((utilityDiff/numOfStates));
        System.out.println(iteration + "," + rmse);
    }

    private char getDirectionCharacter(Direction direction) {
        char ch = ' ';
        switch(direction) {
            case UP :
                // Set the optimal action for current state as UP
                ch = 'U';
                break;
            case RIGHT :
                // Set the optimal action for current state as RIGHT
                ch = 'R';
                break;
            case DOWN :
                // Set the optimal action for current state as DOWN
                ch = 'D';
                break;
            case LEFT :
                // Set the optimal action for current state as LEFT
                ch = 'L';
                break;
        }
        return ch;
    }

    private static void displayUtilityPerIteration(float [][] utility_per_iteration) {
        int rowSize = utility_per_iteration.length;
        int colSize = utility_per_iteration[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                System.out.print(utility_per_iteration[row][col] + ",");
            }
        }
        System.out.println();
    }

    public char[][] getOptimalPolicy() {
        return optimalPolicy;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public float[][][] getqPrime() {
        return qPrime;
    }

    public float[][] getuPrime() {
        return uPrime;
    }
}
