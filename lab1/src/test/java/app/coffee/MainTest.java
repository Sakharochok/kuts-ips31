package app.coffee;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    @BeforeEach
    void redirectOutput() {
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    @Test
    void runsWithBundledData() {
        Main.main(new String[0]);

        String output = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("--- Товари у фургоні ---"), output);
        assertTrue(output.contains("Сортування за співвідношенням ціна/вага"), output);
        assertTrue(output.contains("Colombia Supremo"), output);
    }
}
