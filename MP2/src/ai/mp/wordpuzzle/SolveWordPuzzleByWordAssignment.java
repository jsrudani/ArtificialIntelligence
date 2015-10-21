package ai.mp.wordpuzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to solve word puzzle by word assignment.
 * 
 * @author rudani2
 *
 */
public class SolveWordPuzzleByWordAssignment {

    private Map<String, Map<Integer, Map<Character, List<String> > > > defaultCategoryToLetterWordMap;
    private Map<String, List<Integer>> categoryToIndexPositionMap;
    private Map<String, Set<String>> categoryToWordsMap;
    private Character [] outputArray;

    SolveWordPuzzleByWordAssignment(Map<String, Map<Integer, Map<Character, List<String> > > > categoryToLetterWordMap
            , final int outputArraySize
            , Map<String, Set<String>> categoryToWordsMap
            , Map<String, List<Integer>> categoryToIndexPositionMap) {
        this.defaultCategoryToLetterWordMap = categoryToLetterWordMap;
        this.categoryToWordsMap = categoryToWordsMap;
        this.categoryToIndexPositionMap = categoryToIndexPositionMap;
        this.outputArray = new Character[outputArraySize];
    }

    /**
     * It is used to solve word puzzle by word based assignment
     */
    public void solveByWordBasedAssignment() {
        // Get the order of the category
        List<String> orderedCategory = getOrderedCategory();
        // Call recursively solve()
        solve(null, 0, defaultCategoryToLetterWordMap, orderedCategory);
    }

    private void solve(Node parent, int orderedVariableIdx
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap
            , List<String> orderedCategory) {

        Character filter = null;
        int filterIdx = -1;

        // Check for base condition
        if (orderedVariableIdx < orderedCategory.size()) {

            // Get the category which needs to be assigned a value
            String category = orderedCategory.get(orderedVariableIdx);
            int charIndex = 0;

            // Check if there already exist character at variable position in output array
            for (int categoryIdx : categoryToIndexPositionMap.get(category)) {
                if (outputArray[(categoryIdx - 1)] != null) {
                    filter = outputArray[(categoryIdx - 1)];
                    filterIdx = charIndex;
                    break;
                }
                charIndex++;
            }

            // Debug
            /*System.out.println("====================================");
            System.out.println("category " + category);
            System.out.println("Filter char " + filter + " at index " + filterIdx);*/

            // Get the domain value for given category and filter
            Set<Node> intersectValues = getDomainVariable(category, filterIdx,defaultCategoryToLetterWordMap,filter);

            //System.out.println("Intersect values " + intersectValues);

            // Check if there exist any domain value
            if (intersectValues.isEmpty()) {
                // Backtrack print the path
                //System.out.println("Backtrack ");
                printPath(parent, true, outputArray);
            } else {
                // For each value assign each word to respective index position
                for (Node word : intersectValues) {
                    /*System.out.println("Output array b4 assigning word " + word + " for category " + category
                            + " is : \n" + Arrays.toString(outputArray));*/
                    word.setParent(parent);
                    // Add to visited set
                    word.setVisited(false);
                    // Check for consistency
                    if (!isConsistentAndFillOutputArray(word)) {
                        //System.out.println("Not consistent ");
                        printPath(word, true, outputArray);
                        continue;
                    }
                    // Fill the output array as per word as it is consistent
                    List<Integer> occupiedArrayPosition = fillOutPutArray(word);
                    /*System.out.println("Output array till now " + Arrays.toString(outputArray));
                    System.out.println("====================================");*/
                    // Recursively call to other index position
                    solve(word, (orderedVariableIdx + 1), defaultCategoryToLetterWordMap, orderedCategory);
                    // Remove the previous assignment
                    removePreviousAssignedWord(word, occupiedArrayPosition);
                }
            }
        } else {
            // Success path
            //System.out.println("Success path ");
            printPath(parent, false, outputArray);
        }
    }

    /**
     * It is used to perform consistency check. It checks whether value is aligned with already assigned value.
     * 
     * @param word
     * @return boolean
     */
    private boolean isConsistentAndFillOutputArray(Node word) {

        // Check the word assignment if it is valid with previous assignment
        String wordCategory = word.getCategory();
        String wordValue = word.getValue();
        List<Integer> wordIndexPosition = categoryToIndexPositionMap.get(wordCategory);
        int charIndex = 0;
        for (Integer index : wordIndexPosition) {
            if (outputArray[(index-1)] != null && outputArray[(index-1)] != wordValue.charAt(charIndex)) {
                return false;
            }
            charIndex += 1;
        }
        // Check for validity of word assignment
        return isValid(word) ? true : false;
    }

    /**
     * It is used to insert into output array. It returns the index position which are
     * occupied as per word
     * 
     * @param word
     * @return Collection of occupied position
     */
    private List<Integer> fillOutPutArray(Node word) {

        List<Integer> filledArrayPosition = new ArrayList<Integer>();
        String wordCategory = word.getCategory();
        String wordValue = word.getValue();
        List<Integer> wordIndexPosition = categoryToIndexPositionMap.get(wordCategory);
        int charIndex = 0;
        for (Integer index : wordIndexPosition) {
            if (outputArray[(index-1)] == null) {
                filledArrayPosition.add(index);
                outputArray[(index-1)] = wordValue.charAt(charIndex);
            }
            charIndex += 1;
        }
        return filledArrayPosition;
    }

