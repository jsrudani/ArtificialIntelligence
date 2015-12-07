package ai.mp4.mdp;

import java.util.Set;

/**
 * It is used to implement Model based Q-Learning. It is known as Model based
 * as probability P(s'|s,a) of next state for given action 'a' is not random. It
 * uses transition model and calculate optimal next state.
 * 
 * In this Pizza delivery map, we need to consider different transition model based
 * on either we have/have not pizza/ingredients.
 * 
 * @author rudani2
 *
 */
public class ModelBasedQLearning {

    private final float [][] pizzaGridMap;
    private final Set<Position> studentPosition;
    private final Set<Position> pizzaStorePosition;
    private final Set<Position> groceryStorePosition;
    private final Position startPosition;
    // qPrime is used to hold new calculated q values
    private float [][][] qPrime;
    // qPrime is used to hold previous calculated q values
    private float [][][] q;
    // n is used to hold number of times action 'a' is performed on state 's'
    private int [][][] n;
    // It holds the expected utility calculated based on Q
    private float [][] uPrime;
    // It holds the previous calculated utility values
    private float [][] u;
    // It determines if you have ingredients
    public final boolean hadPizza;
    // It determines if you have pizza
    public final boolean hadIngredients;
    private char[][] optimalPolicy;
    private boolean isIngredients;
    private boolean isPizza;
    private int iterationCount = 0;

    ModelBasedQLearning(
              float [][] pizzaGridMap
            , Position startPosition
            , Set<Position> studentPosition
            , Set<Position> pizzaStorePosition
            , Set<Position> groceryStorePosition
            , boolean hadIngredients
            , boolean hadPizza) {

        this.pizzaGridMap = pizzaGridMap;
        this.startPosition = startPosition;
        this.studentPosition = studentPosition;
        this.pizzaStorePosition = pizzaStorePosition;
        this.groceryStorePosition = groceryStorePosition;
        this.qPrime = new float [pizzaGridMap.length][pizzaGridMap[0].length][MDPConstant.MAXIMUM_NEXT_STATE];
        this.uPrime = new float [pizzaGridMap.length][pizzaGridMap[0].length];
        this.u = new float [pizzaGridMap.length][pizzaGridMap[0].length];
        this.q = new float [pizzaGridMap.length][pizzaGridMap[0].length][MDPConstant.MAXIMUM_NEXT_STATE];
        this.n = new int [pizzaGridMap.length][pizzaGridMap[0].length][MDPConstant.MAXIMUM_NEXT_STATE];
        this.hadIngredients = hadIngredients;
        this.hadPizza = hadPizza;
        this.optimalPolicy = new char[pizzaGridMap.length][pizzaGridMap[0].length];
        initializeOptimalPolicy();

    }

    public void findOptimalPolicy() {

        float threshold = MDPConstant.EPSILON * (1 - MDPConstant.GAMMA)/MDPConstant.GAMMA;
        float delta = 0.0f;
        Position state = startPosition;
        int rowSize = optimalPolicy.length;
        int colSize = optimalPolicy[0].length;
        this.isIngredients = this.hadIngredients;
        this.isPizza = this.hadPizza;

        do {

            delta = 0.0f;
            Direction a = null;
            // Clone qPrime into q
            cloneQPrime(q,qPrime);

            // Learn the model for number of trails
            for (int trailNo = 0; trailNo < MDPConstant.NUMBER_OF_TRIAL; trailNo++) {

                // Select action 'a'
                a = getAction(state);
                Position nextState = getNextState(state, a);
                // Update TD-Q for current state
                qPrime[state.getX()][state.getY()][a.ordinal()] = getReward(state) + MDPConstant.GAMMA * (getModelQFactor(state, a));
                // Update the variables
                updateControlVariable(state);
                // Increment count for action for current state
                n[state.getX()][state.getY()][a.ordinal()] += 1;
                // Update the variable
                state = nextState;
            } // End of trail

            // Calculate the convergence value
            delta = getConvergenceValue(q,qPrime);
            // Calculate UPrime value using qPrime
            calculateUPrime();
            // Reset control variables
            isIngredients = this.hadIngredients;
            isPizza = this.hadPizza;
            state = startPosition;
            // Increment the iteration count
            iterationCount += 1;

        } while (delta >= threshold);
    }

