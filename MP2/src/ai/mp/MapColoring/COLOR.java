package ai.mp.MapColoring;

/**
 * It represent the actual color value
 * 
 * @author rudani2
 *
 */
public enum COLOR {

    RED (0),
    GREEN (1),
    BLUE (2),
    YELLOW (3)
    ;

    private final int colorValue;

    private COLOR (int colorValue) {
        this.colorValue = colorValue;
    }

    public int getColorValue() {
        return this.colorValue;
    }
}
