package ai.mp.wordpuzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private Map<String, List<Integer>> categoryToIndexPositionMap;
    private int outputArraySize;
    private Map<String, Character[]> categoryToCharMap = new HashMap<String, Character[]>();

    SolveWordPuzzleByLetterAssignment(Map<String, Map<Integer, Map<Character, List<String> > > > categoryToLetterWordMap
            , final int outputArraySize
            , Map<String, Set<String>> categoryToWordsMap
            , Map<String, List<Integer>> categoryToIndexPositionMap) {
        this.defaultCategoryToLetterWordMap = categoryToLetterWordMap;
        this.outputArraySize = outputArraySize;
        this.categoryToWordsMap = categoryToWordsMap;
        this.categoryToIndexPositionMap = categoryToIndexPositionMap;
    }

    /**
     * It is used to solve word puzzle by letter based assignment
     */
    public void solveByLetterBasedAssignment() {
        // Get the order of Variable assignment (Most Constraint variable)
        List<Integer> orderedVariables = getMostConstraintVariables(indexToCategoryMap);
        // Call recursively solve()
        solve(null,orderedVariables,0,defaultCategoryToLetterWordMap);
    }

    private void solve(Node parent, List<Integer> orderedVariables, int orderedVariableIdx
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap) {

        // Check for base condition
        if (orderedVariableIdx < orderedVariables.size()) {
            int variable = orderedVariables.get(orderedVariableIdx);
            //int variable = orderedVariables[orderedVariableIdx];
            // Get the domain value for this variable
            Set<Character> domainValues = getDomainValue(variable, defaultCategoryToLetterWordMap);

            // Debug
            /*System.out.println("====================================");
            System.out.println("Variable " + variable);
            System.out.println("Domain values " + domainValues);*/

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
                    child.setVisited(false);
                    child.setCharIndex(variable);
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
            //System.out.println("Success path ");
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
                categoryToCharMap.get(category)[ (variable - 1) ] = ch;
                /*System.out.println("Current allocation "
                        + Arrays.toString(categoryToCharMap.get(category))
                        + " for category " + category);*/
                // Check if category is filled with all character
                StringBuilder word = new StringBuilder();
                for (int index : categoryToIndexPositionMap.get(category)) {
                    if (categoryToCharMap.get(category)[(index - 1)] != ' ') {
                        word.append( categoryToCharMap.get(category)[ (index - 1)] );
                    } else {
                        break;
                    }
                }
                //System.out.println("Word formed for " + category + " : " + word.toString());
                if (word.toString().length() == WordPuzzleConstant.WORD_LEN) {
                    // Check if word is valid
                    if (!categoryToWordsMap.get(category).contains(word.toString())) {
                        categoryToCharMap.get(category)[(variable - 1)] = ' ';
                        isRemove = true;
                        break;
                    }
                }
            } else {
                Character [] charList = new Character[outputArraySize];
                for (int i = 0;i < outputArraySize;i++) {
                    charList[i] = ' ';
                }
                charList[(variable - 1)] = ch;
                categoryToCharMap.put(category, charList);
            }
        }
        if (isRemove) {
            for (String category : categories) {
                // Check if category exist in categoryToWordMap
                if (categoryToCharMap.containsKey(category)) {
                    categoryToCharMap.get(category)[(variable - 1)] = ' ';
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
                categoryToCharMap.get(category)[(variable - 1)] = ' ';
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
    private List<Integer> getMostConstraintVariables(final Map<Integer,Map<String,Integer>> indexToCategoryMap) {
        List<Integer> index = new ArrayList<Integer>();
        index.addAll(indexToCategoryMap.keySet());
        Collections.sort(index, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                return (indexToCategoryMap.get(i2).keySet().size() - indexToCategoryMap.get(i1).keySet().size());
            }
        });
        System.out.println("Search order: " + index);
        return index;
    }

    /**
     * It is used to print the path in reverse order of assignment
     * 
     * @param node
     */
    private void printPath(Node node) {
        StringBuilder path = new StringBuilder();
        String [] letter = new String[outputArraySize];
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < outputArraySize; i++) {
            letter[i] = null;
        }
        while (node != null) {
            //System.out.println(node);
            if (!node.isVisited()) {
                path.insert(0, (WordPuzzleConstant.ARROW + node.getValue() + "(" + node.charIndex + ")"));
                node.setVisited(true);
            } else {
                path.insert(0, (" "));
            }
            letter[(node.getCharIndex() - 1)] = node.getValue();
            node = node.getParent();
        }
        if (path.indexOf(" ") != 0) {
            path.insert(0, "root");
        }
        for (String ch : letter) {
            word.append(ch);
        }
        System.out.println(path.toString() + " (found result: " + word.toString() + ")");
    }
}