    /**
     * It is used to remove the previously assigned word since we need to find all possible solution.
     * 
     * @param word
     */
    private void removePreviousAssignedWord(Node word, List<Integer> myOccupiedPosition) {
        // Remove the previous assigned word since we need to find all possible solution
        String wordCategory = word.getCategory();
        List<Integer> wordIndexPosition = categoryToIndexPositionMap.get(wordCategory);
        for (Integer index : wordIndexPosition) {
            if (myOccupiedPosition.contains(index)) {
                outputArray[(index - 1)] = null;
            }
        }
    }

    /**
     * It used to perform more informed forward checking. It checks whether the
     * assignment of given word leads to correct valid word formation for each
     * category
     * 
     * @param word
     * @return boolean
     */
    private boolean isValid(Node word) {
        // Clone the output array
        Character [] tempOutputArray = outputArray.clone();
        // Assign the value and check for validity
        String wordCategory = word.getCategory();
        String wordValue = word.getValue();
        List<Integer> wordIndexPosition = categoryToIndexPositionMap.get(wordCategory);
        int charIndex = 0;
        for (Integer index : wordIndexPosition) {
            tempOutputArray[(index-1)] = wordValue.charAt(charIndex);
            charIndex += 1;
        }
        // Check for each categories and its corresponding index position
        for (String category : categoryToIndexPositionMap.keySet()) {
            StringBuilder categoryWord = new StringBuilder();
            for (Integer index : categoryToIndexPositionMap.get(category)) {
                if (tempOutputArray[(index - 1)] != null) {
                    categoryWord.append(tempOutputArray[(index - 1)]);
                }
            }
            /*System.out.println("Word formed for category " + category
                    + " is " + categoryWord.toString());*/
            if (categoryWord.toString().length() == WordPuzzleConstant.WORD_LEN) {
                // Check if category contains such word
                if (!categoryToWordsMap.get(category).contains(categoryWord.toString())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * It is used to get the intersection between all categories at given variable position and already
     * exist character at given variable position. The filter helps to know whether there exist any values
     * for category. If no values then the previous assignment is wrong so backtrack.
     * 
     * @param category
     * @param variable
     * @param defaultCategoryToLetterWordMap
     * @param filter
     * @return
     */
    private Set<Node> getDomainVariable(String category, int variable
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap
            , Character filter) {

        // Output List of Node
        Set<Node> values = new HashSet<Node>();
        List<String> words = new ArrayList<String>();
        // If index is -1 then no matching, we need to consider all the words in that category
        if (variable == -1) {
            // Loop till the max length of word seen till now. But in our case we know it is 3
            for (int i = 0;i < WordPuzzleConstant.WORD_LEN;i++) {
                words.addAll(categoryToWordsMap.get(category));
            }
        } else {
            // Get the words corresponding to intersect character at specified index
            words = defaultCategoryToLetterWordMap.get(category).get(variable).get(filter);
        }
        // Check if there exist any words based on filter character
        if (words == null || words.isEmpty()) {
            return values;
        }
        // Prepare node for each words and output to node list
        for (String word : words) {
            Node node = new Node(word);
            node.setCategory(category);
            values.add(node);
        }
        return values;
    }

    /**
     * It is used to return the order in which category is assigned a value
     * @return List of ordered category
     */
    private List<String> getOrderedCategory() {
        List<String> orderedCategory = new ArrayList<String>();
        for (String category : categoryToIndexPositionMap.keySet()) {
            orderedCategory.add(category);
        }
        // Sort the category set as per number of words in each category
        Collections.sort(orderedCategory, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return (categoryToWordsMap.get(s1).size() - categoryToWordsMap.get(s2).size());
            }
        });
        StringBuilder categoryPath = new StringBuilder();
        int i = 0;
        for (i = (orderedCategory.size() - 1);i > 0;i--) {
            categoryPath.insert( 0, (WordPuzzleConstant.ARROW + orderedCategory.get(i)));
        }
        categoryPath.insert(0, orderedCategory.get(i));
        System.out.println("Search order: " + categoryPath.toString());
        return orderedCategory;
    }

    /**
     * It is used to print the path in reverse order of assignment
     * 
     * @param node
     */
    private void printPath(Node node, boolean backtrack, Character [] outputArray) {
        StringBuilder path = new StringBuilder();
        StringBuilder word = new StringBuilder();
        while (node != null) {
            //System.out.println(node + node.getCategory());
            if (!node.isVisited) {
                path.insert(0, (WordPuzzleConstant.ARROW + node.getValue()));
                node.setVisited(true);
            } else {
                path.insert(0, ("\t  "));
            }
            //path.insert(0, (WordPuzzleConstant.ARROW + node.getValue()));
            node = node.getParent();
        }
        if (path.indexOf("\t") != 0) {
            path.insert(0, "root");
        }
        if (backtrack) {
            path.append((WordPuzzleConstant.ARROW + "backtrack"));
            System.out.println(path.toString());
        } else {
            for (Character ch : outputArray) {
                word.append(ch);
            }
            System.out.println(path.toString() + " (found result: " + word.toString() + ")");
        }
    }
}
