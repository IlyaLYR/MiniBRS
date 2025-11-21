package ru.vsu.cs.odinaev.repository;

import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.model.TaskStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ITaskRepository {
    void save(Task task);
    Optional<Task> findById(UUID id);
    List<Task> findByStudentId(UUID studentId);
    List<Task> findByStatus(TaskStatus status);
    void updateStatus(UUID taskId, TaskStatus status);
    void delete(UUID id);
    Optional<Task> findByStudentIdAndNumber(UUID studentId, int number);
    void deleteByStudentId(UUID studentId);
    int countByStudentIdAndStatus(UUID studentId, TaskStatus status);
}