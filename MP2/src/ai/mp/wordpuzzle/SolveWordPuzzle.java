package ai.mp.wordpuzzle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible to solve word puzzle by following two technique
 * <pre>
 * 1. Letter based assignment
 * 2. Word based assignment
 * </pre>
 * 
 * @author rudani2
 *
 */
public class SolveWordPuzzle {

    private static Map<Integer,Map<String,Integer>> indexToCategoryMap = Preprocessing.getIndexToCategoryMap();
    private Map<String, Map<Integer, Map<Character, List<String> > > > defaultCategoryToLetterWordMap;
    private int outputArraySize;

    SolveWordPuzzle(Map<String, Map<Integer, Map<Character, List<String> > > > categoryToLetterWordMap
            , final int outputArraySize) {
        this.defaultCategoryToLetterWordMap = categoryToLetterWordMap;
        this.outputArraySize = outputArraySize;
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
            // Check if there exist domain values
            if (domainValues.isEmpty()) {
                // Backtrack print the path
                printPath(parent);
            } else {
                // Update the default map according to domain value
                Map<String,Map<Integer,Map<Character,List<String>>>> updatedCategoryToLetterWordMap = 
                        updateCategoryToLetterWordMap(variable, domainValues, defaultCategoryToLetterWordMap);
                // After consistency check assign current value and iterate for another variable
                for (Character ch : domainValues) {
                    Node child = new Node(ch.toString());
                    child.setParent(parent);
                    solve(child, orderedVariables, (orderedVariableIdx + 1), updatedCategoryToLetterWordMap);
                }
            }
        } else {
            // Success print path
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
    private Map<String,Map<Integer,Map<Character,List<String>>>> updateCategoryToLetterWordMap(int variable, Set<Character> intersectValue
            , Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMap) {
        // Get the set of categories belong to this variable (index)
        Set<String> categories = indexToCategoryMap.get(variable).keySet();
        // Clone the defaultCategoryToLetterWordMap
        Map<String,Map<Integer,Map<Character,List<String>>>> defaultCategoryToLetterWordMapClone =
                new HashMap<String,Map<Integer,Map<Character,List<String>>>>();
        defaultCategoryToLetterWordMapClone.putAll(defaultCategoryToLetterWordMap);
        // For each category get the corresponding index position and set of letter at that index position
        for (String category : categories) {
            int categoryIdx = indexToCategoryMap.get(variable).get(category);
            // Clear the content of category in default map
            defaultCategoryToLetterWordMapClone.get(category).get(categoryIdx).clear();
            // For each intersect value update the corresponding category
            for (Character ch : intersectValue) {
                // Get the word list from default map
                List<String> charToWordList
                = defaultCategoryToLetterWordMap.get(category).get(categoryIdx).get(ch);
                // Put only word list w.r.t word in a map
                defaultCategoryToLetterWordMapClone.get(category).get(categoryIdx).put(ch, charToWordList);
            }
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
        for (int i = 0;i < mcv.length; i++) {
            mcv[i] = i;
        }
        return mcv;
    }

    /**
     * It is used to print the path in reverse order of assignment
     * 
     * @param node
     */
    private void printPath(Node node) {
        while (node != null) {
            System.out.println(node);
            node = node.getParent();
        }
    }
}
