package com.dance.service;

import com.dance.TestXml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XmlValidatorTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsValidDocument() {
        assertTrue(XmlValidator.validate(TestXml.XML, TestXml.XSD));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "id=\"a\" type=\"breakdance\"",   // тип не з переліку
            "type=\"folk\"",                  // відсутній обов'язковий id
            "id=\"1a\" type=\"folk\""         // id не відповідає типу xs:ID
    })
    void rejectsInvalidAttributes(String attrs) throws IOException {
        File xml = TestXml.write(tempDir, "bad.xml",
                TestXml.document(TestXml.dance(attrs, "Solo", 20, 1)));

        assertFalse(XmlValidator.validate(xml, TestXml.XSD));
    }

    @Test
    void rejectsValuesOutOfRange() throws IOException {
        File badAge = TestXml.write(tempDir, "age.xml",
                TestXml.document(TestXml.dance("id=\"a\" type=\"folk\"", "Solo", 150, 1)));
        File badNumber = TestXml.write(tempDir, "number.xml",
                TestXml.document(TestXml.dance("id=\"a\" type=\"folk\"", "Solo", 20, 0)));

        assertFalse(XmlValidator.validate(badAge, TestXml.XSD));
        assertFalse(XmlValidator.validate(badNumber, TestXml.XSD));
    }

    @Test
    void rejectsDuplicateIds() throws IOException {
        File xml = TestXml.write(tempDir, "dup.xml", TestXml.document(
                TestXml.dance("id=\"a\" type=\"folk\"", "One", 20, 1),
                TestXml.dance("id=\"a\" type=\"pop\"", "Two", 20, 2)));

        assertFalse(XmlValidator.validate(xml, TestXml.XSD));
    }

    @Test
    void returnsFalseForMissingFile() {
        assertFalse(XmlValidator.validate(tempDir.resolve("missing.xml").toFile(), TestXml.XSD));
    }
}
