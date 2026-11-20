package com.dance.parser;

import com.dance.model.Dance;
import com.dance.model.Dancer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DanceBuilderTest {

    @Test
    void buildsDanceWithDancers() {
        DanceBuilder builder = new DanceBuilder();
        builder.startDance("d1", "folk", "Hopak");
        builder.setField(DanceXml.SCENE, "street stage");
        builder.setField(DanceXml.NUMBER_OF_DANCERS, "mass");
        builder.setField(DanceXml.MUSIC, "live music");
        builder.startDancer();
        builder.setField(DanceXml.NAME, "Kalyna");
        builder.setField(DanceXml.AGE, "25");
        builder.setField(DanceXml.EXPERIENCE_YEARS, "8");
        builder.endDancer();
        builder.setField(DanceXml.NUMBER, "1");
        builder.endDance();

        List<Dance> dances = builder.getDances();
        assertEquals(1, dances.size());
        Dance dance = dances.get(0);
        assertEquals("d1", dance.getId());
        assertEquals("folk", dance.getType());
        assertEquals("Hopak", dance.getTitle());
        assertEquals("street stage", dance.getScene());
        assertEquals("mass", dance.getNumberOfDancers());
        assertEquals("live music", dance.getMusic());
        assertEquals(1, dance.getNumber());
        assertEquals(List.of(new Dancer("Kalyna", 25, 8)), dance.getDancers());
    }

    @Test
    void ignoresFieldsOutsideDance() {
        DanceBuilder builder = new DanceBuilder();
        builder.setField(DanceXml.SCENE, "theatre");
        builder.startDancer();
        builder.endDancer();
        builder.endDance();

        assertTrue(builder.getDances().isEmpty());
    }

    @Test
    void ignoresDancerFieldsWithoutDancer() {
        DanceBuilder builder = new DanceBuilder();
        builder.startDance("d1", "folk", null);
        builder.setField(DanceXml.NAME, "Nobody");
        builder.setField(DanceXml.AGE, "20");
        builder.setField(DanceXml.EXPERIENCE_YEARS, "2");
        builder.endDancer();
        builder.endDance();

        assertTrue(builder.getDances().get(0).getDancers().isEmpty());
    }

    @Test
    void ignoresUnknownAndContainerTags() {
        DanceBuilder builder = new DanceBuilder();
        builder.startDance("d1", "folk", null);
        builder.setField("dancers", "");
        builder.setField("unknown", "value");
        builder.endDance();

        Dance expected = new Dance();
        expected.setId("d1");
        expected.setType("folk");
        assertEquals(List.of(expected), builder.getDances());
    }
}
