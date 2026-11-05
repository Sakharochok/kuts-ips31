package com.dance.model;

import java.util.Comparator;

public final class DanceComparators {
    private DanceComparators() {}

    public static final Comparator<Dance> BY_PROGRAM_NUMBER = Comparator.comparingInt(Dance::getNumber);
    public static final Comparator<Dance> BY_TYPE = Comparator.comparing(Dance::getType);
    public static final Comparator<Dance> BY_ID = Comparator.comparing(Dance::getId);
}
