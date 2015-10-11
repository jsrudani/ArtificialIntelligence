package ai.mp.wordpuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    private static Map<Integer,Map<String,Integer>> indexToCategoryMap = Preprocessing.getIndexToCategoryMap();
    private Map<String, Map<Integer, Map<Character, List<String> > > > defaultCategoryToLetterWordMap;
    private Map<String, List<Integer>> categoryToIndexPositionMap;
    private Map<String, Set<String>> categoryToWordsMap;
    private Character [] outputArray;
    private int outputArraySize;

    SolveWordPuzzleByWordAssignment(Map<String, Map<Integer, Map<Character, List<String> > > > categoryToLetterWordMap
            , final int outputArraySize
            , Map<String, Set<String>> categoryToWordsMap
            , Map<String, List<Integer>> categoryToIndexPositionMap) {
        this.defaultCategoryToLetterWordMap = categoryToLetterWordMap;
        this.outputArraySize = outputArraySize;
        this.categoryToWordsMap = categoryToWordsMap;
        this.categoryToIndexPositionMap = categoryToIndexPositionMap;
        this.outputArray = new Character[outputArraySize];
    }

    /**
     * It is used to solve word puzzle by word based assignment
     */
    public void solveByWordBasedAssignment() {
        // Get the order of Variable assignment (Most Constraint variable)
        int [] orderedVariables = getMostConstraintVariables(indexToCategoryMap);
        // Call recursively solve()
        solve(null, orderedVariables, 0, defaultCategoryToLetterWordMap);
    }

    private void solve(Node parent, int [] orderedVariables, int orderedVariableIdx
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap) {

        Character filter = null;

        // Check for base condition
        if (orderedVariableIdx < orderedVariables.length) {
            // Get the variable which needs to be assigned a value
            int variable = orderedVariables[orderedVariableIdx];
            // Check if there already exist character at variable position in output array
            if (outputArray[(variable - 1)] != null) {
                filter = outputArray[(variable - 1)];
            } else {
                filter = null;
            }

            // Debug
            System.out.println("====================================");
            System.out.println("Variable " + variable);
            System.out.println("Filter " + filter);

            // Get the domain value for given variable and filter
            Set<Character> intersectValues = getDomainVariable(variable,defaultCategoryToLetterWordMap,filter);

            System.out.println("Intersect values " + intersectValues);

            // Check if there exist any domain value
            if (intersectValues.isEmpty()) {
                // Backtrack print the path
                System.out.println("Backtrack ");
                //printPath(parent);
            } else {
                // Get all the words
                List<Node> values = getValues(variable,defaultCategoryToLetterWordMap,intersectValues);

                System.out.println("List of values " + values);

                // Update the default map according to domain value
                Map<String,Map<Integer,Map<Character,List<String>>>> updatedCategoryToLetterWordMap = 
                        updateCategoryToLetterWordMap(variable, intersectValues, defaultCategoryToLetterWordMap);
                // For each value assign each word to respective index position
                for (Node word : values) {
                    System.out.println("Output array b4 assigning word " + word + " for variable " + variable
                            + " is : \n" + Arrays.toString(outputArray));
                    word.setParent(parent);
                    // Check for consistency
                    if (!isConsistentAndFillOutputArray(word)) {
                        System.out.println("Not consistent ");
                        //printPath(parent);
                        continue;
                    }
                    // Fill the output array as per word as it is consistent
                    List<Integer> occupiedArrayPosition = fillOutPutArray(word);
                    System.out.println("Output array till now " + Arrays.toString(outputArray));
                    System.out.println("====================================");
                    // Recursively call to other index position
                    solve(word, orderedVariables, (orderedVariableIdx + 1), updatedCategoryToLetterWordMap);
                    // Remove the previous assignment
                    removePreviousAssignedWord(word, occupiedArrayPosition);
                }
            }
        } else {
            // Success path
            System.out.println("Success path ");
            printPath(parent);
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
            System.out.println("Word formed for category " + category
                    + " is " + categoryWord.toString());
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
     * @param variable
     * @param defaultCategoryToLetterWordMap
     * @param filter
     * @return Collection of intersect values
     */
    private Set<Character> getDomainVariable(int variable
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap
            , Character filter) {

        // Output set
        Set<Character> intersectValue = new HashSet<Character>();
        Set<Character> intermediateIntersectValue = new HashSet<Character>();

        if (filter != null) {
            intersectValue.add(filter);
        }

        // Get the set of categories belong to this variable (index)
        Set<String> categories = indexToCategoryMap.get(variable).keySet();

        // For each category get the corresponding index position and set of letter at that index position
        for (String category : categories) {
            int categoryIdx = indexToCategoryMap.get(variable).get(category);

            //System.out.println("map for category " + category);
            //System.out.println((defaultCategoryToLetterWordMap.get(category).get(categoryIdx).keySet()));

            // Add the set of characters at category index to intersect set
            if (!intermediateIntersectValue.isEmpty()) {
                intermediateIntersectValue.retainAll( (defaultCategoryToLetterWordMap.get(category).get(categoryIdx).keySet()) );
            } else {
                intermediateIntersectValue.addAll( (defaultCategoryToLetterWordMap.get(category).get(categoryIdx).keySet()) );
            }
            //System.out.println("intersect after each category " + category);
            //System.out.println(intermediateIntersectValue);
        }

        if (intersectValue.isEmpty()) {
            return intermediateIntersectValue;
        }
        intersectValue.retainAll(intermediateIntersectValue);
        // Return the domain values
        return intersectValue;
    }

    /**
     * It is used to get all the words which satisfy the domain constraint.
     * 
     * @param variable
     * @param defaultCategoryToLetterWordMap
     * @param intersectValue
     * @return Collection of words that has intersect value at specified position
     */
    private List<Node> getValues(int variable
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap
            , Set<Character> intersectValue) {

        // Output List of Node
        List<Node> values = new ArrayList<Node>();
        // Get the set of categories belong to this variable (index)
        Set<String> categories = indexToCategoryMap.get(variable).keySet();

        // For each intersect value and for each category get the all words that has intersect value
        for (Character ch : intersectValue) {
            for (String category : categories) {
                int categoryIdx = indexToCategoryMap.get(variable).get(category);
                // Get the words corresponding to intersect character at specified index
                List<String> words = defaultCategoryToLetterWordMap.get(category).get(categoryIdx).get(ch);
                // Prepare node for each words and output to node list
                for (String word : words) {
                    Node node = new Node(word);
                    node.setCategory(category);
                    values.add(node);
                }
            }
        }
        // Return the domain values
        return values;
    }

    /**
     * It is used to update the default CategoryToLetterWordMap w.r.t allowed character at given variable.
     * 
     * @param variable
     * @param intersectValue
     * @param defaultCategoryToLetterWordMap
     * 
     * @return updated map to be used by other variables
     */
    private Map<String,Map<Integer,Map<Character,List<String>>>> updateCategoryToLetterWordMap(int variable
            ,Set<Character> intersectValue
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap) {

        // Debug
        //System.out.println("updation progress " + defaultCategoryToLetterWordMap);

        // Get the set of categories belong to this variable (index)
        Set<String> categories = indexToCategoryMap.get(variable).keySet();

        // Clone the defaultCategoryToLetterWordMap
        Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMapClone
                = cloneCategoryToLetterWordMap(defaultCategoryToLetterWordMap);

        // For each category get the corresponding index position and set of letter at that index position
        for (String category : categories) {
            int categoryIdx = indexToCategoryMap.get(variable).get(category);
            // Clear the old letter to word map
            for(int i = 0; i < WordPuzzleConstant.WORD_LEN;i++) {
                defaultCategoryToLetterWordMapClone.get(category).get(i).clear();
            }
            // For each intersect value update the corresponding category
            for (Character ch : intersectValue) {
                // Get the word list from default map
                List<String> charToWordList
                        = defaultCategoryToLetterWordMap.get(category).get(categoryIdx).get(ch);
                //System.out.println("Old word list for char " + ch + " list " + charToWordList);
                // Put only word list w.r.t word in a map
                populateCategoryToLetterWordMap(charToWordList,category,defaultCategoryToLetterWordMapClone);
            }
            //System.out.println("Updated map for category " + category + " for index " + categoryIdx);
            //System.out.println(defaultCategoryToLetterWordMapClone.get(category));
        }
        return defaultCategoryToLetterWordMapClone;
    }

    /**
     * It used to clone the CategoryToLetterWordMap.
     * 
     * @param defaultCategoryToLetterWordMap
     * @return
     */
    private Map<String,Map<Integer,Map<Character,List<String>>>> cloneCategoryToLetterWordMap(Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap) {

        // Clone each nested map and list
        Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMapClone
                    = new HashMap<String,Map<Integer,Map<Character,List<String>>>>();
        for (String category : defaultCategoryToLetterWordMap.keySet()) {
            Map<Integer,Map<Character,List<String>>> tempIdxToCharacterMap
                                = defaultCategoryToLetterWordMap.get(category);
            Map<Integer,Map<Character,List<String>>> indexToCharacterMap =
                    new HashMap<Integer,Map<Character,List<String>>>();
            for (Integer index : tempIdxToCharacterMap.keySet()) {
                Map<Character,List<String>> tempCharToWordList = tempIdxToCharacterMap.get(index);
                Map<Character,List<String>> charToWordList = new HashMap<Character,List<String>>();
                for (Character ch : tempCharToWordList.keySet()) {
                    List<String> wordList = new ArrayList<String>();
                    wordList.addAll(tempCharToWordList.get(ch));
                    charToWordList.put(ch, wordList);
                }
                indexToCharacterMap.put(index, charToWordList);
            }
            defaultCategoryToLetterWordMapClone.put(category, indexToCharacterMap);
        }
        return defaultCategoryToLetterWordMapClone;
    }

    /**
     * It is used to update the character to word list as per new domain value for each category.
     * 
     * @param charToWordList
     * @param category
     * @param defaultCategoryToLetterWordMapClone
     */
    private void populateCategoryToLetterWordMap(List<String> charToWordList, String category
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMapClone) {

        Map<Integer, Map<Character,List<String> > > indexToLetterWordMap = defaultCategoryToLetterWordMapClone.get(category);

        Map<Character,List<String>> letterToWordListMap = null;

        for (String word : charToWordList) {
            int charIdx = 0;
            while (charIdx < word.length()) {
                letterToWordListMap = indexToLetterWordMap.get(charIdx);
                char c = word.charAt(charIdx);
                if (letterToWordListMap.containsKey(c)) {
                    letterToWordListMap.get(c).add(word);
                } else {
                    List<String> wordList = new ArrayList<String>();
                    wordList.add(word);
                    letterToWordListMap.put(c, wordList);
                }
                indexToLetterWordMap.put(charIdx, letterToWordListMap);
                charIdx += 1;
            }
        }
        defaultCategoryToLetterWordMapClone.put(category, indexToLetterWordMap);
    }

    /**
     * This method is used to select most constraint variables. The variables here are the
     * index position of final output array. By default we assign in ascending order of the
     * index.
     * 
     * @param indexToCategoryMap
     * 
     * @return most constraint variables
     */
    private int [] getMostConstraintVariables(Map<Integer,Map<String,Integer>> indexToCategoryMap) {
        int [] mcv = new int[outputArraySize];
        for (int i = 1;i <= mcv.length; i++) {
            mcv[i-1] = i;
        }
        return mcv;
    }

    /**
     * It is used to print the path in reverse order of assignment
     * 
     * @param node
     */
    private void printPath(Node node) {
        StringBuilder path = new StringBuilder();
        StringBuilder word = new StringBuilder();
        while (node != null) {
            System.out.println(node);
/*            path.insert(0, (WordPuzzleConstant.ARROW + node.getValue()));
            word.insert(0,node.getValue());*/
            node = node.getParent();
        }
/*        path.insert(0, "root");
        System.out.println(path.toString() + " (found result: " + word.toString() + ")");*/
    }
}
