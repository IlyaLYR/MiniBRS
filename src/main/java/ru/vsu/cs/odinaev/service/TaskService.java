package ru.vsu.cs.odinaev.service;

import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.model.TaskStatus;
import ru.vsu.cs.odinaev.repository.TaskRepository;

import java.util.List;
import java.util.UUID;

public record TaskService(TaskRepository taskRepository) implements ITaskService {

    private static final int REQUIRED_TASKS_COUNT = 3;

    public void initializeStudentTasks(UUID studentId) {
        for (int i = 1; i <= REQUIRED_TASKS_COUNT; i++) {
            UUID taskId = UUID.randomUUID();
            Task task = new Task(taskId, studentId, i, TaskStatus.NOT_SUBMITTED);
            taskRepository.save(task);
        }
    }

    public void updateTaskStatus(UUID taskId, TaskStatus status) {
        getTaskById(taskId);
        taskRepository.updateStatus(taskId, status);
    }

    public void getTaskById(UUID taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача с ID " + taskId + " не найдена"));
    }

    public List<Task> getTasksByStudent(UUID studentId) {
        return taskRepository.findByStudentId(studentId);
    }

    public int getCompletedTasksCount(UUID studentId) {
        return taskRepository.countByStudentIdAndStatus(studentId, TaskStatus.SUBMITTED);
    }

    /**
     * Получить задачу по студенту и номеру (использует Repository.findFirst())
     */
    public Task getTaskByStudentAndNumber(UUID studentId, int taskNumber) {
        return taskRepository.findByStudentIdAndNumber(studentId, taskNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Задача №" + taskNumber + " для студента " + studentId + " не найдена"));
    }

    /**
     * Удалить все задачи студента (использует Repository.delete())
     */
    public void deleteStudentTasks(UUID studentId) {
        taskRepository.deleteByStudentId(studentId);
    }
}