    private void initializeOptimalPolicy() {

        int rowSize = this.optimalPolicy.length;
        int colSize = this.optimalPolicy[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                Position tempPos = new Position(row, col);
                if (pizzaGridMap[row][col] == 0) {
                    this.optimalPolicy[row][col] = 'W';
                } else if (studentPosition.contains(tempPos)) {
                    this.optimalPolicy[row][col] = 'S';
                } else if (pizzaStorePosition.contains(tempPos)) {
                    this.optimalPolicy[row][col] = 'P';
                } else if (groceryStorePosition.contains(tempPos)) {
                    this.optimalPolicy[row][col] = 'G';
                } else {
                    this.optimalPolicy[row][col] = 'I';
                }
            }
        }

    }

    private void cloneQPrime(float [][][] q, float [][][] qPrime) {
        int rowSize = qPrime.length;
        int colSize = qPrime[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                for (int direction = 0; direction < MDPConstant.MAXIMUM_NEXT_STATE; direction++) {
                    q[row][col][direction] = qPrime[row][col][direction];
                }
            }
        }
    }

    private Direction getAction(Position state) {
        Direction optimalAction = null;
        float maxValue = - Float.MAX_VALUE;
        float [] probabilities = getProbabilityOutcome(state);
        for (Direction direction : Direction.values()) {
            float currentValue = getExplorationFunctionValue(
                                      getMaxExpectedUtility(state, direction, probabilities)
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
    }

    private float getMaxExpectedUtility(Position state, Direction action, float [] probabilities) {

        float utility = 0.0f;
        int currentX = state.getX();
        int currentY = state.getY();
        float probabilityOutcome = probabilities[0];
        float probabilityDirection = probabilities[1];
        float probabilitySameState = probabilities[2];

        switch(action) {
            case UP :
                // Set the optimal action for current state as UP
                utility += probabilityOutcome * getUtility((currentX - 1), currentY, state);
                utility += probabilityDirection * getUtility(currentX, (currentY - 1), state);
                utility += probabilityDirection * getUtility(currentX, (currentY + 1), state);
                utility += probabilitySameState * getUtility(currentX, currentY, state);
                break;
            case RIGHT :
                // Set the optimal action for current state as RIGHT
                utility += probabilityOutcome * getUtility(currentX, (currentY + 1), state);
                utility += probabilityDirection * getUtility((currentX - 1), currentY, state);
                utility += probabilityDirection * getUtility((currentX + 1), currentY, state);
                utility += probabilitySameState * getUtility(currentX, currentY, state);
                break;
            case DOWN :
                // Set the optimal action for current state as DOWN
                utility += probabilityOutcome * getUtility((currentX + 1), currentY, state);
                utility += probabilityDirection * getUtility(currentX, (currentY - 1), state);
                utility += probabilityDirection * getUtility(currentX, (currentY + 1), state);
                utility += probabilitySameState * getUtility(currentX, currentY, state);
                break;
            case LEFT :
                // Set the optimal action for current state as LEFT
                utility += probabilityOutcome * getUtility(currentX, (currentY - 1), state);
                utility += probabilityDirection * getUtility((currentX - 1), currentY, state);
                utility += probabilityDirection * getUtility((currentX + 1), currentY, state);
                utility += probabilitySameState * getUtility(currentX, currentY, state);
                break;
        }
        return utility;
    }

    private float getUtility(int child_x, int child_y, Position parent) {
        return (isValid(child_x,child_y) ? u[child_x][child_y] : u[parent.getX()][parent.getY()]);
    }

    private boolean isValid(int x, int y) {
        int rows = pizzaGridMap.length;
        int cols = pizzaGridMap[0].length;
        // Check if it is valid position
        if ( (x >= 0 && x < rows) && (y >= 0 && y < cols) ) {
            // Check if it is Wall
            if (pizzaGridMap[x][y] == 0) {
                return false;
            }
            return true;
        }
        return false;
    }

    private float [] getProbabilityOutcome(Position state) {

        float probabilityOutcome = 0.0f;
        float probabilityDirection = 0.0f;
        float probabilitySameState = 0.0f;
        float [] probabilities = new float[3];

        // If state is Grocery
        if (groceryStorePosition.contains(state)) {
            // No ingredients and No Pizza
            if (!isIngredients && !isPizza) {
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            } else if (!isIngredients && isPizza) {
                // No ingredients and Yes Pizza
                probabilityOutcome = MDPConstant.INTENDED_OUTCOME_PROBABILITY_CARRY_PIZZA;
                probabilityDirection = MDPConstant.INTENDED_DIRECTION_PROBABILITY_CARRY_PIZZA;
                probabilitySameState = MDPConstant.INTENDED_SAMESTATE_PROBABILITY_CARRY_PIZZA;
            } else if (isIngredients && !isPizza) {
                // Yes ingredients and No pizza
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            } else {
                // Yes ingredients and yes pizza
                probabilityOutcome = MDPConstant.INTENDED_OUTCOME_PROBABILITY_CARRY_PIZZA;
                probabilityDirection = MDPConstant.INTENDED_DIRECTION_PROBABILITY_CARRY_PIZZA;
                probabilitySameState = MDPConstant.INTENDED_SAMESTATE_PROBABILITY_CARRY_PIZZA;
            }
        } else if (pizzaStorePosition.contains(state)) {
            // If state is Pizza
            // No ingredients and No Pizza
            if (!isIngredients && !isPizza) {
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            } else if (!isIngredients && isPizza) {
                // No ingredients and Yes Pizza
                probabilityOutcome = MDPConstant.INTENDED_OUTCOME_PROBABILITY_HAVE_PIZZA;
                probabilityDirection = MDPConstant.INTENDED_DIRECTION_PROBABILITY_HAVE_PIZZA;
                probabilitySameState = MDPConstant.INTENDED_SAMESTATE_PROBABILITY_HAVE_PIZZA;
            } else if (isIngredients && !isPizza) {
                // Yes ingredients and No pizza
                probabilityOutcome = MDPConstant.INTENDED_OUTCOME_PROBABILITY_CARRY_PIZZA;
                probabilityDirection = MDPConstant.INTENDED_DIRECTION_PROBABILITY_CARRY_PIZZA;
                probabilitySameState = MDPConstant.INTENDED_SAMESTATE_PROBABILITY_CARRY_PIZZA;
            } else {
                // Yes ingredients and yes pizza
                probabilityOutcome = MDPConstant.INTENDED_OUTCOME_PROBABILITY_HAVE_PIZZA;
                probabilityDirection = MDPConstant.INTENDED_DIRECTION_PROBABILITY_HAVE_PIZZA;
                probabilitySameState = MDPConstant.INTENDED_SAMESTATE_PROBABILITY_HAVE_PIZZA;
            }
        } else if (studentPosition.contains(state)) {
            // If state is student position
            // No ingredients and No Pizza
            if (!isIngredients && !isPizza) {
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            } else if (!isIngredients && isPizza) {
                // No ingredients and Yes Pizza
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            } else if (isIngredients && !isPizza) {
                // Yes ingredients and No pizza
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            } else {
                // Yes ingredients and yes pizza
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            }
        } else {
            // Normal state
            // No ingredients and No Pizza
            if (!isIngredients && !isPizza) {
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            } else if (!isIngredients && isPizza) {
                // No ingredients and Yes Pizza
                probabilityOutcome = MDPConstant.INTENDED_OUTCOME_PROBABILITY_CARRY_PIZZA;
                probabilityDirection = MDPConstant.INTENDED_DIRECTION_PROBABILITY_CARRY_PIZZA;
                probabilitySameState = MDPConstant.INTENDED_SAMESTATE_PROBABILITY_CARRY_PIZZA;
            } else if (isIngredients && !isPizza) {
                // Yes ingredients and No pizza
                probabilityOutcome = MDPConstant.INTENDED_SPECIFIED_OUTCOME_PROBABILITY;
                probabilityDirection = MDPConstant.INTENDED_SPECIFIED_DIRECTION_PROBABILITY;
                probabilitySameState = 0.0f;
            } else {
                // Yes ingredients and yes pizza
                probabilityOutcome = MDPConstant.INTENDED_OUTCOME_PROBABILITY_CARRY_PIZZA;
                probabilityDirection = MDPConstant.INTENDED_DIRECTION_PROBABILITY_CARRY_PIZZA;
                probabilitySameState = MDPConstant.INTENDED_SAMESTATE_PROBABILITY_CARRY_PIZZA;
            }
        } // End of if-else
        // Store the output
        probabilities[0] = probabilityOutcome;
        probabilities[1] = probabilityDirection;
        probabilities[2] = probabilitySameState;

        return probabilities;
    }

    private Position getNextState(Position currState, Direction a) {
        Position nextState = null;
        switch(a) {
            case UP :
                // Set the optimal action for current state as UP
                nextState = isValid((currState.getX() - 1),currState.getY())
                                ? new Position((currState.getX() - 1),currState.getY())
                                : currState;
                break;
            case RIGHT :
                // Set the optimal action for current state as RIGHT
                nextState = isValid(currState.getX(),(currState.getY() + 1))
                                ? new Position(currState.getX(),(currState.getY() + 1))
                                : currState;
                break;
            case DOWN :
                // Set the optimal action for current state as DOWN
                nextState = isValid((currState.getX() + 1),currState.getY())
                                ? new Position((currState.getX() + 1),currState.getY())
                                : currState;
                break;
            case LEFT :
                // Set the optimal action for current state as LEFT
                nextState = isValid(currState.getX(),(currState.getY() - 1))
                                ? new Position(currState.getX(),(currState.getY() - 1))
                                : currState;
                break;
        }
        return nextState;
    }

    private float getModelQFactor(Position state, Direction optimalAction) {

        float utility = 0.0f;
        float [] probabilities = getProbabilityOutcome(state);
        int currentX = state.getX();
        int currentY = state.getY();
        float probabilityOutcome = probabilities[0];
        float probabilityDirection = probabilities[1];
        float probabilitySameState = probabilities[2];

        switch(optimalAction) {
            case UP :
                // Set the optimal action for current state as UP
                utility += probabilityOutcome * getMaxQAPrime((currentX - 1), currentY);
                utility += probabilityDirection * getMaxQAPrime(currentX, (currentY - 1));
                utility += probabilityDirection * getMaxQAPrime(currentX, (currentY + 1));
                utility += probabilitySameState * getMaxQAPrime(currentX, currentY);
                break;
            case RIGHT :
                // Set the optimal action for current state as RIGHT
                utility += probabilityOutcome * getMaxQAPrime(currentX, (currentY + 1));
                utility += probabilityDirection * getMaxQAPrime((currentX - 1), currentY);
                utility += probabilityDirection * getMaxQAPrime((currentX + 1), currentY);
                utility += probabilitySameState * getMaxQAPrime(currentX, currentY);
                break;
            case DOWN :
                // Set the optimal action for current state as DOWN
                utility += probabilityOutcome * getMaxQAPrime((currentX + 1), currentY);
                utility += probabilityDirection * getMaxQAPrime(currentX, (currentY - 1));
                utility += probabilityDirection * getMaxQAPrime(currentX, (currentY + 1));
                utility += probabilitySameState * getMaxQAPrime(currentX, currentY);
                break;
            case LEFT :
                // Set the optimal action for current state as LEFT
                utility += probabilityOutcome * getMaxQAPrime(currentX, (currentY - 1));
                utility += probabilityDirection * getMaxQAPrime((currentX - 1), currentY);
                utility += probabilityDirection * getMaxQAPrime((currentX + 1), currentY);
                utility += probabilitySameState * getMaxQAPrime(currentX, currentY);
                break;
        }
        return utility;
    }

    private float getMaxQAPrime(int x, int y) {
        // Return the maximum q value of next state for given action a
        float maxValue = - Float.MAX_VALUE;
        for (Direction direction : Direction.values()) {
            maxValue = Math.max(qPrime[x][y][direction.ordinal()]
                                , maxValue);
        }
        return maxValue;
    }

    private float getReward(Position state) {
        // If state is student
        if (studentPosition.contains(state)) {
            if (isPizza) {
                return 5.0f;
            }
        } else if (groceryStorePosition.contains(state)
                || pizzaStorePosition.contains(state)) {
            return 0.0f;
        }
        return -0.1f;
    }

    private void updateControlVariable(Position state) {
        // If state is Grocery
        if (groceryStorePosition.contains(state)) {
            // No ingredients and No Pizza
            if (!isIngredients && !isPizza) {
                // Set the ingredients
                isIngredients = true;
            } else if (!isIngredients && isPizza) {
                // No ingredients and Yes Pizza
                // Set the ingredients
                isIngredients = true;
            }
        } else if (pizzaStorePosition.contains(state)) {
            // If state is Pizza
            // No ingredients and No Pizza
            if (isIngredients && !isPizza) {
                // Set isPizza
                isPizza = true;
                // Set isIngredient as false
                isIngredients = false;
            }
        } else if (studentPosition.contains(state)) {
            // If state is student position
            // No ingredients and No Pizza
            if (!isIngredients && isPizza) {
                // No ingredients and Yes Pizza
                // Set isPizza as false
                isPizza = false;
            } else if (isIngredients && isPizza) {
                // Yes ingredients and Yes pizza
                // Set isPizza as false
                isPizza = false;
            }
        } // End of if-else
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

    private void calculateUPrime() {
        for (int row = 0; row < uPrime.length; row++) {
            for (int col = 0; col < uPrime[0].length; col++) {
                if (isValid(row,col)
                        && !studentPosition.contains(new Position(row,col))
                        && !groceryStorePosition.contains(new Position(row,col))
                        && !pizzaStorePosition.contains(new Position(row,col))) {
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

    public float[][] getuPrime() {
        return uPrime;
    }

    public char[][] getOptimalPolicy() {
        return optimalPolicy;
    }

    public int getIterationCount() {
        return iterationCount;
    }

}
