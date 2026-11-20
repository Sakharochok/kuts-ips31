package com.dance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @TempDir
    Path tempDir;

    @Test
    void runProcessesValidDocument() {
        File out = tempDir.resolve("nested/out.xml").toFile();

        assertTrue(Main.run(TestXml.XML, TestXml.XSD, TestXml.XSL, out));
        assertTrue(out.exists());
    }

    @Test
    void runStopsOnInvalidDocument() throws IOException {
        File invalid = TestXml.write(tempDir, "invalid.xml",
                TestXml.document(TestXml.dance("id=\"a\" type=\"breakdance\"", "Solo", 20, 1)));
        File out = tempDir.resolve("out.xml").toFile();

        assertFalse(Main.run(invalid, TestXml.XSD, TestXml.XSL, out));
        assertFalse(out.exists());
    }

    @Test
    void mainWritesTransformedFile() {
        Main.main(new String[0]);

        assertTrue(new File("target/dances_transformed.xml").exists());
    }
}
