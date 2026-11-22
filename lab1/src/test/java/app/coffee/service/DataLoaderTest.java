package app.coffee.service;

import app.coffee.model.Coffee;
import app.coffee.model.CoffeeBeans;
import app.coffee.model.GrindSize;
import app.coffee.model.GroundCoffee;
import app.coffee.model.InstantCoffee;
import app.coffee.model.InstantPackaging;
import app.coffee.model.RoastLevel;
import app.coffee.model.Van;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataLoaderTest {

    private final DataLoader loader = new DataLoader();

    private List<Coffee> parse(String csv) {
        return loader.parseStock(new StringReader(csv));
    }

    private Van parseVan(String properties) {
        return loader.parseVan(new StringReader(properties));
    }

    // --- асортимент ---

    @Test
    void loadsStockFromBundledFile() {
        List<Coffee> stock = loader.loadStock();

        assertEquals(6, stock.size());
        assertEquals("Colombia Supremo", stock.get(0).getName());
        assertTrue(stock.stream().anyMatch(CoffeeBeans.class::isInstance));
        assertTrue(stock.stream().anyMatch(GroundCoffee.class::isInstance));
        assertTrue(stock.stream().anyMatch(c -> c instanceof InstantCoffee i && i.getPackaging() == InstantPackaging.JAR));
        assertTrue(stock.stream().anyMatch(c -> c instanceof InstantCoffee i && i.getPackaging() == InstantPackaging.SACHET));
    }

    @Test
    void parsesEveryCoffeeType() {
        List<Coffee> stock = parse("""
                BEANS;Colombia;1500;1.05;2.5;95;MEDIUM
                GROUND;Espresso;900;0.52;1.2;88;FINE
                INSTANT;Gold;300;0.25;0.8;60;JAR
                """);

        assertEquals(List.of(
                new CoffeeBeans("Colombia", 1500, 1.05, 2.5, 95, RoastLevel.MEDIUM),
                new GroundCoffee("Espresso", 900, 0.52, 1.2, 88, GrindSize.FINE),
                new InstantCoffee("Gold", 300, 0.25, 0.8, 60, InstantPackaging.JAR)), stock);
    }

    @Test
    void skipsCommentsAndBlankLinesAndTrimsFields() {
        List<Coffee> stock = parse("""
                # коментар

                  INSTANT ; Sachet ; 10 ; 0.02 ; 0.05 ; 40 ; SACHET \s
                """);

        assertEquals(List.of(new InstantCoffee("Sachet", 10, 0.02, 0.05, 40, InstantPackaging.SACHET)), stock);
    }

    @Test
    void emptyFileGivesEmptyStock() {
        assertTrue(parse("").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "BEANS;Colombia;1500;1.05;2.5;95",           // мало полів
            "BEANS;Colombia;1500;1.05;2.5;95;MEDIUM;x",  // забагато полів
            "TEA;Green;100;1;1;50;MEDIUM",               // невідомий тип
            "BEANS;Colombia;дорого;1.05;2.5;95;MEDIUM",  // не число
            "BEANS;Colombia;1500;1.05;2.5;9.5;MEDIUM",   // якість не ціле
            "BEANS;Colombia;1500;1.05;2.5;95;BURNT",     // невідоме обсмаження
            "GROUND;Espresso;900;0.52;1.2;88;HUGE",      // невідомий помол
            "INSTANT;Gold;300;0.25;0.8;60;BOX",          // невідома упаковка
            "BEANS;Colombia;1500;1.05;2.5;150;MEDIUM",   // якість поза шкалою
            "BEANS;;1500;1.05;2.5;95;MEDIUM",            // порожня назва
    })
    void reportsInvalidLineWithItsNumber(String badLine) {
        DataLoadException e = assertThrows(DataLoadException.class,
                () -> parse("# header\nBEANS;Ok;1;1;1;1;LIGHT\n" + badLine));

        assertTrue(e.getMessage().startsWith("Line 3:"), e.getMessage());
    }

    @Test
    void wrapsReadErrors() throws IOException {
        Reader broken = mock(Reader.class);
        when(broken.read(any(char[].class), anyInt(), anyInt())).thenThrow(new IOException("disk error"));

        DataLoadException e = assertThrows(DataLoadException.class, () -> loader.parseStock(broken));
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    void missingResourceIsReported() {
        DataLoadException e = assertThrows(DataLoadException.class, () -> loader.loadStock("/no-such-file.csv"));
        assertTrue(e.getMessage().contains("/no-such-file.csv"));
        assertThrows(DataLoadException.class, () -> loader.loadVan("/no-such-file.properties"));
    }

    // --- параметри фургона ---

    @Test
    void loadsVanFromBundledFile() {
        Van van = loader.loadVan();

        assertEquals(5.0, van.getMaxVolume());
        assertEquals(3500.0, van.getMaxBudget());
        assertTrue(van.getCargo().isEmpty());
    }

    @Test
    void parsesVanParameters() {
        Van van = parseVan("van.maxVolume = 7.5\nvan.maxBudget = 1200\n");

        assertEquals(7.5, van.getMaxVolume());
        assertEquals(1200, van.getMaxBudget());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "van.maxBudget=100",                     // немає об'єму
            "van.maxVolume=5",                       // немає суми
            "van.maxVolume=five\nvan.maxBudget=100", // не число
            "van.maxVolume=-5\nvan.maxBudget=100",   // від'ємний об'єм
    })
    void rejectsInvalidVanParameters(String properties) {
        DataLoadException e = assertThrows(DataLoadException.class, () -> parseVan(properties));
        assertFalse(e.getMessage().isBlank());
    }

    @Test
    void wrapsVanReadErrors() throws IOException {
        Reader broken = mock(Reader.class);
        when(broken.read(any(char[].class), anyInt(), anyInt())).thenThrow(new IOException("disk error"));

        assertThrows(DataLoadException.class, () -> loader.parseVan(broken));
    }
}
