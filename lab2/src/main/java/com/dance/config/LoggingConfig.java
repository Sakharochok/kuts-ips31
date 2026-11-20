package com.dance.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Завантажує налаштування java.util.logging з файлу logging.properties у classpath.
 */
public final class LoggingConfig {
    private static final String CONFIG_FILE = "/logging.properties";

    private LoggingConfig() {}

    /**
     * @return true, якщо конфігурацію знайдено та застосовано
     */
    public static boolean init() {
        return init(CONFIG_FILE);
    }

    static boolean init(String resource) {
        try (InputStream in = LoggingConfig.class.getResourceAsStream(resource)) {
            if (in == null) {
                Logger.getLogger(LoggingConfig.class.getName())
                        .warning(() -> "Logging config not found: " + resource + ", using defaults.");
                return false;
            }
            LogManager.getLogManager().readConfiguration(in);
            return true;
        } catch (IOException e) {
            Logger.getLogger(LoggingConfig.class.getName())
                    .warning(() -> "Cannot read logging config: " + e.getMessage());
            return false;
        }
    }
}
