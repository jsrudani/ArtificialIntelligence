package ai.mp.search.strategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible to build Minimum Spanning Tree for given collection of {@link Position} and Goal position
 * 
 * @author rudani2
 *
 */
public class MST {

    private final Set<Position> goalSet;
    private final Position startPosition;
    private List<Edge> edges;
    private Map<Position, Subset> subsets = new HashMap<Position, Subset>();

    MST(Position startPosition, Set<Position> goalSet) {
        this.startPosition = startPosition;
        this.goalSet = goalSet;
        edges = new LinkedList<Edge>();
        // initialize subset
        initializeSubset();
    }

    /**
     * It is used to initialize the subset with start position and all goal position. By default each subset is of 
     * size 1.
     */
    private void initializeSubset() {
        // Reset the visited states for start position
        startPosition.setVisited(false);
        // Add the start position
        subsets.put(startPosition, new Subset(startPosition, 0));
        Iterator<Position> goals = goalSet.iterator();
        while (goals.hasNext()) {
            Position nextGoal = goals.next();
            // Reset the visited states
            nextGoal.setVisited(false);
            subsets.put(nextGoal, new Subset(nextGoal, 0));
        }
    }

    public long buildMST() {
        // Indicate total edge cost
        long edgeCost = 0L;
        try {
            // Indicate we have cover all the edges
            long edgeCount = 0L;
            // Calculate the edge cost between every goals and start position
            calculateEdgeCost();
            // debug
            //System.out.println("B4 sort " + edges);
            // Sort the edge as per weight
            Collections.sort(edges, new EdgeComparator());
            //System.out.println(edges);
            //System.out.println("Edge size " + edges.size());
            //System.out.println(subsets);
            // For every edge build the MST
            Iterator<Edge> edge = edges.iterator();
            while (edge.hasNext()) {
                Edge singleEdge = edge.next();
                Position position1 = find(singleEdge.getSource());
                Position position2 = find(singleEdge.getDestination());
                if (position1 == null || position2 == null) {
                    throw new IllegalArgumentException("Not able to find parent. Missing edge in edge list");
                }
                // Check if both are equal. If not equal means there is no cycle
                if (!position1.equals(position2) || (singleEdge.getSource().equals(singleEdge.getDestination())) ) {
                    //System.out.println("Position 1 " + position1);
                    //System.out.println("Position 2 " + position2);
                    edgeCost += singleEdge.getWeight();
                    edgeCount += 1;
                    union(position1,position2);
                }
                // debug
                //System.out.println("Edge count " + edgeCount);
                //System.out.println("Goal count " + goalSet.size());
                // Check if all edges are visited
                if (edgeCount == goalSet.size()) {
                    System.out.println("Edge cost " + edgeCost);
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        return edgeCost;
    }

    /** 
     * A utility function to find set of an element node using path compression technique.
     * 
     * @param node
     */
    private Position find(Position node) {
        // find root and make root as parent of node (path compression)
        if (subsets.containsKey(node)) {
            if (!subsets.get(node).getParent().equals(node)) {
                Position newParent = find(subsets.get(node).getParent());
                subsets.get(node).setParent(newParent);
            }
            return subsets.get(node).getParent();
        }
        return null;
    }

    /**
     * A function that does union of two sets X and Y using rank value.
     * 
     * @param X
     * @param Y
     */
    private void union(Position X, Position Y) {

        Position xRoot = find(X);
        Position yRoot = find(Y);

        // Attach smaller rank tree under root of higher rank tree
        if (subsets.get(xRoot).getRank() < subsets.get(yRoot).getRank()) {
            subsets.get(xRoot).setParent(yRoot);
        } else if (subsets.get(xRoot).getRank() > subsets.get(yRoot).getRank()) {
            subsets.get(yRoot).setParent(xRoot);
        } else {
            // If ranks are same, then make one as root and increment its rank by one
            subsets.get(yRoot).setParent(xRoot);
            subsets.get(xRoot).incrementRank();
        }
    }

    /**
     * It is used to calculate edge cost for given positions.
     */
    private void calculateEdgeCost() {
        // Calculate the edge cost between every goals and start position
        Iterator<Position> goals = goalSet.iterator();
        while (goals.hasNext()) {
            Position nextGoal = goals.next();
            // If goal and start position is same so don't add into edge list
            edges.add( new Edge( startPosition, nextGoal, getEdgeCost(startPosition, nextGoal) ) );
            Iterator<Position> otherGoals = goalSet.iterator();
            while (otherGoals.hasNext()) {
                Position remainingGoal = otherGoals.next();
                // Check if it is already visited or it represents itself
                if (!nextGoal.equals(remainingGoal) && !remainingGoal.isVisited()) {
                    edges.add( new Edge( nextGoal, remainingGoal, getEdgeCost(nextGoal, remainingGoal) ) );
                }
            }
            // Mark the goal as visited or else we will get bidirectional edge
            nextGoal.setVisited(true);
        }
    }

    /**
     * It is used to calculate heuristic value based on Manhattan distance between goal state and current state.
     * Manhattan distance formula is |x1 - x2| + |y1 - y2|
     * 
     * @param goalState
     * @param currentPosition
     * @return long
     */
    private long getEdgeCost(Position goalState, Position currentPosition) {
        return ( ( Math.abs(goalState.getX() - currentPosition.getX()) )
                + ( Math.abs(goalState.getY() - currentPosition.getY()) ) );
    }
}

/**
 * This class represents the edge between different positions. The edge has weight cost.
 * The weight cost is calculated based on manhattan distance.
 *
 */
class Edge
{
    private final Position source;
    private final Position destination;
    private final long weight;

    Edge(Position source, Position destination, long weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "[source=" + source + ", destination=" + destination
                + ", weight=" + weight + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
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
        Edge other = (Edge) obj;
        if (destination == null) {
            if (other.destination != null)
                return false;
        } else if (!destination.equals(other.destination))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        return true;
    }

    public Position getSource() {
        return source;
    }

    public Position getDestination() {
        return destination;
    }

    public long getWeight() {
        return weight;
    }

}

/**
 * It represents the subset formed during process of building Minimum spanning tree.
 * It helps to detect the cycle in building MST.
 * 
 */
class Subset {

    private Position parent;
    long rank;

    Subset(Position parent, long rank) {
        this.parent = parent;
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "Subset [parent=" + parent + ", rank=" + rank + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
        Subset other = (Subset) obj;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        return true;
    }

    public Position getParent() {
        return parent;
    }
    public void setParent(Position parent) {
        this.parent = parent;
    }
    public long getRank() {
        return rank;
    }
    public void incrementRank() {
        this.rank += 1;
    }
}

/**
 * It is used to compare two different edges based on weight. The weight is calculated using Manhattan distance
 *
 */
class EdgeComparator implements Comparator<Edge>
{
    @Override
    public int compare(Edge edge1, Edge edge2)
    {
        if (edge1.getWeight() < edge2.getWeight())
            return -1;
        if (edge1.getWeight() > edge2.getWeight())
            return 1;
        return 0;
    }
}