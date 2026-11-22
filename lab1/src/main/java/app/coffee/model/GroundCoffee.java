package app.coffee.model;

import java.util.Objects;

/** Мелена кава. */
public class GroundCoffee extends Coffee {

    private final GrindSize grindSize;

    public GroundCoffee(String name, double price, double weightWithPackaging, double volumeWithPackaging,
                        int qualityScore, GrindSize grindSize) {
        super(name, price, weightWithPackaging, volumeWithPackaging, qualityScore);
        this.grindSize = Objects.requireNonNull(grindSize, "grindSize");
    }

    public GrindSize getGrindSize() {
        return grindSize;
    }

    @Override
    public String getCoffeeType() {
        return "Мелена кава (помол: " + grindSize.getDisplayName() + ")";
    }
}
