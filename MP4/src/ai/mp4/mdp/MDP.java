package ai.mp4.mdp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * It is used to run the Markov Decision Process over map grid and find
 * optimal policy.
 * 
 * @author rudani2
 *
 */
public class MDP {

    public static void main(String[] args) {

        boolean isRewardTerminal = false;
        // It represent the GRID map
        float [][] mapGrid = {
                {-0.04f, -1,     -0.04f, -0.04f, -0.04f, -0.04f}
               ,{-0.04f, -0.04f, -0.04f, 0,      -1,     -0.04f}
               ,{-0.04f, -0.04f, -0.04f, 0,      -0.04f,  3}
               ,{-0.04f, -0.04f, -0.04f, 0,      -0.04f, -0.04f}
               ,{-0.04f, -0.04f, -0.04f, -0.04f, -0.04f, -0.04f}
               ,{1,      -1,     -0.04f, 0,      -1,     -1}
        };
        // It represent the GRID map for Pizza delivery system
        float [][] pizzaGridMap = {
                {0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0, 0}
               ,{0, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, 0}
               ,{0, -0.1f,  5.0f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,  5.0f, -0.1f, 0}
               ,{0, -0.1f, -0.1f, -0.1f,     0, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,     0, -0.1f, -0.1f, -0.1f, 0}
               ,{0,     0,     0,     0,     0, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,     0,     0,     0,     0, 0}
               ,{0, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,     0, -0.1f, -0.1f, -0.1f, 0}
               ,{0, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, 0}
               ,{0, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, 0}
               ,{0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,     0, 0}
        };
        // Represent start position in grid map. It also has reward of -0.04
        Position startPosition = new Position(3,1);
        // Store all the terminal state
        Set<Position> terminalState = new HashSet<Position>();
        terminalState.add(new Position(0,1));
        terminalState.add(new Position(1,4));
        terminalState.add(new Position(2,5));
        terminalState.add(new Position(5,0));
        terminalState.add(new Position(5,1));
        terminalState.add(new Position(5,4));
        terminalState.add(new Position(5,5));

        /*// Set isRewardTerminal based on which policy you want to run
        // If isRewardTerminal = false then we continue searching grid if even you reach
        // terminal state else you stop search in terminal state and add only the reward
        isRewardTerminal = true;
        // Find the optimal policy for given grid using value iteration
        ValueIteration valueIteration = new ValueIteration(mapGrid, startPosition, terminalState, isRewardTerminal);
        valueIteration.findOptimalPolicy();

        // Print the statistics
        System.out.println("Total iteration using MDP " + valueIteration.getIterationCount());
        System.out.println("Policy "); 
        displayPolicy(valueIteration.getOptimalPolicy());
        System.out.println("Utility ");
        displayUPrime(valueIteration.getuPrime());
        System.out.println();

        // Reinforcement learning
        TDQLearning qlearning = new TDQLearning(mapGrid, startPosition, terminalState
                , isRewardTerminal, valueIteration.getuPrime());
        qlearning.findOptimalPolicy();

        // Print the statistics
        System.out.println("\nTotal iteration using TD-Q learning " + qlearning.getIterationCount());
        System.out.println("Policy");
        displayPolicy(qlearning.getOptimalPolicy());
        System.out.println("Utility ");
        displayUPrime(qlearning.getuPrime());*/

        // Pizza delivery system

        startPosition = new Position(6,2);
        // Set below variables for different policy
        boolean hadPizza = false;
        boolean hadIngredients = false;
        // Store all the student position
        Set<Position> studentPosition = new HashSet<Position>();
        studentPosition.add(new Position(2,2));
        studentPosition.add(new Position(2,12));
        // Store pizza store location
        Set<Position> pizzaStorePosition = new HashSet<Position>();
        pizzaStorePosition.add(new Position(1,9));
        pizzaStorePosition.add(new Position(7,6));
        pizzaStorePosition.add(new Position(7,7));
        // Store Grocery store location
        Set<Position> groceryStorePosition = new HashSet<Position>();
        groceryStorePosition.add(new Position(6,2));
        groceryStorePosition.add(new Position(6,12));

        ModelBasedQLearning modelQLearning = 
                new ModelBasedQLearning(
                          pizzaGridMap
                        , startPosition
                        , studentPosition
                        , pizzaStorePosition
                        , groceryStorePosition
                        , hadIngredients
                        , hadPizza);
        modelQLearning.findOptimalPolicy();

        // Print the statistics
        System.out.println("\nTotal iteration using TD-Q learning " + modelQLearning.getIterationCount());
        System.out.println("Policy -> Ingredients: " + hadIngredients + " Pizza: " + hadPizza);
        displayPolicy(modelQLearning.getOptimalPolicy());
        System.out.println("Utility ");
        displayUPrime(modelQLearning.getuPrime());

    }

    private static void displayPolicy(char[][] optimalPolicy) {
        int rowSize = optimalPolicy.length;
        int colSize = optimalPolicy[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                System.out.print(optimalPolicy[row][col] + "\t");
            }
            System.out.println();
        }
    }

    private static void displayUPrime(float[][] uPrime) {
        int rowSize = uPrime.length;
        int colSize = uPrime[0].length;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < colSize; col++) {
                System.out.print(uPrime[row][col] + "\t");
            }
            System.out.println();
        }
    }

}
