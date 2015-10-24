package ai.mp.MapColoring;

/**
 * It is used to represent color information
 * 
 * @author rudani2
 *
 */
public class ColorInfo {

    private final COLOR color;

    ColorInfo(COLOR color) {
        this.color = color;
    }

    public COLOR getColor() {
        return color;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
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
        ColorInfo other = (ColorInfo) obj;
        if (color != other.color)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ColorInfo [color=" + color + "]";
    }
}
