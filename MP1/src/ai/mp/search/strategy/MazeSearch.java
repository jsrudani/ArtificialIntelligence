package ai.mp.search.strategy;

import java.util.Scanner;

/**
 * It is main class. It is responsible to delegate the call to respective search strategy based on user input.
 * It takes following 3 argument
 * <pre>
 * 1. Maze file name
 * 2. Type of Search strategy
 * 3. Select the additional option from given list to run selected search strategies with additional options
 * </pre>
 * 
 * @author rudani2
 *
 */
public class MazeSearch {

    private static SearchOperation searchStrategy;

    public static void main(String[] args) {
        // Validate if file name is passed as an argument
        if (args.length != 1) {
            printUsage();
        }
        String mazePattern = args[0];
        try (Scanner input = new Scanner ( System.in )) {
            int totalUserEnteredInput = 0;
            // Get the type of search strategy
            System.out.println(MazeConstant.SEARCH_STRATEGY);
            int searchStrategyChoice = input.nextInt();
            if (isValidate(searchStrategyChoice)) {
                totalUserEnteredInput += 1;
            } else {
                throw new IllegalArgumentException("Please enter valid search startegy");
            }
            System.out.println("User entered searchStrategyChoice " + searchStrategyChoice);
            // Display the additional options
            System.out.println(MazeConstant.SEARCH_WITH_ADDITIONAL_OPTION);
            int searchStrategyWithAdditionalParameter = input.nextInt();
            System.out.println("User entered searchStrategyWithAdditionalParameter " + searchStrategyWithAdditionalParameter);
            if (isValidate(searchStrategyWithAdditionalParameter)) {
                totalUserEnteredInput += 1;
            } else {
                throw new IllegalArgumentException("Please enter valid additional options");
            }
            // Validate number of arguments
            if (totalUserEnteredInput != 2) {
                printUsage();
            }

            // Get the additional options flag set based on user input
            Preprocessing.processUserInput(searchStrategyWithAdditionalParameter);
            // Preprocess the maze to prepare maze for search startegy
            Preprocessing.preprocessFile(mazePattern);

            // Delegate request to respective search strategy
            delegateToRespectiveSearchStrategy(searchStrategyChoice);
            System.out.println("Displaying Maze search statistics for "
                            + searchStrategy.getSearchStrategyName());
            displayCharArray(searchStrategy.getSolutionMaze());
            System.out.println("Step cost :" + searchStrategy.getStepCost());
            System.out.println("Nodes expanded :" + searchStrategy.getNodesExpanded());
            System.out.println("Solution Cost :" + searchStrategy.getSolutionCost());
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            System.exit(-1);
        }
    }

    /**
     * It is used to print the usage information of about how to run the program.
     */
    private static void printUsage() {
        String usage = "Please enter the following argument. \n"
                + "1. File name which contain Maze \n"
                + "2. Select the type of Search startegy from the displayed list \n"
                + "3. Select the additional option from displayed list to run with selected search strategy";
        System.out.println(usage);
    }

    /**
     * It is used to validate the user input. User input must be in range from 1 to 4 (inclusive).
     * @param userInput
     * @return boolean
     */
    private static boolean isValidate(int userInput) {
        if ( (userInput > 0 && userInput <= MazeConstant.EXPECTED_USER_SEARCH_TYPE_INPUT_COUNT) 
        || (userInput > 0 && userInput <= MazeConstant.EXPECTED_USER_ADDITIONAL_INPUT_COUNT) ) {
            return true;
        }
        return false;
    }

    private static void delegateToRespectiveSearchStrategy(int userInput) throws IllegalArgumentException {
        switch (userInput) {
            case 1:
                searchStrategy = new BFS(Preprocessing.getPreprocessedMaze(),
                                        Preprocessing.getSolutionMatrix());
                break;
            case 2:
                searchStrategy = new DFS(Preprocessing.getPreprocessedMaze(),
                                        Preprocessing.getSolutionMatrix());
                break;
            case 3:
                searchStrategy = new GreedyBestFirstSearch(Preprocessing.getPreprocessedMaze(),
                                        Preprocessing.getSolutionMatrix());
                break;
            case 4:
                searchStrategy = new AStar(Preprocessing.getPreprocessedMaze(),
                                        Preprocessing.getSolutionMatrix());
                break;
            default :
                System.out.println("Wrong choice!!!");
                break;
        }
        if (searchStrategy != null && (Preprocessing.isPathFind() 
                || (Preprocessing.isGhost() && Preprocessing.getGhostPosition() != null)
                || Preprocessing.isPenalty()
                || Preprocessing.isMultipleGoal())) {
            System.out.println("Total goals " + Preprocessing.getGoalSet().size());
            searchStrategy.findPath();
        } else {
            throw new IllegalArgumentException("Invalid choice for search strategy");
        }
    }

    private static void displayIntArray(int [][] array) {
        int row = array.length;
        int col = array[0].length;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                System.out.print(array[i][j]);
            }
            System.out.println();
        }
    }
    private static void displayCharArray(char [][] array) {
        int row = array.length;
        int col = array[0].length;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                System.out.print(array[i][j]);
            }
            System.out.println();
        }
    }
}
