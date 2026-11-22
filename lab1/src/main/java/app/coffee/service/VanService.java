package app.coffee.service;

import app.coffee.model.Coffee;
import app.coffee.model.Van;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Операції з вантажем фургона: завантаження, сортування та пошук. */
public class VanService {

    /**
     * Завантажує товари зі складу по черзі; товар, що не вміщується за об'ємом
     * чи сумою, пропускається, і завантаження продовжується наступним.
     *
     * @return той самий фургон із завантаженим вантажем
     */
    public Van loadVan(List<Coffee> availableStock, Van van) {
        Objects.requireNonNull(availableStock, "availableStock");
        Objects.requireNonNull(van, "van");
        for (Coffee coffee : availableStock) {
            van.load(coffee);
        }
        return van;
    }

    /** Повертає новий список, відсортований за зростанням співвідношення ціни до ваги. */
    public List<Coffee> sortByPriceToWeightRatio(List<Coffee> coffeeList) {
        Objects.requireNonNull(coffeeList, "coffeeList");
        return coffeeList.stream()
                .sorted(Comparator.comparingDouble(Coffee::getPriceToWeightRatio))
                .toList();
    }

    /**
     * Знаходить товари з оцінкою якості в діапазоні {@code [minQuality, maxQuality]} включно.
     *
     * @throws IllegalArgumentException якщо діапазон виходить за межі шкали або min більше за max
     */
    public List<Coffee> findCoffeeByQuality(List<Coffee> coffeeList, int minQuality, int maxQuality) {
        Objects.requireNonNull(coffeeList, "coffeeList");
        if (minQuality < Coffee.MIN_QUALITY || maxQuality > Coffee.MAX_QUALITY || minQuality > maxQuality) {
            throw new IllegalArgumentException("Invalid quality range [" + minQuality + ", " + maxQuality + "]");
        }
        return coffeeList.stream()
                .filter(coffee -> coffee.getQualityScore() >= minQuality && coffee.getQualityScore() <= maxQuality)
                .toList();
    }
}
