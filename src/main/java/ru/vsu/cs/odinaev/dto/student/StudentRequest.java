package ru.vsu.cs.odinaev.dto.student;

import java.util.UUID;

public class StudentRequest {
    private String name;
    private UUID groupId;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }
}