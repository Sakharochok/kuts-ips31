package com.dance;

import com.dance.config.LoggingConfig;
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
        LoggingConfig.init();
        run(new File("src/main/resources/dances.xml"),
                new File("src/main/resources/dance.xsd"),
                new File("src/main/resources/transform.xsl"),
                new File("target/dances_transformed.xml"));
    }

    /**
     * @return true, якщо XML валідний і всі три парсери дали однаковий результат
     */
    public static boolean run(File xmlFile, File xsdFile, File xslFile, File transformedXml) {
        // 1. Валідація XSD
        boolean valid = XmlValidator.validate(xmlFile, xsdFile);
        LOGGER.info(() -> "XSD Validation passed: " + valid);
        if (!valid) {
            LOGGER.severe("XML is not valid, stopping.");
            return false;
        }

        // 2. Розбір усіма трьома парсерами
        List<Dance> domList = new DomDanceParser().parse(xmlFile);
        List<Dance> saxList = new SaxDanceParser().parse(xmlFile);
        List<Dance> staxList = new StaxDanceParser().parse(xmlFile);

        // 3. Порівняння та логування результатів роботи парсерів
        boolean allEqual = domList.equals(saxList) && domList.equals(staxList);
        LOGGER.info(() -> "All parsers produced identical results: " + allEqual);

        // 4. Демонстрація сортування через Comparator
        domList.sort(DanceComparators.BY_PROGRAM_NUMBER);
        logList("Sorted by program number (DOM)", domList);

        saxList.sort(DanceComparators.BY_TYPE);
        logList("Sorted by dance type (SAX)", saxList);

        staxList.sort(DanceComparators.BY_ID);
        logList("Sorted by id (StAX)", staxList);

        // 5. XSL-трансформація
        File parent = transformedXml.getAbsoluteFile().getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        XmlTransformer.transform(xmlFile, xslFile, transformedXml);
        return allEqual;
    }

    private static void logList(String header, List<Dance> dances) {
        StringBuilder sb = new StringBuilder("=== ").append(header).append(" ===");
        dances.forEach(d -> sb.append(System.lineSeparator()).append("  ").append(d));
        LOGGER.info(sb::toString);
    }
}
