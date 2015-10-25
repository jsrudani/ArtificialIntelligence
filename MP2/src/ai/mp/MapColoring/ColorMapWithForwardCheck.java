package ai.mp.MapColoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * It is used to color the map by performing forward check for
 * variable assignment.
 * 
 * @author rudani2
 *
 */
public class ColorMapWithForwardCheck {

    private static long startTime = 0L;
    private static long endTime = 0L;
    private static long variableAssignment = 0L;
    private final Set<Region> region;
    private final Set<Segment> segmentSet;
    private Set<Region> regionTrackingSet;
    private boolean isSolutionFound = false;

    ColorMapWithForwardCheck(Set<Region> region, Set<Segment> segmentSet) {
        this.region = region;
        this.segmentSet = segmentSet;
        this.regionTrackingSet = new HashSet<Region>();
        cloneRegion();
    }

    /**
     * It is used to solve map coloring with forward checking.
     */
    public void solveWithFrwdCheck() {

        // Get the order of Variable assignment (Most Constraint variable)
        List<Region> orderedRegions = getMostConstraintVariables(region);
        /*for (Region reg : orderedRegions) {
            System.out.println(reg);
            System.out.println(reg.getDomains());
        }*/
        // Record the start time
        startTime = System.currentTimeMillis();
        //System.out.println("Start time: " + startTime);

        // Call recursively solve()
        solve(orderedRegions,0);

        // Record the end time
        endTime = System.currentTimeMillis();
        //System.out.println("End time: " + endTime);

        // Print all the statistics
        System.out.println("N: " + region.size());
        System.out.println("Number of constraint: " + segmentSet.size());
        System.out.println("Number of variable assignment: " + variableAssignment);
        System.out.println("Running time in ms: " + (endTime - startTime));

    }

    private void solve (List<Region> orderedRegions, int orderedVariableIdx) {

        // Check for Base condition
        if (orderedVariableIdx < orderedRegions.size()) {
            // Get the region from ordered region
            Region variable = orderedRegions.get(orderedVariableIdx);
            // Get the domain values for this region
            List<ColorInfo> values = getDomainVariable(variable);
            // Check if values exist else backtrack
            if (values.isEmpty()) {
                // No more value possible to assigned
                System.out.println("Backtrack");
            } else {
                // Get the value from domain and perform further action
                for (ColorInfo color : values) {
                    // Set the current color as my color
                    variable.setAssignedColor(color);
                    // Perform forward checking
                    if (isForwardCheckSuccessfull(variable)) {
                        // Increment variable assignment
                        variableAssignment += 1;
                        solve(orderedRegions, (orderedVariableIdx + 1));
                        // Reset my color if there is a backtrack
                        if (!isSolutionFound) {
                            variable.setAssignedColor(null);
                        } else {
                            // Solution found no need to find all solution
                            break;
                        }
                    } else {
                        // Not satisfy forward checking, reset the current assignment
                        variable.setAssignedColor(null);
                    }
                }
            }
        } else {
            // Prevent further exploring the solution
            isSolutionFound = true;
            // Success path. Entire map is colored.
            System.out.println("Success");
            // For verification
            System.out.println(region);
        }
    }

    private boolean isForwardCheckSuccessfull(Region region) {
        boolean isRemove = false;
        // Get my assigned color which needs to be verified for forward checking
        Set<ColorInfo> myAssignedColor = new HashSet<ColorInfo>();
        myAssignedColor.add(region.getAssignedColor());
        // For all region in tracking set, check unassigned neighbor domain
        for (Region reg : regionTrackingSet) {
            if (region.getNeighbors().contains(reg)
                    && reg.getAssignedColor() != null) {
                reg.getDomains().removeAll(myAssignedColor);
                // check if domain is empty
                if (reg.getDomains().isEmpty()) {
                    isRemove = true;
                    break;
                }
            }
        }
        if (isRemove) {
            // Restore the neighbor domain value
            for (Region reg : regionTrackingSet) {
                if (region.getNeighbors().contains(reg)
                        && reg.getAssignedColor() != null) {
                    reg.getDomains().addAll(myAssignedColor);
                }
            }
            return false;
        }
        // Check for constraint propogation
        return isSatisfyConstraintPropogation(region) ? true : false;
        //return true;
    }

    private boolean isSatisfyConstraintPropogation(Region region) {
        // Check in tracking set if any of my unassigned neighbor has one value and
        // matches to any of my other unassigned neighbor
        for (Region myNeighbor1 : regionTrackingSet) {
            if (region.getNeighbors().contains(myNeighbor1)
                    && myNeighbor1.getAssignedColor() != null
                    && myNeighbor1.getDomains().size() == 1) {
                for (Region myNeighbor2 : regionTrackingSet) {
                    if (region.getNeighbors().contains(myNeighbor2)
                            && myNeighbor2.getAssignedColor() != null
                            && !myNeighbor1.equals(myNeighbor2)
                            && myNeighbor2.getDomains().size() == 1
                            && myNeighbor1.getDomains().containsAll(myNeighbor2.getDomains())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private List<ColorInfo> getDomainVariable (Region region) {
        // Get the set of colors assigned to all my neighbors
        Set<ColorInfo> neighborsColor = new HashSet<ColorInfo>();
        Set<Region> unassignedNeighbor = new HashSet<Region>();
        for (Region neighbor : region.getNeighbors()) {
            // Check if it is assigned a color
            if (neighbor.getAssignedColor() != null) {
                neighborsColor.add(neighbor.getAssignedColor());
            } else {
                unassignedNeighbor.add(neighbor);
            }
        }
        // Subtract all the neighbors colors from my list
        region.getDomains().removeAll(neighborsColor);
        // For each color possible for me, sort the domain based on least
        // constraint values
        for (ColorInfo color : region.getDomains()) {
            int remainingLegalValues = color.getRemainingLegalValues();
            Set<ColorInfo> myColor = new HashSet<ColorInfo>();
            myColor.add(color);
            for (Region neighbor : unassignedNeighbor) {
                Set<ColorInfo> neighborColor = new HashSet<ColorInfo>(neighbor.getDomains());
                neighborColor.removeAll(myColor);
                remainingLegalValues += neighborColor.size();
            }
            color.setRemainingLegalValues(remainingLegalValues);
        }
        // Sort the domain value based on remaining legal values
        List<ColorInfo> colorValues = new ArrayList<ColorInfo>();
        colorValues.addAll(region.getDomains());
        Collections.sort(colorValues, new Comparator<ColorInfo> () {
            @Override
            public int compare (ColorInfo color1, ColorInfo color2) {
                return color1.getRemainingLegalValues() - color2.getRemainingLegalValues();
            }
        });
        return colorValues;
    }

    /**
     * It is used to order the region in descending order of number
     * of neighbors.
     * 
     * @param region
     * @return Descending order of the regions
     */
    private List<Region> getMostConstraintVariables(Set<Region> region) {
        List<Region> orderedRegions = new ArrayList<Region>();
        orderedRegions.addAll(region);
        Collections.sort(orderedRegions, new Comparator<Region> () {
            @Override
            public int compare (Region r1, Region r2) {
                return r2.getNeighbors().size() - r1.getNeighbors().size();
            }
        });
        return orderedRegions;
    }

    /**
     * It is used to keep track of legal values in unassigned variable. It
     * is used in forward checking.
     */
    private void cloneRegion() {
        // For each set of region, clone the region and
        // store into regionTrackingSet
        for (Region region : this.region) {
            regionTrackingSet.add(region.clone());
        }
    }
}
