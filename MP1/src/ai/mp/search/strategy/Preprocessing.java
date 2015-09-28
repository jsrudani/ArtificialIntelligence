package ai.mp.search.strategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * This class is used to preprocess the maze file. It converst the maze file in following format
 * 
 * <pre>
 * [-1 -1 -1 -1
 *  -1  3  3 -1
 *  -1  1  0 -1]
 *  -1 represent Wall
 *  0 represent Start state
 *  1 represent Goal State
 *  2 represent Ghost
 *  3 represent Path
 *  4 represent visited
 * </pre>
 * 
 * @author rudani2
 *
 */
public class Preprocessing {

    private static int [][] preprocessedMaze;
    private static char[][] solutionMatrix;
    private static Set<Position> goalSet;
    private static Position startPosition;
    private static Position ghostPosition;
    private static Position goalPosition;

    private static boolean isInitialized = false;
    private static int rowCount = 0;

    private static boolean isPenalty = false;
    private static boolean isGhost = false;
    private static boolean isMultipleGoal = false;
    private static boolean isOurHeuristic = false;
    private static boolean isPathFind = false;

    public static void processUserInput (int userInput) {
        switch (userInput) {
            case 1:
                isPenalty = true;
                break;
            case 2:
                isGhost = true;
                break;
            case 3:
                isMultipleGoal = true;
                break;
            case 4:
                isOurHeuristic = true;
                break;
            case 5:
                isPathFind = true;
                break;
        }
    }

    /**
     * It is used to read file by line and process each line to populate the
     * maze and solution maze
     * 
     * @param file
     * @throws IOException
     */
    public static void preprocessFile(String file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file), Charset.defaultCharset());
        rowCount = lines.size();
        readFileByLine(new File(file));
    }

    /**
     * It used to read file by line
     * @param file
     * @throws IOException
     */
    private static void readFileByLine(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = null;
            int row = 0;
            while ((line = br.readLine()) != null) {
                if (!isInitialized) {
                    preprocessedMaze = new int[rowCount][line.length()];
                    solutionMatrix = new char[rowCount][line.length()];
                    goalSet = new HashSet<Position>();
                    isInitialized = true;
                }
                System.out.println(line);
                processLineAndPopulateMaze(row++, line);
            }
        }
    }

    /**
     * It is used to process each line and populate preprocessed Maze
     * and solution Maze. It interprets
     * <pre>
     * % as -1 for Wall
     * P as 0 for start position
     * . as 1 for Goal state
     * Ghost as 2
     * Path as 3
     * </pre>
     *
     * @param line
     */
    private static void processLineAndPopulateMaze(int row, String line) {
        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);
            switch (ch) {
                case '%' :
                    preprocessedMaze[row][i] = MazeConstant.WALL_MARKER;
                    solutionMatrix[row][i] = '%';
                    break;
                case 'P' :
                    preprocessedMaze[row][i] = MazeConstant.START_POSITION_MARKER;
                    solutionMatrix[row][i] = 'P';
                    startPosition = new Position(row, i, null, MazeConstant.DEFAULT_COST, 0L, MazeConstant.RIGHT_DIRECTION);
                    break;
                case '.' :
                    preprocessedMaze[row][i] = MazeConstant.GOAL_POSITION_MARKER;
                    solutionMatrix[row][i] = '.';
                    if (!isMultipleGoal()) {
                        goalPosition = new Position(row, i, null, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
                    } else {
                        // Collect all the goal position
                        goalSet.add( new Position(row, i, null, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION) );
                    }
                    break;
                case 'G' :
                    preprocessedMaze[row][i] = MazeConstant.GHOST_POSITION_MARKER;
                    solutionMatrix[row][i] = 'G';
                    if (isGhost()) {
                        ghostPosition = new Position(row, i, null, MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_COST, MazeConstant.RIGHT_DIRECTION);
                    }
                    break;
                case ' ' :
                case 'g' :
                    preprocessedMaze[row][i] = MazeConstant.PATH_MARKER;
                    solutionMatrix[row][i] = ' ';
                    break;
            }
            i += 1;
        }
    }

    public static boolean isPenalty() {
        return isPenalty;
    }

    public static boolean isGhost() {
        return isGhost;
    }

    public static boolean isMultipleGoal() {
        return isMultipleGoal;
    }

    public static boolean isPathFind() {
        return isPathFind;
    }

    public static boolean isOurHeuristic() {
        return isOurHeuristic;
    }

    public static int[][] getPreprocessedMaze() {
        return preprocessedMaze;
    }

    public static char[][] getSolutionMatrix() {
        return solutionMatrix;
    }

    public static Position getStartPosition() {
        return startPosition;
    }

    public static void setStartPosition(Position startPosition) {
        Preprocessing.startPosition = startPosition;
    }

    public static Position getGhostPosition() {
        return ghostPosition;
    }

    public static void setGhostPosition(Position ghostPosition) {
        Preprocessing.ghostPosition = ghostPosition;
    }

    public static Position getGoalPosition() {
        return goalPosition;
    }

    public static void setGoalPosition(Position goalPosition) {
        Preprocessing.goalPosition = goalPosition;
    }

    public static Set<Position> getGoalSet() {
        return goalSet;
    }
}
