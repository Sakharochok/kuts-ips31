package app.coffee.model;

import java.util.Objects;

/** Кава в зернах. */
public class CoffeeBeans extends Coffee {

    private final RoastLevel roastLevel;

    public CoffeeBeans(String name, double price, double weightWithPackaging, double volumeWithPackaging,
                       int qualityScore, RoastLevel roastLevel) {
        super(name, price, weightWithPackaging, volumeWithPackaging, qualityScore);
        this.roastLevel = Objects.requireNonNull(roastLevel, "roastLevel");
    }

    public RoastLevel getRoastLevel() {
        return roastLevel;
    }

    @Override
    public String getCoffeeType() {
        return "Кава в зернах (обсмаження: " + roastLevel.getDisplayName() + ")";
    }
}
