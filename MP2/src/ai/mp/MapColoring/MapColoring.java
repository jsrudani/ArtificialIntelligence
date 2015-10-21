package ai.mp.MapColoring;

import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * It is used to perform following action
 * <pre>
 * 1. Create Map of N vertices
 * 2. Color the Map
 * </pre>
 * 
 * @author rudani2
 *
 */
public class MapColoring {

    /**
     * It is used to hold the valid segment between regions
     */
    private Set<Segment> segmentSet = new HashSet<Segment>();

    public static void main(String[] args) {

        // Instantiate the Map Coloring instance
        MapColoring mapColor = new MapColoring();
        // Read the Number of vertices (N)
        System.out.println("Enter the number of points : ");
        Scanner input = new Scanner(System.in);
        int numberOfVertices = input.nextInt();
        System.out.println("Number of vertices " + numberOfVertices);

        // Create the random region (point)
        //Set<Region> region = mapColor.generateRandomRegion(numberOfVertices);
        //System.out.println(region);

        // Temp set of regions. It will be removed after entire problem solved
        Set<Region> region = new HashSet<Region>();
        Region reg = new Region(3,0);
        region.add(reg);
        reg = new Region(1,1);
        region.add(reg);
        reg = new Region(0,2);
        region.add(reg);
        reg = new Region(1,3);
        region.add(reg);
        System.out.println(region);

        // Generate Map with N connected points
        mapColor.createMapWithRegion(region);
        System.out.println(mapColor.segmentSet);

        // Color the Map
    }

    /**
     * It is used to generate given N regions using random number generator.
     * 
     * @param numberOfVertices
     * @return Set of regions
     */
    public Set<Region> generateRandomRegion(int numberOfVertices) {

        Region region;
        Set<Region> regionSet = new HashSet<Region>();
        Random rand = new Random();
        int regionCount = 0;
        while (regionCount < numberOfVertices) {
            // Generate random number
            int randomX = rand.nextInt(numberOfVertices);
            int randomY = rand.nextInt(numberOfVertices);
            region = new Region(randomX, randomY);
            // Check if region already exist
            if (!regionSet.contains(region)) {
                regionSet.add(region);
                // increment the region count
                regionCount += 1;
            }
        }
        return regionSet;
    }

    public void createMapWithRegion(Set<Region> regions) {
        // Randomly select the region and find the closest region
        // and connect them
        int size = regions.size();
        Random randomRegionIndex = new Random();
        Region [] regionArray = regions.toArray(new Region[size]);
        int totalPossibleCombination = size * (size - 1);
        int count = 0;
        while (count < totalPossibleCombination) {
            int index = randomRegionIndex.nextInt(size);
            Region randomRegion = regionArray[index];
            double minDistance = Double.MAX_VALUE;
            Region minDistRegion = null;
            for (Region reg : regions) {
                if (!randomRegion.equals(reg)
                    && (!randomRegion.checkIfNeighborExist(reg))
                    && (!isIntersect(new Segment(randomRegion, reg))) ) {
                    // Track the minimum distance neighbor
                    double distance = calculateDistance(randomRegion, reg);
                    if (distance < minDistance) {
                        minDistance = distance;
                        minDistRegion = reg;
                    }
                }
            }
            // check if there exist minimum distance region. If null then all neighbors are explored
            if (minDistRegion != null) {
                // Add to my neighbor list
                randomRegion.addToNeighbors(minDistRegion);
                // Add myself to neighbor's list
                minDistRegion.addToNeighbors(randomRegion);
                // Add the segment to global list
                segmentSet.add(new Segment(randomRegion, minDistRegion));
            }
            count += 1;
        }
    }

    /**
     * It is used to calculate the distance between two region using distance formula
     * 
     * @param fromRegion
     * @param toRegion
     * @return distance
     */
    private double calculateDistance(Region fromRegion, Region toRegion) {
        double x1 = (double)fromRegion.getX();
        double y1 = (double)fromRegion.getY();
        double x2 = (double)toRegion.getX();
        double y2 = (double)toRegion.getY();
        double squareOfDifferences = (Math.pow((x1 - x2), 2)) + (Math.pow((y1 - y2), 2));
        double distance = Math.sqrt(squareOfDifferences);
        return distance;
    }

    /**
     * It is used to check if given segment intersect with other valid segment or not
     * 
     * @param newSegment
     * @return boolean
     */
    private boolean isIntersect(Segment newSegment) {
        // For every other valid segment check whether new segment intersect or not
        for (Segment segment : segmentSet) {
            if (isTwoSegmentIntersect(newSegment, segment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * It is used to check if given two segment intersect or not
     * 
     * @param newSegment
     * @param oldSegment
     * @return boolean
     */
    private boolean isTwoSegmentIntersect(Segment newSegment, Segment oldSegment) {
        int denominator = ( (newSegment.getToRegion().getY() - newSegment.getFromRegion().getY())
                          * (oldSegment.getToRegion().getX() - oldSegment.getFromRegion().getX())
                          ) - ( (newSegment.getToRegion().getX() - newSegment.getFromRegion().getX())
                          * (oldSegment.getToRegion().getY() - oldSegment.getFromRegion().getY()) );
        int numeratorA = ( (newSegment.getToRegion().getX() - newSegment.getFromRegion().getX())
                         * (oldSegment.getFromRegion().getY() - newSegment.getFromRegion().getY())
                         ) - ( (newSegment.getToRegion().getY() - newSegment.getFromRegion().getY())
                         * (oldSegment.getFromRegion().getX() - newSegment.getFromRegion().getX()) );
        int numeratorB = ( (oldSegment.getToRegion().getX() - oldSegment.getFromRegion().getX())
                         * (oldSegment.getFromRegion().getY() - newSegment.getFromRegion().getY())
                         ) - ( (oldSegment.getToRegion().getY() - oldSegment.getFromRegion().getY())
                         * (oldSegment.getFromRegion().getX() - newSegment.getFromRegion().getX()) );
        // Check if segment are parallel
        if (Math.abs(denominator) == 0) {
            return false;
        }
        // Is the intersection along the the segments
        float mNumeratorA = (float)numeratorA/(float)denominator;
        float mNumneratorB = (float)numeratorB/(float)denominator;
        if (mNumeratorA < 0.0 || mNumeratorA > 1.0
                || mNumneratorB < 0.0 || mNumneratorB > 1.0) {
            return false;
        }
        return true;
    }
}
