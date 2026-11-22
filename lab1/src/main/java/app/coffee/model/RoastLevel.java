package app.coffee.model;

/** Ступінь обсмаження кавових зерен. */
public enum RoastLevel {
    LIGHT("світле"),
    MEDIUM("середнє"),
    DARK("темне");

    private final String displayName;

    RoastLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
