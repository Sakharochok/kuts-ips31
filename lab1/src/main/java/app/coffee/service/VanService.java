package app.coffee.service;

import app.coffee.model.Coffee;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VanService {

    public List<Coffee> loadVan(List<Coffee> availableStock, double maxVolume, double maxBudget) {
        List<Coffee> loadedVan = new ArrayList<>();
        double currentVolume = 0;
        double currentBudget = 0;

        for (Coffee coffee : availableStock) {
            if (currentVolume + coffee.getVolume() <= maxVolume && currentBudget + coffee.getPrice() <= maxBudget) {
                loadedVan.add(coffee);
                currentVolume += coffee.getVolume();
                currentBudget += coffee.getPrice();
            }
        }
        return loadedVan;
    }

    public void sortByPriceToWeightRatio(List<Coffee> coffeeList) {
        coffeeList.sort(Comparator.comparingDouble(Coffee::getPriceToWeightRatio));
    }

    public List<Coffee> findCoffeeByQuality(List<Coffee> coffeeList, int minQuality, int maxQuality) {
        List<Coffee> result = new ArrayList<>();
        for (Coffee coffee : coffeeList) {
            if (coffee.getQualityScore() >= minQuality && coffee.getQualityScore() <= maxQuality) {
                result.add(coffee);
            }
        }
        return result;
    }
}