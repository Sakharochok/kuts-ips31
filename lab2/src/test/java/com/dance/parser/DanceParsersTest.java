package com.dance.parser;

import com.dance.model.Dance;
import com.dance.model.DanceComparators;
import com.dance.parser.dom.DomDanceParser;
import com.dance.parser.sax.SaxDanceParser;
import com.dance.parser.stax.StaxDanceParser;
import com.dance.service.XmlValidator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DanceParsersTest {
    private final File xmlFile = new File("src/main/resources/dances.xml");
    private final File xsdFile = new File("src/main/resources/dance.xsd");

    @Test
    void testXsdValidation() {
        assertTrue(XmlValidator.validate(xmlFile, xsdFile));
    }

    @Test
    void testAllParsersProduceIdenticalLists() {
        List<Dance> dom = new DomDanceParser().parse(xmlFile);
        List<Dance> sax = new SaxDanceParser().parse(xmlFile);
        List<Dance> stax = new StaxDanceParser().parse(xmlFile);

        assertEquals(5, dom.size());
        assertEquals(dom, sax);
        assertEquals(dom, stax);
    }

    @Test
    void testComparatorSorting() {
        List<Dance> dances = new DomDanceParser().parse(xmlFile);
        dances.sort(DanceComparators.BY_PROGRAM_NUMBER);

        assertEquals(1, dances.get(0).getNumber());
        assertEquals(2, dances.get(1).getNumber());
        assertEquals(7, dances.get(dances.size() - 1).getNumber());
    }
}
