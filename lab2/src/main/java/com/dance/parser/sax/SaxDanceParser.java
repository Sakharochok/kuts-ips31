package com.dance.parser.sax;

import com.dance.model.Dance;
import com.dance.parser.DanceBuilder;
import com.dance.parser.DanceParseException;
import com.dance.parser.DanceParser;
import com.dance.parser.DanceXml;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SaxDanceParser implements DanceParser {
    private static final Logger LOGGER = Logger.getLogger(SaxDanceParser.class.getName());

    @Override
    public List<Dance> parse(File xmlFile) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            DanceHandler handler = new DanceHandler();
            factory.newSAXParser().parse(xmlFile, handler);

            List<Dance> dances = handler.builder.getDances();
            LOGGER.info(() -> "SAX parser loaded " + dances.size() + " items.");
            return dances;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "SAX parse error: " + e.getMessage(), e);
            throw new DanceParseException("SAX parse error: " + xmlFile, e);
        }
    }

    private static class DanceHandler extends DefaultHandler {
        private final DanceBuilder builder = new DanceBuilder();
        private final StringBuilder content = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            content.setLength(0);
            if (DanceXml.DANCE.equals(localName)) {
                builder.startDance(
                        attributes.getValue(DanceXml.ATTR_ID),
                        attributes.getValue(DanceXml.ATTR_TYPE),
                        attributes.getValue(DanceXml.ATTR_TITLE));
            } else if (DanceXml.DANCER.equals(localName)) {
                builder.startDancer();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            content.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (DanceXml.DANCER.equals(localName)) {
                builder.endDancer();
            } else if (DanceXml.DANCE.equals(localName)) {
                builder.endDance();
            } else {
                builder.setField(localName, content.toString().trim());
            }
            content.setLength(0);
        }
    }
}
