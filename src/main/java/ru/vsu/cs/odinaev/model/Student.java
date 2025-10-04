package ru.vsu.cs.odinaev.model;

import java.util.UUID;

public class Student {
    private final UUID id;
    private String name;
    private UUID groupId;

    public Student(UUID id, String name, UUID groupId) {
        this.id = id;
        this.name = name;
        this.groupId = groupId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }
}
