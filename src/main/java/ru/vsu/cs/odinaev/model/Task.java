package ru.vsu.cs.odinaev.model;

import java.util.UUID;

public class Task {
    private final UUID id;
    private final UUID studentId;
    private final int number;
    private TaskStatus status;

    public Task(UUID id, UUID studentId, int number, TaskStatus status) {
        this.id = id;
        this.studentId = studentId;
        this.number = number;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public int getNumber() {
        return number;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
