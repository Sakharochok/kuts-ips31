package com.dance.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private static Dance sampleDance() {
        Dance dance = new Dance();
        dance.setId("d1");
        dance.setType("ballroom");
        dance.setTitle("Waltz");
        dance.setScene("theatre");
        dance.setNumberOfDancers("duet");
        dance.setMusic("live music");
        dance.setNumber(3);
        dance.setDancers(new ArrayList<>(List.of(new Dancer("Ann", 20, 10), new Dancer("Bob", 21, 11))));
        return dance;
    }

    @Test
    void dancerGettersAndSetters() {
        Dancer dancer = new Dancer();
        dancer.setName("Ann");
        dancer.setAge(20);
        dancer.setExperienceYears(10);

        assertEquals("Ann", dancer.getName());
        assertEquals(20, dancer.getAge());
        assertEquals(10, dancer.getExperienceYears());
        assertEquals(new Dancer("Ann", 20, 10), dancer);
    }

    @Test
    void dancerEqualsAndHashCode() {
        Dancer dancer = new Dancer("Ann", 20, 10);

        assertEquals(dancer, dancer);
        assertEquals(new Dancer("Ann", 20, 10).hashCode(), dancer.hashCode());
        assertNotEquals(new Dancer("Ann", 21, 10), dancer);
        assertNotEquals(new Dancer("Ann", 20, 11), dancer);
        assertNotEquals(new Dancer("Bob", 20, 10), dancer);
        assertNotEquals(null, dancer);
        assertNotEquals("Ann", dancer);
    }

    @Test
    void dancerToString() {
        assertEquals("Dancer{Ann, age=20, exp=10}", new Dancer("Ann", 20, 10).toString());
    }

    @Test
    void danceEqualsAndHashCode() {
        Dance dance = sampleDance();

        assertEquals(dance, dance);
        assertEquals(sampleDance(), dance);
        assertEquals(sampleDance().hashCode(), dance.hashCode());
        assertNotEquals(null, dance);
        assertNotEquals("d1", dance);
    }

    @Test
    void danceNotEqualWhenAnyFieldDiffers() {
        Dance dance = sampleDance();
        List<java.util.function.Consumer<Dance>> changes = List.of(
                d -> d.setId("x"),
                d -> d.setType("folk"),
                d -> d.setTitle("Tango"),
                d -> d.setScene("tv studio"),
                d -> d.setNumberOfDancers("solo"),
                d -> d.setMusic("phonogram"),
                d -> d.setNumber(4),
                d -> d.setDancers(new ArrayList<>()));

        for (var change : changes) {
            Dance other = sampleDance();
            change.accept(other);
            assertNotEquals(dance, other);
        }
    }

    @Test
    void danceToString() {
        assertEquals("#3 [ballroom] Waltz (id=d1, scene=theatre, dancers=2)", sampleDance().toString());

        Dance untitled = sampleDance();
        untitled.setTitle(null);
        assertEquals("#3 [ballroom]  (id=d1, scene=theatre, dancers=2)", untitled.toString());
    }
}
