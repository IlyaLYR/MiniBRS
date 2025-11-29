package ru.vsu.cs.odinaev.dto.group;

import java.util.UUID;

public class GroupResponse {
    private UUID id;
    private String name;
    private int course;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
