package com.dance.parser.dom;

import com.dance.model.Dance;
import com.dance.model.Dancer;
import com.dance.parser.DanceParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DomDanceParser implements DanceParser {
    private static final Logger LOGGER = Logger.getLogger(DomDanceParser.class.getName());

    @Override
    public List<Dance> parse(File xmlFile) {
        List<Dance> dances = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList danceNodes = doc.getElementsByTagNameNS("http://example.com/dance", "Dance");
            for (int i = 0; i < danceNodes.getLength(); i++) {
                Element danceElem = (Element) danceNodes.item(i);
                Dance dance = new Dance();
                dance.setId(danceElem.getAttribute("id"));
                dance.setType(danceElem.getAttribute("type"));
                if (danceElem.hasAttribute("title")) {
                    dance.setTitle(danceElem.getAttribute("title"));
                }
                dance.setScene(getText(danceElem, "scene"));
                dance.setNumberOfDancers(getText(danceElem, "numberOfDancers"));
                dance.setMusic(getText(danceElem, "music"));
                dance.setNumber(Integer.parseInt(getText(danceElem, "number")));

                NodeList dancerNodes = danceElem.getElementsByTagNameNS("http://example.com/dance", "dancer");
                for (int j = 0; j < dancerNodes.getLength(); j++) {
                    Element dElem = (Element) dancerNodes.item(j);
                    Dancer dancer = new Dancer(
                            getText(dElem, "name"),
                            Integer.parseInt(getText(dElem, "age")),
                            Integer.parseInt(getText(dElem, "experienceYears"))
                    );
                    dance.getDancers().add(dancer);
                }
                dances.add(dance);
            }
            LOGGER.info(() -> "DOM parser loaded " + dances.size() + " items.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "DOM parse error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return dances;
    }

    private String getText(Element parent, String localName) {
        NodeList nl = parent.getElementsByTagNameNS("http://example.com/dance", localName);
        return (nl.getLength() > 0) ? nl.item(0).getTextContent().trim() : "";
    }
}
