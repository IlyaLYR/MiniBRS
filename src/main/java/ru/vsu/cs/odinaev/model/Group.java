package ru.vsu.cs.odinaev.model;

import java.util.UUID;

public class Group {
    private final UUID id;
    private String name;
    private int courseNumber;

    public Group(UUID id, String name, int courseNumber) {
        this.id = id;
        this.name = name;
        this.courseNumber = courseNumber;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCourseNumber(int courseNumber) {
        this.courseNumber = courseNumber;
    }
}
