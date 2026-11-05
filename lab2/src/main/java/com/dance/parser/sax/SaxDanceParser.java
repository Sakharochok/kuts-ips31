package com.dance.parser.sax;

import com.dance.model.Dance;
import com.dance.model.Dancer;
import com.dance.parser.DanceParser;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
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
            SAXParser saxParser = factory.newSAXParser();

            DanceHandler handler = new DanceHandler();
            saxParser.parse(xmlFile, handler);
            LOGGER.info(() -> "SAX parser loaded " + handler.getDances().size() + " items.");
            return handler.getDances();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "SAX parse error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static class DanceHandler extends DefaultHandler {
        private final List<Dance> dances = new ArrayList<>();
        private Dance currentDance;
        private Dancer currentDancer;
        private final StringBuilder content = new StringBuilder();

        public List<Dance> getDances() { return dances; }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            content.setLength(0);
            if ("Dance".equals(localName)) {
                currentDance = new Dance();
                currentDance.setId(attributes.getValue("id"));
                currentDance.setType(attributes.getValue("type"));
                currentDance.setTitle(attributes.getValue("title"));
            } else if ("dancer".equals(localName)) {
                currentDancer = new Dancer();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            content.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String text = content.toString().trim();
            if (currentDance != null) {
                switch (localName) {
                    case "scene": currentDance.setScene(text); break;
                    case "numberOfDancers": currentDance.setNumberOfDancers(text); break;
                    case "music": currentDance.setMusic(text); break;
                    case "number": currentDance.setNumber(Integer.parseInt(text)); break;
                    case "name":
                        if (currentDancer != null) currentDancer.setName(text);
                        break;
                    case "age":
                        if (currentDancer != null) currentDancer.setAge(Integer.parseInt(text));
                        break;
                    case "experienceYears":
                        if (currentDancer != null) currentDancer.setExperienceYears(Integer.parseInt(text));
                        break;
                    case "dancer":
                        if (currentDancer != null) {
                            currentDance.getDancers().add(currentDancer);
                            currentDancer = null;
                        }
                        break;
                    case "Dance":
                        dances.add(currentDance);
                        currentDance = null;
                        break;
                }
            }
        }
    }
}
