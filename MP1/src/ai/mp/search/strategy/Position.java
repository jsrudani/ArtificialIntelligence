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
    /**
     * It holds the next neighbor which has least distance among other neighbors.
     */
    private Position nextNeighborNode;
    /**
     * It holds the edge cost between neighbor and this node.
     */
    private long edgeCost;
    /**
     * It indicates whether position was already explored or not.
     */
    private boolean isVisited;
    /**
     * It represents the ghost position at that particular point in time.
     */
    private Position myGhost;
    /**
     * It includes penalty incurred for these position.
     */
    private long penalty;

    Position(int x, int y, Position parent, long cost, long approachableCost, int direction) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.cost = cost;
        this.approachableCost = approachableCost;
        this.direction = direction;
        this.isVisited = false;
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

    public Position getNextNeighborNode() {
        return nextNeighborNode;
    }

    public void setNextNeighborNode(Position nextNeighborNode) {
        this.nextNeighborNode = nextNeighborNode;
    }

    public long getEdgeCost() {
        return edgeCost;
    }

    public void setEdgeCost(long edgeCost) {
        this.edgeCost = edgeCost;
    }

    public long getPenalty() {
        return penalty;
    }

    public void setPenalty(long penalty) {
        this.penalty = penalty;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    public Position getMyGhost() {
        return myGhost;
    }

    public void setMyGhost(Position myGhost) {
        this.myGhost = myGhost;
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

    /**
     * It is used to return new position object with all default value data members.
     * 
     * @param obj
     * @return Position
     */
    public static Position clone(Position obj) {
        if (obj instanceof Position) {
            Position newClonedObject = new Position(obj.getX(), obj.getY(), null, MazeConstant.DEFAULT_COST
                    , MazeConstant.DEFAULT_COST, MazeConstant.DEFAULT_DIRECTION);
            newClonedObject.setEdgeCost(MazeConstant.DEFAULT_COST);
            return newClonedObject;
        }
        return null;
    }

    @Override
    public int compareTo(Position o) {
        return this.equals(o) ? 0 : -1;
    }
}
