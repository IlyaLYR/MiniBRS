package ru.vsu.cs.odinaev.service;

import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.model.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface ITaskService {
    void initializeStudentTasks(UUID studentId);
    void updateTaskStatus(UUID taskId, TaskStatus status);
    void getTaskById(UUID taskId);
    List<Task> getTasksByStudent(UUID studentId);
    int getCompletedTasksCount(UUID studentId);
    Task getTaskByStudentAndNumber(UUID studentId, int taskNumber);
    void deleteStudentTasks(UUID studentId);
}