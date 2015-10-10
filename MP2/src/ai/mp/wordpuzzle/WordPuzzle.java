package ai.mp.wordpuzzle;

/**
 * It is the main class for solving word puzzle game. It pre-process the
 * puzzle file and word list.
 * 
 * @author rudani2
 *
 */
public class WordPuzzle {

    public static void main(String[] args) {
        // Validate if file name is passed as an argument
        if (args.length != 2) {
            printUsage();
        }
        // Get the puzzle file name
        String puzzleFilename = args[0];
        // Get the word list file name
        String worldListFilename = args[1];
        System.out.println("Puzzle file -> " + puzzleFilename);
        System.out.println("Word list file -> " + worldListFilename);

        // Start pre-processing the puzzle and word list
        Preprocessing preprocess = new Preprocessing(puzzleFilename, worldListFilename);
        // Pre process puzzle file
        preprocess.prepareIndexToCategoryMap();

        System.out.println("==========================================");
        System.out.println("After preparing Index Map ");
        System.out.println(Preprocessing.getIndexToCategoryMap());
        System.out.println("==========================================");

        // Pre process word list
        preprocess.prepareCategoryToLetterWordMap();

        System.out.println("==========================================");
        System.out.println("After preparing Category Map ");
        System.out.println(preprocess.getCategoryToLetterWordMap());
        System.out.println("==========================================");

        // Solve the word puzzle
        SolveWordPuzzle solve = new SolveWordPuzzle(preprocess.getCategoryToLetterWordMap()
                , preprocess.getOutputArraySize()
                , preprocess.getCategoryToWordsMap());

        // Solve by Letter based assignment
        solve.solveByLetterBasedAssignment();
    }

    /**
     * It is used to print the usage information of about how to run the program.
     */
    private static void printUsage() {
        String usage = "Please enter the following argument. \n"
                + "1. File name which contain Puzzle to solve \n"
                + "2. File name which contain valid word list for each category \n";
        System.out.println(usage);
    }
}
