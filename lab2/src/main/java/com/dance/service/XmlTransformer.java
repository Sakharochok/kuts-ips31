package com.dance.service;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlTransformer {
    private static final Logger LOGGER = Logger.getLogger(XmlTransformer.class.getName());

    public static void transform(File xmlFile, File xslFile, File outputFile) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xslFile));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new StreamSource(xmlFile), new StreamResult(outputFile));
            LOGGER.info(() -> "Transformed XML saved to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "XSL transformation error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
