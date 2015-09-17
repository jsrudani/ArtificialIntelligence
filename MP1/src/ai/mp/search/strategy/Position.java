package ai.mp.search.strategy;

/**
 * It is used to identify the position in Maze. 
 * It holds the co-ordinate information.
 * 
 * @author rudani2
 *
 */
public class Position implements Comparable<Position> {

    /**
     * It represent the x-axis of the position.
     */
    private final int x;
    /**
     * It represent the y-axis of the position.
     */
    private final int y;
    /**
     * It points to parent location.
     */
    private Position parent;

    /**
     * It holds the overall cost.
     * It includes summation of cost of reaching this node from start node.
     * and heuristic cost.
     */
    private long cost;
    /**
     * It holds the cost of reaching this node from start node.
     */
    private long approachableCost;
    /**
     * It used to indicate the direction of this position with respect to its parent.
     * It holds the value of direction as follows
     * <pre>
     * 1 - UP
     * 2 - DOWN
     * 3 - LEFT
     * 4 - RIGHT
     * </pre>
     */
    private final int direction;
    /**
     * It indicates towards which direction the current node is facing. It holds
     * the Position node from that direction.
     */
    private Position facing;

    Position(int x, int y, Position parent, long cost, long approachableCost, int direction) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.cost = cost;
        this.approachableCost = approachableCost;
        this.direction = direction;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Position getParent() {
        return parent;
    }

    public void setParent(Position parent) {
        this.parent = parent;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getApproachableCost() {
        return approachableCost;
    }

    public void setApproachableCost(long approachableCost) {
        this.approachableCost = approachableCost;
    }

    public int getDirection() {
        return direction;
    }

    public Position getFacing() {
        return facing;
    }

    public void setFacing(Position facing) {
        this.facing = facing;
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
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
        Position other = (Position) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    @Override
    public int compareTo(Position o) {
        return this.equals(o) ? 0 : -1;
    }
}
