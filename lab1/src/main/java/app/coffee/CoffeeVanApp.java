package app.coffee;

import app.coffee.model.Coffee;
import app.coffee.model.Van;
import app.coffee.service.DataLoader;
import app.coffee.service.VanService;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Сценарій роботи застосунку: завантажити фургон, відсортувати товари
 * за співвідношенням ціни до ваги та знайти каву преміальної якості.
 */
public class CoffeeVanApp {

    static final int PREMIUM_MIN_QUALITY = 90;
    static final int PREMIUM_MAX_QUALITY = 100;

    private final DataLoader dataLoader;
    private final VanService vanService;
    private final PrintStream out;

    public CoffeeVanApp(DataLoader dataLoader, VanService vanService, PrintStream out) {
        this.dataLoader = Objects.requireNonNull(dataLoader, "dataLoader");
        this.vanService = Objects.requireNonNull(vanService, "vanService");
        this.out = Objects.requireNonNull(out, "out");
    }

    public void run() {
        List<Coffee> stock = dataLoader.loadStock();
        Van van = vanService.loadVan(stock, dataLoader.loadVan());

        out.println("--- Товари у фургоні ---");
        printAll(van.getCargo());
        out.printf(Locale.ROOT, "Завантажено %d з %d товарів: об'єм %.2f / %.2f л, сума %.2f / %.2f грн, вага %.2f кг%n",
                van.getCargo().size(), stock.size(), van.getLoadedVolume(), van.getMaxVolume(),
                van.getLoadedCost(), van.getMaxBudget(), van.getTotalWeight());

        out.println();
        out.println("--- Сортування за співвідношенням ціна/вага ---");
        printAll(vanService.sortByPriceToWeightRatio(van.getCargo()));

        out.println();
        out.printf("--- Кава преміум-якості (%d-%d балів) ---%n", PREMIUM_MIN_QUALITY, PREMIUM_MAX_QUALITY);
        List<Coffee> premium = vanService.findCoffeeByQuality(van.getCargo(), PREMIUM_MIN_QUALITY, PREMIUM_MAX_QUALITY);
        if (premium.isEmpty()) {
            out.println("Не знайдено");
        } else {
            printAll(premium);
        }
    }

    private void printAll(List<Coffee> coffeeList) {
        coffeeList.forEach(out::println);
    }
}
