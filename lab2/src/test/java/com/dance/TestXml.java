package com.dance;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Допоміжні файли та фрагменти XML для тестів.
 */
public final class TestXml {
    private TestXml() {}

    public static final File XML = new File("src/main/resources/dances.xml");
    public static final File XSD = new File("src/main/resources/dance.xsd");
    public static final File XSL = new File("src/main/resources/transform.xsl");

    public static String document(String... dances) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<Dances xmlns=\"http://example.com/dance\">\n"
                + String.join("\n", dances)
                + "\n</Dances>";
    }

    public static String dance(String attrs, String dancerName, int age, int number) {
        return "<Dance " + attrs + ">"
                + "<scene>theatre</scene>"
                + "<numberOfDancers>solo</numberOfDancers>"
                + "<music>phonogram</music>"
                + "<dancers><dancer>"
                + "<name>" + dancerName + "</name>"
                + "<age>" + age + "</age>"
                + "<experienceYears>3</experienceYears>"
                + "</dancer></dancers>"
                + "<number>" + number + "</number>"
                + "</Dance>";
    }

    public static File write(Path dir, String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file.toFile();
    }
}
