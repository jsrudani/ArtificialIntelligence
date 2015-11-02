package ai.mp3.digit.classification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class contains utility functions which are useful for calculations.
 * 
 * @author rudani2
 *
 */
public class Utilities {

    public static List<Integer> readLabelFile(String filename) {
        List<Integer> labels = new ArrayList<Integer>();
        try {
            // Read the label filename
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    labels.add(Integer.parseInt(line));
                }
            }
        } catch (Exception e) {
            System.out.println("There is error while reading label file " + e.getMessage());
        }
        return labels;
    }

    /**
     * It is used to get integer value for given character.
     * 
     * <pre>
     * ' '      -> 0
     * '+'/'#'  -> 1
     * </pre>
     * 
     * @param ch
     * @return Integer
     */
    public static int getIntegerForCharacter(char ch) {
        if (ch == ' ') {
            return 0;
        } else if (ch == '+' || ch == '#') {
            return 1;
        }
        return -1;
    }

}
