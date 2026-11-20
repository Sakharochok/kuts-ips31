package com.dance.parser;

import com.dance.TestXml;
import com.dance.model.Dance;
import com.dance.model.Dancer;
import com.dance.parser.dom.DomDanceParser;
import com.dance.parser.sax.SaxDanceParser;
import com.dance.parser.stax.StaxDanceParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DanceParsersTest {
    @TempDir
    Path tempDir;

    static Stream<DanceParser> parsers() {
        return Stream.of(new DomDanceParser(), new SaxDanceParser(), new StaxDanceParser());
    }

    @ParameterizedTest
    @MethodSource("parsers")
    void parsesAllDances(DanceParser parser) {
        List<Dance> dances = parser.parse(TestXml.XML);

        assertEquals(5, dances.size());
        assertEquals(List.of("d1", "d2", "d3", "d4", "d5"),
                dances.stream().map(Dance::getId).toList());
    }

    @ParameterizedTest
    @MethodSource("parsers")
    void parsesAllFieldsOfDance(DanceParser parser) {
        Dance first = parser.parse(TestXml.XML).get(0);

        assertEquals("d1", first.getId());
        assertEquals("ballroom", first.getType());
        assertEquals("Viennese Waltz", first.getTitle());
        assertEquals("theatre", first.getScene());
        assertEquals("duet", first.getNumberOfDancers());
        assertEquals("live music", first.getMusic());
        assertEquals(3, first.getNumber());
        assertEquals(List.of(
                new Dancer("Oleksandr Melnyk", 22, 12),
                new Dancer("Olena Shevchenko", 20, 10)), first.getDancers());
    }

    @ParameterizedTest
    @MethodSource("parsers")
    void keepsTextWithEntitiesWhole(DanceParser parser) throws IOException {
        File xml = TestXml.write(tempDir, "entity.xml",
                TestXml.document(TestXml.dance("id=\"a\" type=\"folk\"", "Kalyna &amp; Friends", 20, 1)));

        Dance dance = parser.parse(xml).get(0);

        assertEquals("Kalyna & Friends", dance.getDancers().get(0).getName());
    }

    @ParameterizedTest
    @MethodSource("parsers")
    void titleIsNullWhenAbsent(DanceParser parser) throws IOException {
        File xml = TestXml.write(tempDir, "notitle.xml",
                TestXml.document(TestXml.dance("id=\"a\" type=\"folk\"", "Solo", 20, 1)));

        assertNull(parser.parse(xml).get(0).getTitle());
    }

    @ParameterizedTest
    @MethodSource("parsers")
    void throwsOnMissingFile(DanceParser parser) {
        File missing = tempDir.resolve("missing.xml").toFile();

        assertThrows(DanceParseException.class, () -> parser.parse(missing));
    }

    @ParameterizedTest
    @MethodSource("parsers")
    void throwsOnMalformedXml(DanceParser parser) throws IOException {
        File broken = TestXml.write(tempDir, "broken.xml", "<Dances><Dance></Dances>");

        assertThrows(DanceParseException.class, () -> parser.parse(broken));
    }

    @ParameterizedTest
    @MethodSource("parsers")
    void throwsOnNonNumericNumber(DanceParser parser) throws IOException {
        File xml = TestXml.write(tempDir, "nan.xml",
                TestXml.document(TestXml.dance("id=\"a\" type=\"folk\"", "Solo", 20, 1)
                        .replace("<number>1</number>", "<number>first</number>")));

        assertThrows(DanceParseException.class, () -> parser.parse(xml));
    }

    @Test
    void allParsersProduceIdenticalLists() {
        List<Dance> dom = new DomDanceParser().parse(TestXml.XML);
        List<Dance> sax = new SaxDanceParser().parse(TestXml.XML);
        List<Dance> stax = new StaxDanceParser().parse(TestXml.XML);

        assertEquals(dom, sax);
        assertEquals(dom, stax);
    }
}
