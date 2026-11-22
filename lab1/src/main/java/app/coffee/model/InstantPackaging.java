package app.coffee.model;

/** Упаковка розчинної кави: у банках або в пакетиках. */
public enum InstantPackaging {
    JAR("скляна банка"),
    SACHET("пакетик");

    private final String displayName;

    InstantPackaging(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
