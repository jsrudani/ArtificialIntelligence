package ai.mp.MapColoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * It is used to color the map without performing any forward check for
 * variable assignment.
 * 
 * @author rudani2
 *
 */
public class ColorMapWithoutForwardCheck {

    private static long startTime = 0L;
    private static long endTime = 0L;
    private static long variableAssignment = 0L;
    private final Set<Region> region;
    private final Set<Segment> segmentSet;
    private boolean isSolutionFound = false;

    ColorMapWithoutForwardCheck(Set<Region> region, Set<Segment> segmentSet) {
        this.region = region;
        this.segmentSet = segmentSet;
    }

    /**
     * It is used to solve map coloring without forward checking.
     */
    public void solveWithoutFrwdCheck() {

        // Get the order of Variable assignment (Most Constraint variable)
        List<Region> orderedRegions = getMostConstraintVariables(region);
        /*for (Region reg : orderedRegions) {
            System.out.println(reg);
            System.out.println(reg.getDomains());
        }*/

        // Record the start time
        startTime = System.currentTimeMillis();

        // Call recursively solve()
        solve(orderedRegions,0);
    }

    private void solve (List<Region> orderedRegions, int orderedVariableIdx) {

        // Check for Base condition
        if (orderedVariableIdx < orderedRegions.size()) {
            // Get the region from ordered region
            Region variable = orderedRegions.get(orderedVariableIdx);
            // Get the domain values for this region
            Set<ColorInfo> values = getDomainVariable(variable);
            // Check if values exist else backtrack
            if (values.isEmpty()) {
                // No more value possible to assigned
                System.out.println("Backtrack");
            } else {
                // Get the value from domain and perform further action
                for (ColorInfo color : values) {
                    // Set the current color as my color
                    variable.setAssignedColor(color);
                    // Increment variable assignment
                    variableAssignment += 1;
                    solve(orderedRegions, (orderedVariableIdx + 1));
                    // Unset my color if there is a backtrack
                    if (!isSolutionFound) {
                        variable.setAssignedColor(null);
                    } else {
                        // Solution found no need to find all solution
                        break;
                    }
                }
            }
        } else {
            // Prevent further exploring the solution
            isSolutionFound = true;
            // Record the end time
            endTime = System.currentTimeMillis();
            // Success path. Entire map is colored.
            System.out.println("Success");
            // For verification
            System.out.println(region);
            // Print all the statistics
            System.out.println("N: " + region.size());
            System.out.println("Number of constraint: " + segmentSet.size());
            System.out.println("Number of variable assignment: " + variableAssignment);
            System.out.println("Running time in ms: " + (endTime - startTime));
        }
    }

    private Set<ColorInfo> getDomainVariable (Region region) {
        // Get the set of colors assigned to all my neighbors
        Set<ColorInfo> neighborsColor = new HashSet<ColorInfo>();
        for (Region neighbor : region.getNeighbors()) {
            // Check if it is assigned a color
            if (neighbor.getAssignedColor() != null) {
                neighborsColor.add(neighbor.getAssignedColor());
            }
        }
        // Subtract all the neighbors colors from my list
        region.getDomains().removeAll(neighborsColor);
        return region.getDomains();
    }

    /**
     * It is used to order the region randomly.
     * 
     * @param region
     * @return Random order of the regions
     */
    private List<Region> getMostConstraintVariables(Set<Region> region) {
        List<Region> orderedRegions = new ArrayList<Region>();
        orderedRegions.addAll(region);
        Collections.shuffle(orderedRegions);
        return orderedRegions;
    }
}
