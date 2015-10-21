package ai.mp.MapColoring;

/**
 * It is used to represent segment between two regions.
 * 
 * @author rudani2
 *
 */
public class Segment {

    private final Region fromRegion;
    private final Region toRegion;

    Segment(Region fromRegion, Region toRegion) {
        this.fromRegion = fromRegion;
        this.toRegion = toRegion;
    }

    @Override
    public String toString() {
        return "Segment [fromRegion=" + fromRegion + ", toRegion=" + toRegion
                + "]\n";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fromRegion == null) ? 0 : fromRegion.hashCode());
        result = prime * result
                + ((toRegion == null) ? 0 : toRegion.hashCode());
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
        Segment other = (Segment) obj;
        if (fromRegion == null) {
            if (other.fromRegion != null)
                return false;
        } else if (!fromRegion.equals(other.fromRegion))
            return false;
        if (toRegion == null) {
            if (other.toRegion != null)
                return false;
        } else if (!toRegion.equals(other.toRegion))
            return false;
        return true;
    }

    public Region getFromRegion() {
        return fromRegion;
    }

    public Region getToRegion() {
        return toRegion;
    }

}
