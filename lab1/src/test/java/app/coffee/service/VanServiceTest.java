package app.coffee.service;

import app.coffee.model.Coffee;
import app.coffee.model.Van;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class VanServiceTest {

    private final VanService vanService = new VanService();

    @Mock
    private Van van;

    /**
     * Товар-заглушка для завантаження: фургону важливі лише ціна та об'єм.
     * Нестрогий режим, бо фургон не перевіряє ціну, якщо товар уже не вмістився за об'ємом.
     */
    private static Coffee sized(double price, double volume) {
        Coffee coffee = mock(Coffee.class, withSettings().strictness(Strictness.LENIENT));
        when(coffee.getPrice()).thenReturn(price);
        when(coffee.getVolumeWithPackaging()).thenReturn(volume);
        return coffee;
    }

    private static Coffee withRatio(double ratio) {
        Coffee coffee = mock(Coffee.class);
        when(coffee.getPriceToWeightRatio()).thenReturn(ratio);
        return coffee;
    }

    private static Coffee withQuality(int quality) {
        Coffee coffee = mock(Coffee.class);
        when(coffee.getQualityScore()).thenReturn(quality);
        return coffee;
    }

    // --- loadVan ---

    @Test
    void loadVanOffersEveryItemToTheVanInStockOrder() {
        Coffee first = mock(Coffee.class);
        Coffee second = mock(Coffee.class);

        Van result = vanService.loadVan(List.of(first, second), van);

        assertSame(van, result);
        InOrder order = inOrder(van);
        order.verify(van).load(first);
        order.verify(van).load(second);
    }

    @Test
    void loadVanSkipsItemsOverBudgetAndContinues() {
        Coffee expensive = sized(5000, 2);
        Coffee cheap = sized(500, 2);

        Van loaded = vanService.loadVan(List.of(expensive, cheap), new Van(10, 1000));

        assertEquals(List.of(cheap), loaded.getCargo());
    }

    @Test
    void loadVanSkipsItemsOverVolumeAndContinues() {
        Coffee bulky = sized(500, 15);
        Coffee small = sized(100, 1);

        Van loaded = vanService.loadVan(List.of(bulky, small), new Van(5, 10_000));

        assertEquals(List.of(small), loaded.getCargo());
    }

    @Test
    void loadVanWithNothingThatFits() {
        Van loaded = vanService.loadVan(List.of(sized(5000, 15)), new Van(1, 100));
        assertTrue(loaded.getCargo().isEmpty());
    }

    @Test
    void loadVanWithEmptyStock() {
        assertTrue(vanService.loadVan(List.of(), new Van(1, 100)).getCargo().isEmpty());
    }

    @Test
    void loadVanRejectsNulls() {
        assertThrows(NullPointerException.class, () -> vanService.loadVan(null, van));
        assertThrows(NullPointerException.class, () -> vanService.loadVan(List.of(), null));
    }

    // --- sortByPriceToWeightRatio ---

    @Test
    void sortsByPriceToWeightRatioAscending() {
        Coffee middle = withRatio(1200);
        Coffee cheapest = withRatio(500);
        Coffee dearest = withRatio(1730);

        List<Coffee> sorted = vanService.sortByPriceToWeightRatio(List.of(middle, cheapest, dearest));

        assertEquals(List.of(cheapest, middle, dearest), sorted);
    }

    @Test
    void sortingDoesNotChangeTheSourceList() {
        Coffee second = withRatio(2);
        Coffee first = withRatio(1);
        List<Coffee> source = new ArrayList<>(List.of(second, first));

        vanService.sortByPriceToWeightRatio(source);

        assertEquals(List.of(second, first), source);
    }

    @Test
    void sortingEmptyListGivesEmptyList() {
        assertTrue(vanService.sortByPriceToWeightRatio(List.of()).isEmpty());
        assertThrows(NullPointerException.class, () -> vanService.sortByPriceToWeightRatio(null));
    }

    // --- findCoffeeByQuality ---

    @Test
    void findsCoffeeInRangeInclusive() {
        Coffee low = withQuality(79);
        Coffee lowerBound = withQuality(80);
        Coffee upperBound = withQuality(90);
        Coffee high = withQuality(91);

        List<Coffee> found = vanService.findCoffeeByQuality(List.of(low, lowerBound, upperBound, high), 80, 90);

        assertEquals(List.of(lowerBound, upperBound), found);
    }

    @Test
    void findReturnsEmptyListWhenNothingMatches() {
        assertTrue(vanService.findCoffeeByQuality(List.of(withQuality(75)), 90, 100).isEmpty());
    }

    @Test
    void findReturnsAllWhenEverythingMatches() {
        List<Coffee> all = List.of(withQuality(85), withQuality(90));
        assertEquals(all, vanService.findCoffeeByQuality(all, 80, 100));
    }

    @ParameterizedTest(name = "[{0}, {1}]")
    @CsvSource({"-1, 50", "0, 101", "90, 80"})
    void findRejectsInvalidRange(int min, int max) {
        assertThrows(IllegalArgumentException.class, () -> vanService.findCoffeeByQuality(List.of(), min, max));
    }

    @Test
    void findRejectsNullList() {
        assertThrows(NullPointerException.class, () -> vanService.findCoffeeByQuality(null, 0, 100));
    }
}
