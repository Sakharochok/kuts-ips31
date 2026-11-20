package com.dance.parser;

import com.dance.model.Dance;
import com.dance.model.Dancer;

import java.util.ArrayList;
import java.util.List;

/**
 * Спільна логіка побудови колекції {@link Dance} з подій розбору XML.
 * DOM, SAX і StAX парсери лише перетворюють свої події на виклики цього класу,
 * тому відображення "тег -> поле" описане в одному місці.
 */
public class DanceBuilder {
    private final List<Dance> dances = new ArrayList<>();
    private Dance currentDance;
    private Dancer currentDancer;

    public void startDance(String id, String type, String title) {
        currentDance = new Dance();
        currentDance.setId(id);
        currentDance.setType(type);
        currentDance.setTitle(title);
    }

    public void startDancer() {
        currentDancer = new Dancer();
    }

    /**
     * Записує текстове значення елемента у відповідне поле поточного танцю або танцюриста.
     * Невідомі та контейнерні елементи ігноруються.
     */
    public void setField(String tag, String text) {
        if (currentDance == null) {
            return;
        }
        switch (tag) {
            case DanceXml.SCENE -> currentDance.setScene(text);
            case DanceXml.NUMBER_OF_DANCERS -> currentDance.setNumberOfDancers(text);
            case DanceXml.MUSIC -> currentDance.setMusic(text);
            case DanceXml.NUMBER -> currentDance.setNumber(Integer.parseInt(text));
            case DanceXml.NAME -> {
                if (currentDancer != null) currentDancer.setName(text);
            }
            case DanceXml.AGE -> {
                if (currentDancer != null) currentDancer.setAge(Integer.parseInt(text));
            }
            case DanceXml.EXPERIENCE_YEARS -> {
                if (currentDancer != null) currentDancer.setExperienceYears(Integer.parseInt(text));
            }
            default -> { }
        }
    }

    public void endDancer() {
        if (currentDance != null && currentDancer != null) {
            currentDance.getDancers().add(currentDancer);
        }
        currentDancer = null;
    }

    public void endDance() {
        if (currentDance != null) {
            dances.add(currentDance);
        }
        currentDance = null;
    }

    public List<Dance> getDances() {
        return dances;
    }
}
