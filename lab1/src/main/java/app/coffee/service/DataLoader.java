package app.coffee.service;

import app.coffee.model.*;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    public List<Coffee> loadInitializationData() {
        List<Coffee> stock = new ArrayList<>();
        stock.add(new CoffeeBeans("Colombia Supremo", 1500, 1.05, 2.5, 95, "Medium"));
        stock.add(new GroundCoffee("Espresso Blend", 900, 0.52, 1.2, 88, "Fine (Fiorenzato)"));
        stock.add(new InstantCoffee("Nescafe Gold", 300, 0.25, 0.8, 60, "Скляна банка"));
        stock.add(new InstantCoffee("MacCoffee 3-in-1", 10, 0.02, 0.05, 40, "Пакетик"));
        stock.add(new CoffeeBeans("Ethiopia Yirgacheffe", 1800, 1.05, 2.5, 98, "Light"));
        return stock;
    }
}