package com.dance.parser.dom;

import com.dance.model.Dance;
import com.dance.parser.DanceBuilder;
import com.dance.parser.DanceParseException;
import com.dance.parser.DanceParser;
import com.dance.parser.DanceXml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DomDanceParser implements DanceParser {
    private static final Logger LOGGER = Logger.getLogger(DomDanceParser.class.getName());

    @Override
    public List<Dance> parse(File xmlFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().parse(xmlFile);

            DanceBuilder builder = new DanceBuilder();
            walk(doc.getDocumentElement(), builder);

            List<Dance> dances = builder.getDances();
            LOGGER.info(() -> "DOM parser loaded " + dances.size() + " items.");
            return dances;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "DOM parse error: " + e.getMessage(), e);
            throw new DanceParseException("DOM parse error: " + xmlFile, e);
        }
    }

    /**
     * Рекурсивно обходить дерево та передає його вузли в {@link DanceBuilder}
     * у тому ж порядку, у якому їх бачать потокові парсери.
     */
    private void walk(Element element, DanceBuilder builder) {
        String tag = element.getLocalName();
        if (DanceXml.DANCE.equals(tag)) {
            builder.startDance(
                    element.getAttribute(DanceXml.ATTR_ID),
                    element.getAttribute(DanceXml.ATTR_TYPE),
                    element.hasAttribute(DanceXml.ATTR_TITLE) ? element.getAttribute(DanceXml.ATTR_TITLE) : null);
        } else if (DanceXml.DANCER.equals(tag)) {
            builder.startDancer();
        }

        boolean hasChildElements = false;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                hasChildElements = true;
                walk((Element) child, builder);
            }
        }
        if (!hasChildElements) {
            builder.setField(tag, element.getTextContent().trim());
        }

        if (DanceXml.DANCER.equals(tag)) {
            builder.endDancer();
        } else if (DanceXml.DANCE.equals(tag)) {
            builder.endDance();
        }
    }
}
