package com.dance.service;

import com.dance.TestXml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XmlTransformerTest {
    @TempDir
    Path tempDir;

    private static Document read(File file) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
    }

    private static List<String> ids(Element group) {
        List<String> ids = new ArrayList<>();
        NodeList dances = group.getElementsByTagName("Dance");
        for (int i = 0; i < dances.getLength(); i++) {
            ids.add(((Element) dances.item(i)).getAttribute("id"));
        }
        return ids;
    }

    @Test
    void groupsDancesByType() throws Exception {
        File out = tempDir.resolve("out.xml").toFile();

        XmlTransformer.transform(TestXml.XML, TestXml.XSL, out);

        Element root = read(out).getDocumentElement();
        assertEquals("root", root.getTagName());
        assertEquals(List.of("d1", "d3"), ids((Element) root.getElementsByTagName("ballroom").item(0)));
        assertEquals(List.of("d2", "d5"), ids((Element) root.getElementsByTagName("folk").item(0)));
        assertEquals(List.of("d4"), ids((Element) root.getElementsByTagName("oriental").item(0)));
        assertEquals(3, root.getChildNodes().getLength() - countWhitespace(root));
    }

    @Test
    void keepsDanceContent() throws Exception {
        File out = tempDir.resolve("out.xml").toFile();

        XmlTransformer.transform(TestXml.XML, TestXml.XSL, out);

        Element waltz = (Element) read(out).getElementsByTagName("Dance").item(0);
        assertEquals("Viennese Waltz", waltz.getAttribute("title"));
        assertEquals("theatre", waltz.getElementsByTagName("scene").item(0).getTextContent());
        assertEquals(2, waltz.getElementsByTagName("dancer").getLength());
        assertEquals("3", waltz.getElementsByTagName("number").item(0).getTextContent());
    }

    @Test
    void omitsTitleWhenAbsent() throws Exception {
        File xml = TestXml.write(tempDir, "in.xml",
                TestXml.document(TestXml.dance("id=\"a\" type=\"folk\"", "Solo", 20, 1)));
        File out = tempDir.resolve("out.xml").toFile();

        XmlTransformer.transform(xml, TestXml.XSL, out);

        Element dance = (Element) read(out).getElementsByTagName("Dance").item(0);
        assertFalse(dance.hasAttribute("title"));
    }

    @Test
    void throwsOnBrokenStylesheet() throws IOException {
        File xsl = TestXml.write(tempDir, "bad.xsl", "<not-a-stylesheet");
        File out = tempDir.resolve("out.xml").toFile();

        assertThrows(RuntimeException.class, () -> XmlTransformer.transform(TestXml.XML, xsl, out));
    }

    private static int countWhitespace(Element element) {
        int count = 0;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) count++;
        }
        return count;
    }
}
