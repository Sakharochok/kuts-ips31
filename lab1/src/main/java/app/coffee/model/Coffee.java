package app.coffee.model;

import java.util.Locale;
import java.util.Objects;

/**
 * Товар-кава у фургоні. Вага та об'єм враховуються разом з упаковкою,
 * бо саме вони займають місце у фургоні.
 */
public abstract class Coffee {

    public static final int MIN_QUALITY = 0;
    public static final int MAX_QUALITY = 100;

    private final String name;
    private final double price;
    private final double weightWithPackaging;
    private final double volumeWithPackaging;
    private final int qualityScore;

    /**
     * @param name                назва сорту
     * @param price               ціна, грн (більше нуля)
     * @param weightWithPackaging вага з упаковкою, кг (більше нуля)
     * @param volumeWithPackaging об'єм з упаковкою, л (більше нуля)
     * @param qualityScore        оцінка якості від {@value #MIN_QUALITY} до {@value #MAX_QUALITY}
     * @throws IllegalArgumentException якщо параметри некоректні
     */
    protected Coffee(String name, double price, double weightWithPackaging,
                     double volumeWithPackaging, int qualityScore) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Coffee name must not be blank");
        }
        requirePositive(price, "price");
        requirePositive(weightWithPackaging, "weightWithPackaging");
        requirePositive(volumeWithPackaging, "volumeWithPackaging");
        if (qualityScore < MIN_QUALITY || qualityScore > MAX_QUALITY) {
            throw new IllegalArgumentException("qualityScore must be in [" + MIN_QUALITY + ", "
                    + MAX_QUALITY + "], got " + qualityScore);
        }
        this.name = name;
        this.price = price;
        this.weightWithPackaging = weightWithPackaging;
        this.volumeWithPackaging = volumeWithPackaging;
        this.qualityScore = qualityScore;
    }

    private static void requirePositive(double value, String field) {
        if (!(value > 0) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(field + " must be a positive number, got " + value);
        }
    }

    /** Ціна за кілограм ваги разом з упаковкою, грн/кг. */
    public double getPriceToWeightRatio() {
        return price / weightWithPackaging;
    }

    /** Опис фізичного стану кави (зерно, мелена, розчинна) з його особливістю. */
    public abstract String getCoffeeType();

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getWeightWithPackaging() {
        return weightWithPackaging;
    }

    public double getVolumeWithPackaging() {
        return volumeWithPackaging;
    }

    public int getQualityScore() {
        return qualityScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Coffee coffee = (Coffee) o;
        return Double.compare(price, coffee.price) == 0
                && Double.compare(weightWithPackaging, coffee.weightWithPackaging) == 0
                && Double.compare(volumeWithPackaging, coffee.volumeWithPackaging) == 0
                && qualityScore == coffee.qualityScore
                && name.equals(coffee.name)
                && getCoffeeType().equals(coffee.getCoffeeType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, weightWithPackaging, volumeWithPackaging, qualityScore, getCoffeeType());
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "%s '%s' | ціна: %.2f грн | вага: %.2f кг | ціна/вага: %.2f грн/кг | об'єм: %.2f л | якість: %d",
                getCoffeeType(), name, price, weightWithPackaging, getPriceToWeightRatio(),
                volumeWithPackaging, qualityScore);
    }
}
