package app.coffee.model;

public abstract class Coffee {
    private String name;
    private double price;
    private double weightWithPackaging;
    private double volume;
    private int qualityScore;

    public Coffee(String name, double price, double weightWithPackaging, double volume, int qualityScore) {
        this.name = name;
        this.price = price;
        this.weightWithPackaging = weightWithPackaging;
        this.volume = volume;
        this.qualityScore = qualityScore;
    }

    public double getPriceToWeightRatio() {
        return price / weightWithPackaging;
    }

    public abstract String getCoffeeType();

    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getWeightWithPackaging() { return weightWithPackaging; }
    public double getVolume() { return volume; }
    public int getQualityScore() { return qualityScore; }

    @Override
    public String toString() {
        return String.format("%s '%s' | Ціна/Вага: %.2f | Об'єм: %.2f | Якість: %d",
                getCoffeeType(), name, getPriceToWeightRatio(), volume, qualityScore);
    }
}