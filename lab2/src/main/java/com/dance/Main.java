package com.dance;

import com.dance.model.Dance;
import com.dance.model.DanceComparators;
import com.dance.parser.dom.DomDanceParser;
import com.dance.parser.sax.SaxDanceParser;
import com.dance.parser.stax.StaxDanceParser;
import com.dance.service.XmlTransformer;
import com.dance.service.XmlValidator;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        File xmlFile = new File("src/main/resources/dances.xml");
        File xsdFile = new File("src/main/resources/dance.xsd");
        File xslFile = new File("src/main/resources/transform.xsl");
        File transformedXml = new File("target/dances_transformed.xml");

        // 1. Валідація XSD
        boolean valid = XmlValidator.validate(xmlFile, xsdFile);
        LOGGER.info(() -> "XSD Validation passed: " + valid);

        // 2. Розбір усіма трьома парсерами
        List<Dance> domList = new DomDanceParser().parse(xmlFile);
        List<Dance> saxList = new SaxDanceParser().parse(xmlFile);
        List<Dance> staxList = new StaxDanceParser().parse(xmlFile);

        // 3. Порівняння та логування результатів роботи парсерів
        boolean allEqual = domList.equals(saxList) && domList.equals(staxList);
        LOGGER.info(() -> "All parsers produced identical results: " + allEqual);

        // 4. Демонстрація сортування через Comparator
        domList.sort(DanceComparators.BY_PROGRAM_NUMBER);
        System.out.println("\n=== Sorted by Program Number (DOM) ===");
        domList.forEach(System.out::println);

        saxList.sort(DanceComparators.BY_TYPE);
        System.out.println("\n=== Sorted by Dance Type (SAX) ===");
        saxList.forEach(System.out::println);

        System.out.println("\n=== StAX Parsed Count: " + staxList.size() + " items ===");

        // 5. XSL-трансформація
        transformedXml.getParentFile().mkdirs();
        XmlTransformer.transform(xmlFile, xslFile, transformedXml);
    }
}
