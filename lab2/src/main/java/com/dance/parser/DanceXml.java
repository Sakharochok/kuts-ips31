package com.dance.parser;

/**
 * Імена елементів, атрибутів та простір імен XML-документа (спільні для всіх парсерів).
 */
public final class DanceXml {
    private DanceXml() {}

    public static final String NAMESPACE = "http://example.com/dance";

    public static final String DANCE = "Dance";
    public static final String DANCER = "dancer";

    public static final String ATTR_ID = "id";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_TITLE = "title";

    public static final String SCENE = "scene";
    public static final String NUMBER_OF_DANCERS = "numberOfDancers";
    public static final String MUSIC = "music";
    public static final String NUMBER = "number";
    public static final String NAME = "name";
    public static final String AGE = "age";
    public static final String EXPERIENCE_YEARS = "experienceYears";
}
