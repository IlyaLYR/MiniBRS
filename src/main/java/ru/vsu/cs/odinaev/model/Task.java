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

    @Override
    public String toString() {
        String statusEmoji = status == TaskStatus.SUBMITTED ? "✅" : "❌";
        String statusText = status == TaskStatus.SUBMITTED ? "СДАНО" : "НЕ СДАНО";

        return String.format("Задача{ID: %s, №%d, Статус: %s %s, Студент: %s}",
                id.toString().substring(0, 8) + "...",
                number,
                statusEmoji,
                statusText,
                studentId.toString().substring(0, 8) + "..."
        );
    }
}
