package com.dance.model;

import java.util.Objects;

public class Dancer {
    private String name;
    private int age;
    private int experienceYears;

    public Dancer() {}

    public Dancer(String name, int age, int experienceYears) {
        this.name = name;
        this.age = age;
        this.experienceYears = experienceYears;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dancer dancer = (Dancer) o;
        return age == dancer.age && experienceYears == dancer.experienceYears && Objects.equals(name, dancer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, experienceYears);
    }

    @Override
    public String toString() {
        return "Dancer{" + name + ", age=" + age + ", exp=" + experienceYears + '}';
    }
}
