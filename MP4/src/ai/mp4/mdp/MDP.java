package ai.mp4.mdp;

/**
 * It is used to run the Markov Decision Process over map grid and find
 * optimal policy.
 * 
 * @author rudani2
 *
 */
public class MDP {
    public static void main(String[] args) {

        // It represent the GRID map
        float [][] mapGrid = {
                {-0.04f, -1,     -0.04f, -0.04f, -0.04f, -0.04f}
               ,{-0.04f, -0.04f, -0.04f, 0,      -1,     -0.04f}
               ,{-0.04f, -0.04f, -0.04f, 0,      -0.04f,  3}
               ,{-0.04f, -0.04f, -0.04f, 0,      -0.04f, -0.04f}
               ,{-0.04f, -0.04f, -0.04f, -0.04f, -0.04f, -0.04f}
               ,{1,      -1,     -0.04f, 0,      -1,     -1}
        };
        // Represent start position in grid map. It also has reward of -0.04
        Position startPosition = new Position(3,1);

        // Find the optimal policy for given grid using value iteration
        ValueIteration valueIteration = new ValueIteration(mapGrid, startPosition);
        valueIteration.findOptimalPolicy();

        // Print the statistics
        System.out.println("Total iteration " + valueIteration.getIterationCount());
        System.out.println("Policy "); 
        displayPolicy(valueIteration.getOptimalPolicy());
        System.out.println("Utility ");
        displayUPrime(valueIteration.getuPrime());
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
                System.out.print(Math.floor(uPrime[row][col]) + "\t");
            }
            System.out.println();
        }
    }
}
