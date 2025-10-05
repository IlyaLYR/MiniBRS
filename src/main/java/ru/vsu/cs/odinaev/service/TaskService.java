package ru.vsu.cs.odinaev.service;

import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.model.TaskStatus;
import ru.vsu.cs.odinaev.repository.Params;
import ru.vsu.cs.odinaev.repository.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TaskService(Repository<Task> taskRepository) implements Service {

    private static final int REQUIRED_TASKS_COUNT = 3;

    public void initializeStudentTasks(UUID studentId) {
        for (int i = 1; i <= REQUIRED_TASKS_COUNT; i++) {
            UUID taskId = UUID.randomUUID();
            Task task = new Task(taskId, studentId, i, TaskStatus.NOT_SUBMITTED);
            taskRepository.save(task);
        }
    }

    public void updateTaskStatus(UUID taskId, TaskStatus status) {
        Task task = getTaskById(taskId);
        task.setStatus(status);
        taskRepository.save(task);
    }

    public Task getTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача с ID " + taskId + " не найдена"));
    }

    public List<Task> getTasksByStudent(UUID studentId) {
        Params<Task> params = new Params<>(Task.class, "studentId", studentId);
        List<Task> tasks = taskRepository.find(params);

        // Сортировка по номеру задачи
        tasks.sort((t1, t2) -> Integer.compare(t1.getNumber(), t2.getNumber()));
        return tasks;
    }

    public int getCompletedTasksCount(UUID studentId) {
        Map<String, Object> filterParams = Map.of(
                "studentId", studentId,
                "status", TaskStatus.SUBMITTED
        );
        Params<Task> params = new Params<>(Task.class, filterParams);
        return taskRepository.find(params).size();
    }

    public int getPendingTasksCount(UUID studentId) {
        Map<String, Object> filterParams = Map.of(
                "studentId", studentId,
                "status", TaskStatus.NOT_SUBMITTED
        );
        Params<Task> params = new Params<>(Task.class, filterParams);
        return taskRepository.find(params).size();
    }

    /**
     * Получить задачи по статусу (использует Repository.find())
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        Params<Task> params = new Params<>(Task.class, "status", status);
        return taskRepository.find(params);
    }

    /**
     * Получить задачу по студенту и номеру (использует Repository.findFirst())
     */
    public Task getTaskByStudentAndNumber(UUID studentId, int taskNumber) {
        Map<String, Object> filterParams = Map.of(
                "studentId", studentId,
                "number", taskNumber
        );
        Params<Task> params = new Params<>(Task.class, filterParams);
        return taskRepository.findFirst(params)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Задача №" + taskNumber + " для студента " + studentId + " не найдена"));
    }

    /**
     * Проверить, существует ли задача у студента с указанным номером
     */
    public boolean taskExistsByStudentAndNumber(UUID studentId, int taskNumber) {
        Map<String, Object> filterParams = Map.of(
                "studentId", studentId,
                "number", taskNumber
        );
        Params<Task> params = new Params<>(Task.class, filterParams);
        return taskRepository.exists(params);
    }

    /**
     * Удалить все задачи студента (использует Repository.delete())
     */
    public int deleteStudentTasks(UUID studentId) {
        Params<Task> params = new Params<>(Task.class, "studentId", studentId);
        return taskRepository.delete(params);
    }

    /**
     * Получить общее количество задач в системе
     */
    public int getTotalTasksCount() {
        return taskRepository.findAll().size();
    }

    /**
     * Получить количество задач по статусу
     */
    public int getTasksCountByStatus(TaskStatus status) {
        Params<Task> params = new Params<>(Task.class, "status", status);
        return taskRepository.find(params).size();
    }
}