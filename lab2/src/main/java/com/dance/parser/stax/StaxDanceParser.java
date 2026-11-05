package com.dance.parser.stax;

import com.dance.model.Dance;
import com.dance.model.Dancer;
import com.dance.parser.DanceParser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaxDanceParser implements DanceParser {
    private static final Logger LOGGER = Logger.getLogger(StaxDanceParser.class.getName());

    @Override
    public List<Dance> parse(File xmlFile) {
        List<Dance> dances = new ArrayList<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XMLStreamReader reader = factory.createXMLStreamReader(fis);
            Dance currentDance = null;
            Dancer currentDancer = null;
            String currentTag = "";

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        currentTag = reader.getLocalName();
                        if ("Dance".equals(currentTag)) {
                            currentDance = new Dance();
                            currentDance.setId(reader.getAttributeValue(null, "id"));
                            currentDance.setType(reader.getAttributeValue(null, "type"));
                            currentDance.setTitle(reader.getAttributeValue(null, "title"));
                        } else if ("dancer".equals(currentTag)) {
                            currentDancer = new Dancer();
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        String text = reader.getText().trim();
                        if (text.isEmpty() || currentDance == null) break;

                        switch (currentTag) {
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
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        String endTag = reader.getLocalName();
                        if ("dancer".equals(endTag) && currentDance != null && currentDancer != null) {
                            currentDance.getDancers().add(currentDancer);
                            currentDancer = null;
                        } else if ("Dance".equals(endTag) && currentDance != null) {
                            dances.add(currentDance);
                            currentDance = null;
                        }
                        currentTag = "";
                        break;
                }
            }
            LOGGER.info(() -> "StAX parser loaded " + dances.size() + " items.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "StAX parse error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return dances;
    }
}
