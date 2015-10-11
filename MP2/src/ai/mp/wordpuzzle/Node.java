package ai.mp.wordpuzzle;

/**
 * This class is used to hold the node which has following information
 * <pre>
 * 1. String (single character or group of character)
 * 2. Parent (Parent of each node)
 * </pre>
 *  
 * @author rudani2
 *
 */
public class Node {

    private final String value;
    private Node parent;
    private String category;

    Node(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" + value + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        Node other = (Node) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
