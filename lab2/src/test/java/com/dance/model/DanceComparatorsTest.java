package com.dance.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DanceComparatorsTest {

    private static Dance dance(String id, String type, int number) {
        Dance dance = new Dance();
        dance.setId(id);
        dance.setType(type);
        dance.setNumber(number);
        return dance;
    }

    private final List<Dance> dances = new ArrayList<>(List.of(
            dance("d2", "pop", 3),
            dance("d3", "ballroom", 1),
            dance("d1", "folk", 2)));

    @Test
    void sortsByProgramNumber() {
        dances.sort(DanceComparators.BY_PROGRAM_NUMBER);
        assertEquals(List.of(1, 2, 3), dances.stream().map(Dance::getNumber).toList());
    }

    @Test
    void sortsByType() {
        dances.sort(DanceComparators.BY_TYPE);
        assertEquals(List.of("ballroom", "folk", "pop"), dances.stream().map(Dance::getType).toList());
    }

    @Test
    void sortsById() {
        dances.sort(DanceComparators.BY_ID);
        assertEquals(List.of("d1", "d2", "d3"), dances.stream().map(Dance::getId).toList());
    }
}
