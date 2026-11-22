package app.coffee.model;

/** Ступінь помелу меленої кави. */
public enum GrindSize {
    FINE("дрібний"),
    MEDIUM("середній"),
    COARSE("крупний");

    private final String displayName;

    GrindSize(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
