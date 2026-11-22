package app.coffee.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoffeeTest {

    private static CoffeeBeans beans(String name, double price, double weight, double volume, int quality) {
        return new CoffeeBeans(name, price, weight, volume, quality, RoastLevel.MEDIUM);
    }

    @Test
    void storesAllParameters() {
        Coffee coffee = beans("Colombia", 1500, 1.05, 2.5, 95);

        assertEquals("Colombia", coffee.getName());
        assertEquals(1500, coffee.getPrice());
        assertEquals(1.05, coffee.getWeightWithPackaging());
        assertEquals(2.5, coffee.getVolumeWithPackaging());
        assertEquals(95, coffee.getQualityScore());
    }

    @Test
    void priceToWeightRatioUsesWeightWithPackaging() {
        assertEquals(1000.0, beans("A", 500, 0.5, 1, 50).getPriceToWeightRatio(), 1e-9);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankName(String name) {
        assertThrows(IllegalArgumentException.class, () -> beans(name, 100, 1, 1, 50));
    }

    @ParameterizedTest(name = "price={0}, weight={1}, volume={2}")
    @CsvSource({
            "0, 1, 1",
            "-5, 1, 1",
            "100, 0, 1",
            "100, -1, 1",
            "100, 1, 0",
            "100, 1, -0.5",
            "NaN, 1, 1",
            "Infinity, 1, 1",
    })
    void rejectsNonPositiveNumbers(double price, double weight, double volume) {
        assertThrows(IllegalArgumentException.class, () -> beans("A", price, weight, volume, 50));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101, 500})
    void rejectsQualityOutOfRange(int quality) {
        assertThrows(IllegalArgumentException.class, () -> beans("A", 100, 1, 1, quality));
    }

    @ParameterizedTest
    @ValueSource(ints = {Coffee.MIN_QUALITY, Coffee.MAX_QUALITY})
    void acceptsQualityBounds(int quality) {
        assertEquals(quality, beans("A", 100, 1, 1, quality).getQualityScore());
    }

    @Test
    void eachPhysicalStateDescribesItself() {
        CoffeeBeans beans = new CoffeeBeans("A", 1, 1, 1, 1, RoastLevel.DARK);
        GroundCoffee ground = new GroundCoffee("B", 1, 1, 1, 1, GrindSize.COARSE);
        InstantCoffee jar = new InstantCoffee("C", 1, 1, 1, 1, InstantPackaging.JAR);
        InstantCoffee sachet = new InstantCoffee("D", 1, 1, 1, 1, InstantPackaging.SACHET);

        assertEquals("Кава в зернах (обсмаження: темне)", beans.getCoffeeType());
        assertEquals("Мелена кава (помол: крупний)", ground.getCoffeeType());
        assertEquals("Розчинна кава (скляна банка)", jar.getCoffeeType());
        assertEquals("Розчинна кава (пакетик)", sachet.getCoffeeType());

        assertEquals(RoastLevel.DARK, beans.getRoastLevel());
        assertEquals(GrindSize.COARSE, ground.getGrindSize());
        assertEquals(InstantPackaging.SACHET, sachet.getPackaging());
    }

    @Test
    void subclassesRequireTheirVariant() {
        assertThrows(NullPointerException.class, () -> new CoffeeBeans("A", 1, 1, 1, 1, null));
        assertThrows(NullPointerException.class, () -> new GroundCoffee("A", 1, 1, 1, 1, null));
        assertThrows(NullPointerException.class, () -> new InstantCoffee("A", 1, 1, 1, 1, null));
    }

    @Test
    void enumsHaveUkrainianNames() {
        assertEquals("світле", RoastLevel.LIGHT.getDisplayName());
        assertEquals("середнє", RoastLevel.MEDIUM.getDisplayName());
        assertEquals("дрібний", GrindSize.FINE.getDisplayName());
        assertEquals("середній", GrindSize.MEDIUM.getDisplayName());
    }

    @Test
    void toStringContainsMainParameters() {
        String text = beans("Colombia", 1500, 1.25, 2.5, 95).toString();

        assertTrue(text.startsWith("Кава в зернах (обсмаження: середнє) 'Colombia'"), text);
        assertTrue(text.contains("ціна: 1500.00 грн"), text);
        assertTrue(text.contains("ціна/вага: 1200.00 грн/кг"), text);
        assertTrue(text.contains("об'єм: 2.50 л"), text);
        assertTrue(text.contains("якість: 95"), text);
    }

    @Test
    void equalsAndHashCodeCompareAllFields() {
        Coffee coffee = beans("A", 100, 1, 1, 50);

        assertEquals(coffee, coffee);
        assertEquals(beans("A", 100, 1, 1, 50), coffee);
        assertEquals(beans("A", 100, 1, 1, 50).hashCode(), coffee.hashCode());

        assertNotEquals(beans("B", 100, 1, 1, 50), coffee);
        assertNotEquals(beans("A", 101, 1, 1, 50), coffee);
        assertNotEquals(beans("A", 100, 2, 1, 50), coffee);
        assertNotEquals(beans("A", 100, 1, 2, 50), coffee);
        assertNotEquals(beans("A", 100, 1, 1, 51), coffee);
        assertNotEquals(new CoffeeBeans("A", 100, 1, 1, 50, RoastLevel.DARK), coffee);
        assertNotEquals(new GroundCoffee("A", 100, 1, 1, 50, GrindSize.MEDIUM), coffee);
        assertNotEquals(null, coffee);
        assertNotEquals("A", coffee);
    }
}
