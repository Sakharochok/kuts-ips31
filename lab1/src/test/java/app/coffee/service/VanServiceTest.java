package app.coffee.service;

import app.coffee.model.Coffee;
import app.coffee.model.CoffeeBeans;
import app.coffee.model.GroundCoffee;
import app.coffee.model.InstantCoffee;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VanServiceTest {

    private final VanService vanService = new VanService();

    @Test
    public void testLoadVan_LimitsBudget() {
        List<Coffee> stock = Arrays.asList(
            new CoffeeBeans("Expensive Beans", 5000, 1.0, 2.0, 90, "Medium"),
            new GroundCoffee("Cheap Ground", 500, 1.0, 2.0, 70, "Fine")
        );
        List<Coffee> loaded = vanService.loadVan(stock, 10.0, 1000.0);
        assertEquals(1, loaded.size());
        assertEquals("Cheap Ground", loaded.get(0).getName());
    }

    @Test
    public void testLoadVan_EmptyStock() {
        List<Coffee> emptyStock = new ArrayList<>();
        List<Coffee> loaded = vanService.loadVan(emptyStock, 10.0, 1000.0);
        assertTrue(loaded.isEmpty());
    }

    @Test
    public void testLoadVan_WeightLimit() {
        List<Coffee> stock = Arrays.asList(
            new CoffeeBeans("Heavy Beans", 500, 10.0, 15.0, 80, "Dark"),
            new InstantCoffee("Light Instant", 100, 0.5, 1.0, 60, "Spray")
        );
        List<Coffee> loaded = vanService.loadVan(stock, 5.0, 10000.0);
        assertEquals(1, loaded.size());
        assertEquals("Light Instant", loaded.get(0).getName());
    }

    @Test
    public void testLoadVan_NothingFits() {
        List<Coffee> stock = Arrays.asList(
            new CoffeeBeans("Heavy", 5000, 10.0, 15.0, 80, "Dark")
        );
        List<Coffee> loaded = vanService.loadVan(stock, 1.0, 100.0);
        assertTrue(loaded.isEmpty());
    }

    @Test
    public void testFindCoffeeByQuality_Standard() {
        List<Coffee> stock = Arrays.asList(
            new CoffeeBeans("High Quality", 500, 1.0, 2.0, 95, "Medium"),
            new GroundCoffee("Low Quality", 500, 1.0, 2.0, 60, "Fine")
        );
        List<Coffee> result = vanService.findCoffeeByQuality(stock, 80, 100);
        assertEquals(1, result.size());
        assertEquals("High Quality", result.get(0).getName());
    }

    @Test
    public void testFindCoffeeByQuality_NoneMatch() {
        List<Coffee> stock = Arrays.asList(
            new CoffeeBeans("Average", 500, 1.0, 2.0, 75, "Medium")
        );
        List<Coffee> result = vanService.findCoffeeByQuality(stock, 90, 100);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindCoffeeByQuality_AllMatch() {
        List<Coffee> stock = Arrays.asList(
            new CoffeeBeans("Gomd", 500, 1.0, 2.0, 85, "Medium"),
            new GroundCoffee("Better", 500, 1.0, 2.0, 90, "Fine")
        );
        List<Coffee> result = vanService.findCoffeeByQuality(stock, 80, 100);
        assertEquals(2, result.size());
    }
}
