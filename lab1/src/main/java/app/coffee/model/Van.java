package app.coffee.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Фургон заданого об'єму, який завантажують кавою не більше ніж на задану суму.
 */
public class Van {

    /** Похибка порівняння дробових об'ємів і сум (щоб 0.1 + 0.2 "вміщалося" у 0.3). */
    private static final double EPSILON = 1e-9;

    private final double maxVolume;
    private final double maxBudget;
    private final List<Coffee> cargo = new ArrayList<>();
    private double loadedVolume;
    private double loadedCost;

    /**
     * @param maxVolume максимальний об'єм вантажу, л (більше нуля)
     * @param maxBudget максимальна сума вантажу, грн (більше нуля)
     */
    public Van(double maxVolume, double maxBudget) {
        if (!(maxVolume > 0) || !(maxBudget > 0)) {
            throw new IllegalArgumentException(
                    "Van volume and budget must be positive, got " + maxVolume + " and " + maxBudget);
        }
        this.maxVolume = maxVolume;
        this.maxBudget = maxBudget;
    }

    /** Чи вміститься товар у фургон за об'ємом і чи вистачить суми. */
    public boolean canLoad(Coffee coffee) {
        Objects.requireNonNull(coffee, "coffee");
        return loadedVolume + coffee.getVolumeWithPackaging() <= maxVolume + EPSILON
                && loadedCost + coffee.getPrice() <= maxBudget + EPSILON;
    }

    /**
     * Завантажує товар, якщо він вміщується.
     *
     * @return true, якщо товар завантажено
     */
    public boolean load(Coffee coffee) {
        if (!canLoad(coffee)) {
            return false;
        }
        cargo.add(coffee);
        loadedVolume += coffee.getVolumeWithPackaging();
        loadedCost += coffee.getPrice();
        return true;
    }

    public List<Coffee> getCargo() {
        return Collections.unmodifiableList(cargo);
    }

    public double getMaxVolume() {
        return maxVolume;
    }

    public double getMaxBudget() {
        return maxBudget;
    }

    public double getLoadedVolume() {
        return loadedVolume;
    }

    public double getLoadedCost() {
        return loadedCost;
    }

    public double getTotalWeight() {
        return cargo.stream().mapToDouble(Coffee::getWeightWithPackaging).sum();
    }
}
