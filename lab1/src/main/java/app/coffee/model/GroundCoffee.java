package app.coffee.model;

public class GroundCoffee extends Coffee {
    private String grindSize;

    public GroundCoffee(String name, double price, double weightWithPackaging, double volume, int qualityScore, String grindSize) {
        super(name, price, weightWithPackaging, volume, qualityScore);
        this.grindSize = grindSize;
    }

    @Override
    public String getCoffeeType() {
        return "Мелена кава (Помол: " + grindSize + ")";
    }
}