package ai.mp.MapColoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used to represent region in a given plane. It includes following
 * information
 * 
 * <pre>
 * 1. coordinate information (x,y)
 * 2. List of neighbor
 * 3. color assigned
 * </pre>
 * 
 * @author rudani2
 *
 */
public class Region {

    private final int x;
    private final int y;
    private List<Region> neighbors;
    private ColorInfo assignedColor;
    private Set<ColorInfo> domains;

    Region(int x, int y) {
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<Region>();
        domains = new HashSet<ColorInfo>();
        initializeDefaultDomain();
    }

    private void initializeDefaultDomain() {
        for (COLOR color : COLOR.values()) {
            domains.add(new ColorInfo(color));
        }
    }

    public Region clone() {
        return new Region(this.getX(), this.getY());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Region other = (Region) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Region [x=" + x + ", y=" + y + ", assignedColor="
                + assignedColor + "]\n";
    }

    public List<Region> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Region> neighbors) {
        this.neighbors = neighbors;
    }

    public ColorInfo getAssignedColor() {
        return assignedColor;
    }

    public void setAssignedColor(ColorInfo assignedColor) {
        this.assignedColor = assignedColor;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void addToNeighbors(Region region) {
        this.neighbors.add(region);
    }

    public void removeFromNeighbors(Region region) {
        this.neighbors.remove(region);
    }

    public boolean checkIfNeighborExist(Region region) {
        return this.neighbors.contains(region);
    }

    public Set<ColorInfo> getDomains() {
        return domains;
    }
}
