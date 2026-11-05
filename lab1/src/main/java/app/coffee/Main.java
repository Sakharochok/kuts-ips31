package app.coffee;

import app.coffee.model.Coffee;
import app.coffee.service.DataLoader;
import app.coffee.service.VanService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Ініціалізація фургона...");
        
        DataLoader loader = new DataLoader();
        List<Coffee> stock = loader.loadInitializationData();
        
        VanService vanService = new VanService();
        
        double vanMaxVolume = 5.0; 
        double vanMaxBudget = 3000.0;
        
        List<Coffee> vanCargo = vanService.loadVan(stock, vanMaxVolume, vanMaxBudget);
        
        System.out.println("\n--- Товари у фургоні ---");
        vanCargo.forEach(System.out::println);
        
        System.out.println("\n--- Сортування за співвідношенням ціна/вага ---");
        vanService.sortByPriceToWeightRatio(vanCargo);
        vanCargo.forEach(System.out::println);
        
        System.out.println("\n--- Пошук кави преміум якості (90-100 балів) ---");
        List<Coffee> premiumCoffee = vanService.findCoffeeByQuality(vanCargo, 90, 100);
        premiumCoffee.forEach(System.out::println);
    }
}