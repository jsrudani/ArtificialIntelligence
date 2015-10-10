package ai.mp.wordpuzzle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


/**
 * This class is used to pre-process the puzzle file and word list. It is used to
 * prepare following map
 * <pre>
 *
 * 1. Map Name : indexToCategoryMap
 *    Structure :
 *         Key        |         Value
 *                               Key     |     Value
 *    =============================================
 *      Integer  | String |  Integer
 *    Example :
 *          4   |  emotion | 0 
 *    Explanation : At index position 4th holds 0th character of word belong
 *                  to emotion category
 *
 * 2. Map Name : categoryToLetterWordMap
 *    Structure :
 *         Key        |         Value
 *                               Key  |  Value
 *                                             Key   |   Value
 *    ======================================================
 *      String | Integer  |  String |   List\<words\>
 *      Example :
 *      emotion | 0 | A | [AID,AIT]
 *      Explanation : Category "emotion" contains words "[AID,AIT]" at index "0"
 *                    which starts with letter 'A'.
 *
 * </pre>
 * 
 * @author rudani2
 *
 */
public class Preprocessing {

    private static Map<Integer,Map<String,Integer>> IndexToCategoryMap =
            new HashMap<Integer,Map<String,Integer>>();

    private Map<String, Map<Integer, Map<Character, List<String> > > > CategoryToLetterWordMap =
            new HashMap<String, Map<Integer, Map<Character,List<String> > > >();

    private int outputArraySize;

    public final String puzzleFilename;
    public final String worldListFilename;

    Preprocessing(String puzzleFilename, String worldListFilename) {
        this.puzzleFilename = puzzleFilename;
        this.worldListFilename = worldListFilename;
    }

    /**
     * It is used to prepare IndexToCategoryMap
     */
    public void prepareIndexToCategoryMap() {
        try {
            // Read the Puzzle filename
            try (BufferedReader br = new BufferedReader(new FileReader(puzzleFilename))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    // check if delimiter : exist
                    if (line.indexOf(WordPuzzleConstant.COLON) != -1) {
                        System.out.println(line);
                        populateIndexToCategoryMap(line);
                    } else {
                        System.out.println(line);
                        outputArraySize = Integer.parseInt(line);
                        System.out.println("outputArraySize " + outputArraySize);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("There is error while reading puzzle file " + e.getMessage());
        }
    }

    /**
     * It is used to populate IndexToCategoryMap by splitting line as per COMMA and COLON
     * 
     * @param line
     */
    private void populateIndexToCategoryMap(String line) {
        Map<String,Integer> categoryToCharIndexPos;
        String [] lineSubpart = line.split(WordPuzzleConstant.COLON);
        String categoryName = lineSubpart[0];
        String [] position = lineSubpart[1].split(WordPuzzleConstant.COMMA);
        int charIndex = 0;
        while (charIndex < position.length) {
            int inputIdx = Integer.parseInt((position[charIndex]).trim());
            // Present
            if (IndexToCategoryMap.containsKey(inputIdx)) {
                IndexToCategoryMap.get(inputIdx).put(categoryName, charIndex);
            } else {
                // Not present
                categoryToCharIndexPos = new HashMap<String,Integer>();
                categoryToCharIndexPos.put(categoryName, charIndex);
                IndexToCategoryMap.put(inputIdx, categoryToCharIndexPos);
            }
            charIndex += 1;
        }
    }

    /**
     * It is used to prepare Category to Letter-Word Map.
     */
    public void prepareCategoryToLetterWordMap() {
        try {
            // Read the Word list filename
            try (BufferedReader br = new BufferedReader(new FileReader(worldListFilename))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    //System.out.println(line);
                    populateCategoryToLetterWordMap(line);
                    //System.out.println(CategoryToLetterWordMap);
                    //System.out.println("==================");
                }
            }
        } catch (Exception e) {
            System.out.println("There is error while reading word list file " + e.getMessage());
        }
    }

    /**
     * It is used to populate CategoryToLetterWordMap. It helps to get every character at
     * different position for each category.
     * 
     * @param line
     */
    private void populateCategoryToLetterWordMap(String line) {

        Map<Integer, Map<Character,List<String> > > indexToLetterWordMap =
                new HashMap<Integer, Map<Character,List<String>>>();
        Map<Character,List<String>> letterToWordListMap = null;
        List<String> wordList;

        String [] lineSubpart = line.split(WordPuzzleConstant.COLON);
        String categoryName = lineSubpart[0].trim();
        String [] words = lineSubpart[1].split(WordPuzzleConstant.COMMA);

        // Initialize the letter to word map
        for(int i = 0; i < WordPuzzleConstant.WORD_LEN;i++) {
            Map<Character,List<String>> letterToWordListEmptyMap = new HashMap<Character,List<String>>();
            indexToLetterWordMap.put(i, letterToWordListEmptyMap);
        }

        for (String word : words) {
            word = word.trim();
            //System.out.println("Word " + word);
            int charIdx = 0;
            while (charIdx < word.length()) {
                letterToWordListMap = indexToLetterWordMap.get(charIdx);
                char c = word.charAt(charIdx);
                if (letterToWordListMap.containsKey(c)) {
                    letterToWordListMap.get(c).add(word);
                } else {
                    wordList = new ArrayList<String>();
                    wordList.add(word);
                    letterToWordListMap.put(c, wordList);
                }
                charIdx += 1;
            }
        }
        // Store the index->letter->word combination into category map
        CategoryToLetterWordMap.put(categoryName, indexToLetterWordMap);
    }

    public static Map<Integer, Map<String, Integer>> getIndexToCategoryMap() {
        return IndexToCategoryMap;
    }

    public Map<String, Map<Integer, Map<Character, List<String>>>> getCategoryToLetterWordMap() {
        return CategoryToLetterWordMap;
    }

    public int getOutputArraySize() {
        return outputArraySize;
    }
}
