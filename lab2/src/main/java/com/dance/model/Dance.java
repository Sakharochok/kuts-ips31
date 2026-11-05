package com.dance.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dance {
    private String id;
    private String type;
    private String title;
    private String scene;
    private String numberOfDancers;
    private String music;
    private List<Dancer> dancers = new ArrayList<>();
    private int number;

    public Dance() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }

    public String getNumberOfDancers() { return numberOfDancers; }
    public void setNumberOfDancers(String numberOfDancers) { this.numberOfDancers = numberOfDancers; }

    public String getMusic() { return music; }
    public void setMusic(String music) { this.music = music; }

    public List<Dancer> getDancers() { return dancers; }
    public void setDancers(List<Dancer> dancers) { this.dancers = dancers; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dance dance = (Dance) o;
        return number == dance.number && Objects.equals(id, dance.id) && Objects.equals(type, dance.type)
                && Objects.equals(scene, dance.scene) && Objects.equals(numberOfDancers, dance.numberOfDancers)
                && Objects.equals(music, dance.music) && Objects.equals(dancers, dance.dancers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, number, scene, numberOfDancers, music, dancers);
    }

    @Override
    public String toString() {
        return String.format("#%d [%s] %s (id=%s, scene=%s, dancers=%d)",
                number, type, title != null ? title : "", id, scene, dancers.size());
    }
}
