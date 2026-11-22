package app.coffee.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanTest {

    private static Coffee coffee(String name, double price, double weight, double volume) {
        return new GroundCoffee(name, price, weight, volume, 80, GrindSize.FINE);
    }

    @ParameterizedTest
    @CsvSource({"0, 100", "-1, 100", "10, 0", "10, -5", "NaN, 100"})
    void rejectsInvalidCapacity(double volume, double budget) {
        assertThrows(IllegalArgumentException.class, () -> new Van(volume, budget));
    }

    @Test
    void newVanIsEmpty() {
        Van van = new Van(5, 1000);

        assertEquals(5, van.getMaxVolume());
        assertEquals(1000, van.getMaxBudget());
        assertTrue(van.getCargo().isEmpty());
        assertEquals(0, van.getLoadedVolume());
        assertEquals(0, van.getLoadedCost());
        assertEquals(0, van.getTotalWeight());
    }

    @Test
    void loadsCoffeeAndTracksTotals() {
        Van van = new Van(5, 1000);
        Coffee first = coffee("A", 300, 0.5, 2);
        Coffee second = coffee("B", 200, 0.25, 1.5);

        assertTrue(van.load(first));
        assertTrue(van.load(second));

        assertEquals(List.of(first, second), van.getCargo());
        assertEquals(3.5, van.getLoadedVolume(), 1e-9);
        assertEquals(500, van.getLoadedCost(), 1e-9);
        assertEquals(0.75, van.getTotalWeight(), 1e-9);
    }

    @Test
    void rejectsCoffeeThatExceedsVolume() {
        Van van = new Van(3, 10_000);
        van.load(coffee("A", 100, 1, 2));

        assertFalse(van.canLoad(coffee("B", 100, 1, 1.5)));
        assertFalse(van.load(coffee("B", 100, 1, 1.5)));
        assertEquals(1, van.getCargo().size());
    }

    @Test
    void rejectsCoffeeThatExceedsBudget() {
        Van van = new Van(100, 500);
        van.load(coffee("A", 400, 1, 1));

        assertFalse(van.load(coffee("B", 101, 1, 1)));
        assertTrue(van.load(coffee("C", 100, 1, 1)));
        assertEquals(500, van.getLoadedCost(), 1e-9);
    }

    @Test
    void exactFitIsAllowedDespiteRoundingErrors() {
        Van van = new Van(0.3, 0.3);
        assertTrue(van.load(coffee("A", 0.1, 1, 0.1)));
        assertTrue(van.load(coffee("B", 0.2, 1, 0.2)));
    }

    @Test
    void cargoCannotBeModifiedFromOutside() {
        Van van = new Van(5, 1000);
        van.load(coffee("A", 100, 1, 1));

        assertThrows(UnsupportedOperationException.class, () -> van.getCargo().clear());
    }

    @Test
    void rejectsNullCoffee() {
        assertThrows(NullPointerException.class, () -> new Van(5, 1000).load(null));
    }
}
