package com.dance.config;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class LoggingConfigTest {

    @Test
    void loadsConfigFromClasspath() {
        assertTrue(LoggingConfig.init());
        assertEquals(Level.INFO, Logger.getLogger("com.dance").getLevel());
    }

    @Test
    void returnsFalseWhenConfigMissing() {
        assertFalse(LoggingConfig.init("/no-such-logging.properties"));
    }
}
