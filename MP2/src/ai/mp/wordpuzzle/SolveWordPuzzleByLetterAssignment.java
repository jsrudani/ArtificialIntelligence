package ai.mp.wordpuzzle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible to solve word puzzle by following technique
 * <pre>
 * 1. Letter based assignment
 * </pre>
 * 
 * @author rudani2
 *
 */
public class SolveWordPuzzleByLetterAssignment {

    private static Map<Integer,Map<String,Integer>> indexToCategoryMap = Preprocessing.getIndexToCategoryMap();
    private Map<String, Map<Integer, Map<Character, List<String> > > > defaultCategoryToLetterWordMap;
    private Map<String, Set<String>> categoryToWordsMap;
    private int outputArraySize;
    private Map<String, List<Character>> categoryToCharMap = new HashMap<String, List<Character>>();

    SolveWordPuzzleByLetterAssignment(Map<String, Map<Integer, Map<Character, List<String> > > > categoryToLetterWordMap
            , final int outputArraySize
            , Map<String, Set<String>> categoryToWordsMap) {
        this.defaultCategoryToLetterWordMap = categoryToLetterWordMap;
        this.outputArraySize = outputArraySize;
        this.categoryToWordsMap = categoryToWordsMap;
    }

    /**
     * It is used to solve word puzzle by letter based assignment
     */
    public void solveByLetterBasedAssignment() {
        // Get the order of Variable assignment (Most Constraint variable)
        int [] orderedVariables = getMostConstraintVariables(indexToCategoryMap);
        // Call recursively solve()
        solve(null,orderedVariables,0,defaultCategoryToLetterWordMap);
    }

    private void solve(Node parent, int [] orderedVariables, int orderedVariableIdx
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap) {

        // Check for base condition
        if (orderedVariableIdx < orderedVariables.length) {
            int variable = orderedVariables[orderedVariableIdx];
            // Get the domain value for this variable
            Set<Character> domainValues = getDomainValue(variable, defaultCategoryToLetterWordMap);

            // Debug
            //System.out.println("====================================");
            //System.out.println("Variable " + variable);
            //System.out.println("Domain values " + domainValues);

            // Check if there exist domain values
            if (domainValues.isEmpty()) {
                // Backtrack print the path
                System.out.println("Backtrack ");
                printPath(parent);
            } else {
                // Update the default map according to domain value
                Map<String,Map<Integer,Map<Character,List<String>>>> updatedCategoryToLetterWordMap = 
                        updateCategoryToLetterWordMap(variable, domainValues, defaultCategoryToLetterWordMap);
                // After consistency check assign current value and iterate for another variable
                for (Character ch : domainValues) {
                    //System.out.println("Assign " + ch + " for variable " + variable);
                    Node child = new Node(ch.toString());
                    child.setParent(parent);
                    // consistency check
                    if (!isConsistent(ch, variable)) {
                        //System.out.println("Not consistent ");
                        //printPath(child);
                        continue;
                    }
                    //System.out.println("====================================");
                    solve(child, orderedVariables, (orderedVariableIdx + 1), updatedCategoryToLetterWordMap);
                    // Remove the previous assignment
                    removePreviousAssignment(ch, variable);
                }
            }
        } else {
            // Success print path
            System.out.println("Success path ");
            printPath(parent);
        }
    }

    /**
     * This method is used to get the common value from all the categories which are at given variable.
     * 
     * @param variable
     * @param defaultCategoryToLetterWordMap
     * 
     * @return intersect value set
     */
    private Set<Character> getDomainValue(int variable
            ,Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap) {
        // Output set
        Set<Character> intersectValue = new HashSet<Character>();
        // Get the set of categories belong to this variable (index)
        Set<String> categories = indexToCategoryMap.get(variable).keySet();
        // For each category get the corresponding index position and set of letter at that index position
        for (String category : categories) {
            int categoryIdx = indexToCategoryMap.get(variable).get(category);

            // Debug
            //System.out.println("Category " + category + " For index " + categoryIdx);
            //System.out.println( defaultCategoryToLetterWordMap.get(category).get(categoryIdx) );
            //System.out.println( defaultCategoryToLetterWordMap.get(category).get(categoryIdx).keySet() );

            // Add the set of characters at category index to intersect set
            if (!intersectValue.isEmpty()) {
                intersectValue.retainAll( (defaultCategoryToLetterWordMap.get(category).get(categoryIdx).keySet()) );
            } else {
                intersectValue.addAll( (defaultCategoryToLetterWordMap.get(category).get(categoryIdx).keySet()) );
            }
        }
        return intersectValue;
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
     * It is used to perform forward checking. It is used to validate whether word formed by
     * assigning character is valid for given category.
     * 
     * @param ch
     * @param variable
     * @return boolean
     */
    private boolean isConsistent(Character ch, int variable) {

        boolean isRemove = false;
        // Get the set of categories belong to this variable (index)
        Set<String> categories = indexToCategoryMap.get(variable).keySet();
        for (String category : categories) {
            //System.out.println("Before consistency check for category " + category);
            // Check if category exist in categoryToWordMap
            if (categoryToCharMap.containsKey(category)) {
                categoryToCharMap.get(category).add(ch);
                /*System.out.println("Current allocation "
                        + categoryToCharMap.get(category)
                        + " for category " + category);*/
                // Check if category is filled with all character
                if (categoryToCharMap.get(category).size() == WordPuzzleConstant.WORD_LEN) {
                    StringBuilder word = new StringBuilder();
                    word.append(categoryToCharMap.get(category).get(0));
                    word.append(categoryToCharMap.get(category).get(1));
                    word.append(categoryToCharMap.get(category).get(2));
                    //System.out.println("isConsistent word " + word.toString() + " for category " + category);
                    // Check if word is valid
                    if (!categoryToWordsMap.get(category).contains(word.toString())) {
                        categoryToCharMap.get(category).remove(ch);
                        isRemove = true;
                        break;
                    }
                }
            } else {
                List<Character> charList = new ArrayList<Character>();
                charList.add(ch);
                categoryToCharMap.put(category, charList);
            }
        }
        if (isRemove) {
            for (String category : categories) {
                // Check if category exist in categoryToWordMap
                if (categoryToCharMap.containsKey(category)) {
                    categoryToCharMap.get(category).remove(ch);
                }
            }
            return false;
        }
        return true;
    }

    /**
     * It is used to remove the previous assignment since we need to find all the possible solution
     * 
     * @param ch
     * @param variable
     */
    private void removePreviousAssignment(Character ch, int variable) {

        // Get the set of categories belong to this variable (index)
        Set<String> categories = indexToCategoryMap.get(variable).keySet();
        for (String category : categories) {
            // Check if category exist in categoryToWordMap
            if (categoryToCharMap.containsKey(category)) {
                // remove the assignment
                categoryToCharMap.get(category).remove(ch);
            }
        }
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
            //System.out.println(node);
            path.insert(0, (WordPuzzleConstant.ARROW + node.getValue()));
            word.insert(0,node.getValue());
            node = node.getParent();
        }
        path.insert(0, "root");
        System.out.println(path.toString() + " (found result: " + word.toString() + ")");
    }
}
