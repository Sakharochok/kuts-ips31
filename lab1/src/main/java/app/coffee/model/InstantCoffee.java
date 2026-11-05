package app.coffee.model;

public class InstantCoffee extends Coffee {
    private String packagingType;

    public InstantCoffee(String name, double price, double weightWithPackaging, double volume, int qualityScore, String packagingType) {
        super(name, price, weightWithPackaging, volume, qualityScore);
        this.packagingType = packagingType;
    }

    @Override
    public String getCoffeeType() {
        return "Розчинна кава (" + packagingType + ")";
    }
}