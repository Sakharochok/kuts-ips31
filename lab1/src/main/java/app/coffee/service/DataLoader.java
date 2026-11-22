package app.coffee.service;

import app.coffee.model.Coffee;
import app.coffee.model.CoffeeBeans;
import app.coffee.model.GrindSize;
import app.coffee.model.GroundCoffee;
import app.coffee.model.InstantCoffee;
import app.coffee.model.InstantPackaging;
import app.coffee.model.RoastLevel;
import app.coffee.model.Van;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

/**
 * Завантажує початкові дані з файлів у classpath:
 * асортимент складу ({@value #STOCK_RESOURCE}) і параметри фургона ({@value #VAN_RESOURCE}).
 */
public class DataLoader {

    static final String STOCK_RESOURCE = "/coffee.csv";
    static final String VAN_RESOURCE = "/van.properties";

    private static final String SEPARATOR = ";";
    private static final String COMMENT = "#";
    private static final int FIELD_COUNT = 7;

    public List<Coffee> loadStock() {
        return loadStock(STOCK_RESOURCE);
    }

    public Van loadVan() {
        return loadVan(VAN_RESOURCE);
    }

    List<Coffee> loadStock(String resource) {
        return readResource(resource, this::parseStock);
    }

    Van loadVan(String resource) {
        return readResource(resource, this::parseVan);
    }

    /**
     * Розбирає асортимент у форматі {@code тип;назва;ціна;вага;об'єм;якість;варіант}.
     * Порожні рядки та рядки, що починаються з {@value #COMMENT}, пропускаються.
     */
    public List<Coffee> parseStock(Reader reader) {
        List<Coffee> stock = new ArrayList<>();
        BufferedReader lines = new BufferedReader(reader);
        try {
            String line;
            int lineNumber = 0;
            while ((line = lines.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith(COMMENT)) {
                    stock.add(parseLine(trimmed, lineNumber));
                }
            }
        } catch (IOException e) {
            throw new DataLoadException("Cannot read stock data", e);
        }
        return stock;
    }

    /** Розбирає параметри фургона: {@code van.maxVolume} і {@code van.maxBudget}. */
    public Van parseVan(Reader reader) {
        Properties properties = new Properties();
        try {
            properties.load(reader);
        } catch (IOException e) {
            throw new DataLoadException("Cannot read van parameters", e);
        }
        double maxVolume = parseRequiredDouble(properties, "van.maxVolume");
        double maxBudget = parseRequiredDouble(properties, "van.maxBudget");
        try {
            return new Van(maxVolume, maxBudget);
        } catch (IllegalArgumentException e) {
            throw new DataLoadException("Invalid van parameters: " + e.getMessage(), e);
        }
    }

    Coffee parseLine(String line, int lineNumber) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            throw new DataLoadException("Line " + lineNumber + ": expected " + FIELD_COUNT
                    + " fields separated by '" + SEPARATOR + "', got " + fields.length);
        }
        try {
            String type = fields[0].trim();
            String name = fields[1].trim();
            double price = Double.parseDouble(fields[2].trim());
            double weight = Double.parseDouble(fields[3].trim());
            double volume = Double.parseDouble(fields[4].trim());
            int quality = Integer.parseInt(fields[5].trim());
            String variant = fields[6].trim();

            return switch (type) {
                case "BEANS" -> new CoffeeBeans(name, price, weight, volume, quality, RoastLevel.valueOf(variant));
                case "GROUND" -> new GroundCoffee(name, price, weight, volume, quality, GrindSize.valueOf(variant));
                case "INSTANT" ->
                        new InstantCoffee(name, price, weight, volume, quality, InstantPackaging.valueOf(variant));
                default -> throw new IllegalArgumentException("unknown coffee type '" + type + "'");
            };
        } catch (IllegalArgumentException e) {
            // NumberFormatException теж є IllegalArgumentException.
            throw new DataLoadException("Line " + lineNumber + ": " + e.getMessage(), e);
        }
    }

    private static double parseRequiredDouble(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new DataLoadException("Missing van parameter: " + key);
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new DataLoadException("Van parameter " + key + " is not a number: " + value, e);
        }
    }

    private static <T> T readResource(String resource, Function<Reader, T> parser) {
        InputStream in = DataLoader.class.getResourceAsStream(resource);
        if (in == null) {
            throw new DataLoadException("Resource not found: " + resource);
        }
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return parser.apply(reader);
        } catch (IOException | UncheckedIOException e) {
            throw new DataLoadException("Cannot read " + resource, e);
        }
    }
}
