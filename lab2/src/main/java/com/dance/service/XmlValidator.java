package com.dance.service;

import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlValidator {
    private static final Logger LOGGER = Logger.getLogger(XmlValidator.class.getName());

    public static boolean validate(File xmlFile, File xsdFile) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdFile);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlFile));
            LOGGER.info(() -> "XML is strictly valid according to XSD.");
            return true;
        } catch (SAXException | IOException e) {
            LOGGER.log(Level.SEVERE, "Validation error: " + e.getMessage(), e);
            return false;
        }
    }
}
