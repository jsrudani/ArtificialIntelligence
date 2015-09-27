package ai.mp.search.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the maze state at given position
 * 
 * @author rudani2
 *
 */
public class MazeState {
    /**
     * It represents the position in this maze state.
     */
    private final Position position;
    /**
     * It represents the edge cost for this maze.
     */
    private long edgeCost;
    /**
     * It represents the updated goal set for this maze.
     */
    private Set<Position> goalSet;
    /**
     * It represents the parent state of this maze.
     */
    private MazeState parent;
    /**
     * It holds the cost of reaching this node from start node.
     */
    private long approachableCost;
    /**
     * It holds the expanded set for this maze.
     */
    private Map<MazeState,Long> expandedMazeState;

    MazeState(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "MazeState [position=" + position + ", edgeCost=" + edgeCost
                + ",goal size=" + goalSet.size() + "]\n";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((goalSet == null) ? 0 : goalSet.hashCode());
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
        MazeState other = (MazeState) obj;
        if (goalSet == null) {
            if (other.goalSet != null)
                return false;
        } else if (!goalSet.equals(other.goalSet))
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        return true;
    }

    public Set<Position> getGoalSet() {
        return goalSet;
    }

    public void setGoalSet(Set<Position> goalSet) {
        this.goalSet = goalSet;
    }

    public MazeState getParent() {
        return parent;
    }

    public void setParent(MazeState parent) {
        this.parent = parent;
    }

    public long getApproachableCost() {
        return approachableCost;
    }

    public void setApproachableCost(long approachableCost) {
        this.approachableCost = approachableCost;
    }

    public Position getPosition() {
        return position;
    }

    public long getEdgeCost() {
        return edgeCost;
    }

    public void setEdgeCost(long edgeCost) {
        this.edgeCost = edgeCost;
    }

    public Map<MazeState, Long> getExpandedMazeState() {
        return expandedMazeState;
    }

    public void setExpandedMazeState(Map<MazeState, Long> expandedMazeState) {
        this.expandedMazeState = expandedMazeState;
    }

}
