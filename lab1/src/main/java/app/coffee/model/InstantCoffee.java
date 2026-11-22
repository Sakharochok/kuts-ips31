package app.coffee.model;

import java.util.Objects;

/** Розчинна кава в банках або пакетиках. */
public class InstantCoffee extends Coffee {

    private final InstantPackaging packaging;

    public InstantCoffee(String name, double price, double weightWithPackaging, double volumeWithPackaging,
                         int qualityScore, InstantPackaging packaging) {
        super(name, price, weightWithPackaging, volumeWithPackaging, qualityScore);
        this.packaging = Objects.requireNonNull(packaging, "packaging");
    }

    public InstantPackaging getPackaging() {
        return packaging;
    }

    @Override
    public String getCoffeeType() {
        return "Розчинна кава (" + packaging.getDisplayName() + ")";
    }
}
