package app.coffee;

import app.coffee.model.Coffee;
import app.coffee.model.CoffeeBeans;
import app.coffee.model.GroundCoffee;
import app.coffee.model.GrindSize;
import app.coffee.model.InstantCoffee;
import app.coffee.model.InstantPackaging;
import app.coffee.model.RoastLevel;
import app.coffee.model.Van;
import app.coffee.service.DataLoader;
import app.coffee.service.VanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoffeeVanAppTest {

    @Mock
    private DataLoader dataLoader;

    @Spy
    private VanService vanService;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    private String runApp(List<Coffee> stock, Van van) {
        when(dataLoader.loadStock()).thenReturn(stock);
        when(dataLoader.loadVan()).thenReturn(van);
        new CoffeeVanApp(dataLoader, vanService, out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void loadsSortsAndSearchesUsingDataFromLoader() {
        Coffee premium = new CoffeeBeans("Ethiopia", 1800, 1.05, 2.5, 98, RoastLevel.LIGHT);
        Coffee cheap = new InstantCoffee("Sachet", 10, 0.02, 0.05, 40, InstantPackaging.SACHET);
        Coffee tooBig = new GroundCoffee("Huge sack", 900, 10, 50, 85, GrindSize.COARSE);
        Van van = new Van(5, 3000);

        String output = runApp(List.of(premium, cheap, tooBig), van);

        verify(dataLoader).loadStock();
        verify(dataLoader).loadVan();
        verify(vanService).loadVan(List.of(premium, cheap, tooBig), van);
        verify(vanService).sortByPriceToWeightRatio(van.getCargo());
        verify(vanService).findCoffeeByQuality(van.getCargo(), 90, 100);

        assertTrue(output.contains("Завантажено 2 з 3 товарів"), output);
        assertTrue(!output.contains("Huge sack"), output);
        int sortedSection = output.indexOf("Сортування");
        assertTrue(output.indexOf("'Sachet'", sortedSection) < output.indexOf("'Ethiopia'", sortedSection), output);
        String premiumSection = output.substring(output.indexOf("преміум"));
        assertTrue(premiumSection.contains("'Ethiopia'") && !premiumSection.contains("'Sachet'"), output);
    }

    @Test
    void reportsWhenNoPremiumCoffeeFound() {
        String output = runApp(List.of(new InstantCoffee("Sachet", 10, 0.02, 0.05, 40, InstantPackaging.SACHET)),
                new Van(5, 3000));

        assertTrue(output.contains("Не знайдено"), output);
    }

    @Test
    void requiresAllDependencies() {
        assertThrows(NullPointerException.class, () -> new CoffeeVanApp(null, vanService, out));
        assertThrows(NullPointerException.class, () -> new CoffeeVanApp(dataLoader, null, out));
        assertThrows(NullPointerException.class, () -> new CoffeeVanApp(dataLoader, vanService, null));
    }
}
