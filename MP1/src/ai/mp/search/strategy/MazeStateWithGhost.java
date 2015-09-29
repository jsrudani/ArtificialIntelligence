package ai.mp.search.strategy;

/**
 * It is used to represent the state at a particular instance of time.
 * 
 * @author rudani2
 *
 */
public class MazeStateWithGhost {

    /**
     * It represents the position in this maze state.
     */
    private final Position position;
    /**
     * It represents the edge cost for this maze.
     */
    private long edgeCost;
    /**
     * It represents the parent state of this maze.
     */
    private MazeStateWithGhost parent;
    /**
     * It holds the cost of reaching this node from start node.
     */
    private long approachableCost;
    /**
     * It represents the ghost in these maze state.
     */
    private Position ghost;

    MazeStateWithGhost(Position position) {
        this.position = position;
    }

    public long getEdgeCost() {
        return edgeCost;
    }

    public void setEdgeCost(long edgeCost) {
        this.edgeCost = edgeCost;
    }

    public MazeStateWithGhost getParent() {
        return parent;
    }

    public void setParent(MazeStateWithGhost parent) {
        this.parent = parent;
    }

    public long getApproachableCost() {
        return approachableCost;
    }

    public void setApproachableCost(long approachableCost) {
        this.approachableCost = approachableCost;
    }

    public Position getGhost() {
        return ghost;
    }

    public void setGhost(Position ghost) {
        this.ghost = ghost;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "MazeStateWithGhost [position=" + position + ", parent="
                + parent + ", ghost=" + ghost + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ghost == null) ? 0 : ghost.hashCode());
        result = prime * result
                + ((position == null) ? 0 : position.hashCode());
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
        MazeStateWithGhost other = (MazeStateWithGhost) obj;
        if (ghost == null) {
            if (other.ghost != null)
                return false;
        } else if (!ghost.equals(other.ghost))
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        return true;
    }
}
