package app.coffee.model;

public class CoffeeBeans extends Coffee {
    private String roastLevel;

    public CoffeeBeans(String name, double price, double weightWithPackaging, double volume, int qualityScore, String roastLevel) {
        super(name, price, weightWithPackaging, volume, qualityScore);
        this.roastLevel = roastLevel;
    }

    @Override
    public String getCoffeeType() {
        return "Кава в зернах (" + roastLevel + ")";
    }
}
