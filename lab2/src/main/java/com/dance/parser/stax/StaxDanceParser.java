package com.dance.parser.stax;

import com.dance.model.Dance;
import com.dance.parser.DanceBuilder;
import com.dance.parser.DanceParseException;
import com.dance.parser.DanceParser;
import com.dance.parser.DanceXml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaxDanceParser implements DanceParser {
    private static final Logger LOGGER = Logger.getLogger(StaxDanceParser.class.getName());

    @Override
    public List<Dance> parse(File xmlFile) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XMLStreamReader reader = factory.createXMLStreamReader(fis);
            DanceBuilder builder = new DanceBuilder();
            // Текст елемента може надходити кількома подіями CHARACTERS (наприклад, через &amp;),
            // тому накопичуємо його і застосовуємо лише в END_ELEMENT.
            StringBuilder content = new StringBuilder();

            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        content.setLength(0);
                        String tag = reader.getLocalName();
                        if (DanceXml.DANCE.equals(tag)) {
                            builder.startDance(
                                    reader.getAttributeValue(null, DanceXml.ATTR_ID),
                                    reader.getAttributeValue(null, DanceXml.ATTR_TYPE),
                                    reader.getAttributeValue(null, DanceXml.ATTR_TITLE));
                        } else if (DanceXml.DANCER.equals(tag)) {
                            builder.startDancer();
                        }
                    }
                    case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> content.append(reader.getText());
                    case XMLStreamConstants.END_ELEMENT -> {
                        String tag = reader.getLocalName();
                        if (DanceXml.DANCER.equals(tag)) {
                            builder.endDancer();
                        } else if (DanceXml.DANCE.equals(tag)) {
                            builder.endDance();
                        } else {
                            builder.setField(tag, content.toString().trim());
                        }
                        content.setLength(0);
                    }
                    default -> { }
                }
            }
            reader.close();

            List<Dance> dances = builder.getDances();
            LOGGER.info(() -> "StAX parser loaded " + dances.size() + " items.");
            return dances;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "StAX parse error: " + e.getMessage(), e);
            throw new DanceParseException("StAX parse error: " + xmlFile, e);
        }
    }
}
