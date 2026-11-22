package app.coffee;

import app.coffee.service.DataLoader;
import app.coffee.service.VanService;

/** Точка входу консольного застосунку «Фургон кави». */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        new CoffeeVanApp(new DataLoader(), new VanService(), System.out).run();
    }
}
