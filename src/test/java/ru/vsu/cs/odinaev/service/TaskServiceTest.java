package ru.vsu.cs.odinaev.service;

import org.junit.jupiter.api.*;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.model.TaskStatus;
import ru.vsu.cs.odinaev.repository.LocalRepository;
import ru.vsu.cs.odinaev.repository.Params;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private LocalRepository<Task> taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = new LocalRepository<>("src/test/resources/test_tasks.json", Task.class);
        taskService = new TaskService(taskRepository);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Инициализация задач для студента")
    void initializeStudentTasks_NewStudent_CreatesThreeTasks() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act
        taskService.initializeStudentTasks(studentId);

        // Assert
        Params<Task> params = new Params<>(Task.class, "studentId", studentId);
        List<Task> tasks = taskRepository.find(params);

        assertEquals(3, tasks.size());

        // СОРТИРУЕМ задачи по номеру перед проверкой
        tasks.sort((t1, t2) -> Integer.compare(t1.getNumber(), t2.getNumber()));

        // Проверяем, что задачи созданы с правильными номерами и статусами
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            assertEquals(i + 1, task.getNumber());
            assertEquals(TaskStatus.NOT_SUBMITTED, task.getStatus());
            assertEquals(studentId, task.getStudentId());
            assertNotNull(task.getId());
        }
    }

    @Test
    @DisplayName("Обновление статуса задачи")
    void updateTaskStatus_ExistingTask_UpdatesStatus() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Task task = new Task(taskId, studentId, 1, TaskStatus.NOT_SUBMITTED);
        taskRepository.save(task);

        // Act
        taskService.updateTaskStatus(taskId, TaskStatus.SUBMITTED);

        // Assert
        Task updatedTask = taskService.getTaskById(taskId);
        assertEquals(TaskStatus.SUBMITTED, updatedTask.getStatus());
        // Проверяем, что остальные поля не изменились
        assertEquals(studentId, updatedTask.getStudentId());
        assertEquals(1, updatedTask.getNumber());
    }

    @Test
    @DisplayName("Обновление статуса несуществующей задачи")
    void updateTaskStatus_NonExistingTask_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                taskService.updateTaskStatus(UUID.randomUUID(), TaskStatus.SUBMITTED));
    }

    @Test
    @DisplayName("Получение задачи по ID")
    void getTaskById_ExistingTask_ReturnsTask() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Task expectedTask = new Task(taskId, studentId, 1, TaskStatus.SUBMITTED);
        taskRepository.save(expectedTask);

        // Act
        Task actualTask = taskService.getTaskById(taskId);

        // Assert
        assertEquals(expectedTask.getId(), actualTask.getId());
        assertEquals(studentId, actualTask.getStudentId());
        assertEquals(1, actualTask.getNumber());
        assertEquals(TaskStatus.SUBMITTED, actualTask.getStatus());
    }

    @Test
    @DisplayName("Получение несуществующей задачи по ID")
    void getTaskById_NonExistingTask_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                taskService.getTaskById(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Получение задач студента")
    void getTasksByStudent_ExistingStudent_ReturnsTasks() {
        // Arrange
        UUID student1Id = UUID.randomUUID();
        UUID student2Id = UUID.randomUUID();

        Task task1 = new Task(UUID.randomUUID(), student1Id, 1, TaskStatus.SUBMITTED);
        Task task2 = new Task(UUID.randomUUID(), student1Id, 2, TaskStatus.NOT_SUBMITTED);
        Task task3 = new Task(UUID.randomUUID(), student2Id, 1, TaskStatus.SUBMITTED);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        // Act
        List<Task> studentTasks = taskService.getTasksByStudent(student1Id);

        // Assert
        assertEquals(2, studentTasks.size());
        assertTrue(studentTasks.stream().allMatch(t -> t.getStudentId().equals(student1Id)));
        // Проверяем сортировку по номеру задачи
        assertEquals(1, studentTasks.get(0).getNumber());
        assertEquals(2, studentTasks.get(1).getNumber());
    }

    @Test
    @DisplayName("Получение задач несуществующего студента")
    void getTasksByStudent_NonExistingStudent_ReturnsEmptyList() {
        // Act
        List<Task> tasks = taskService.getTasksByStudent(UUID.randomUUID());

        // Assert
        assertTrue(tasks.isEmpty());
    }

    @Test
    @DisplayName("Получение количества сданных задач студента")
    void getCompletedTasksCount_WithCompletedTasks_ReturnsCount() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        Task submitted1 = new Task(UUID.randomUUID(), studentId, 1, TaskStatus.SUBMITTED);
        Task submitted2 = new Task(UUID.randomUUID(), studentId, 2, TaskStatus.SUBMITTED);
        Task notSubmitted = new Task(UUID.randomUUID(), studentId, 3, TaskStatus.NOT_SUBMITTED);

        taskRepository.save(submitted1);
        taskRepository.save(submitted2);
        taskRepository.save(notSubmitted);

        // Act
        int completedCount = taskService.getCompletedTasksCount(studentId);

        // Assert
        assertEquals(2, completedCount);
    }

    @Test
    @DisplayName("Получение количества несданных задач студента")
    void getPendingTasksCount_WithPendingTasks_ReturnsCount() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        Task submitted = new Task(UUID.randomUUID(), studentId, 1, TaskStatus.SUBMITTED);
        Task notSubmitted1 = new Task(UUID.randomUUID(), studentId, 2, TaskStatus.NOT_SUBMITTED);
        Task notSubmitted2 = new Task(UUID.randomUUID(), studentId, 3, TaskStatus.NOT_SUBMITTED);

        taskRepository.save(submitted);
        taskRepository.save(notSubmitted1);
        taskRepository.save(notSubmitted2);

        // Act
        int pendingCount = taskService.getPendingTasksCount(studentId);

        // Assert
        assertEquals(2, pendingCount);
    }

    @Test
    @DisplayName("Получение задач по статусу")
    void getTasksByStatus_ExistingStatus_ReturnsTasks() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        Task submitted1 = new Task(UUID.randomUUID(), studentId, 1, TaskStatus.SUBMITTED);
        Task submitted2 = new Task(UUID.randomUUID(), studentId, 2, TaskStatus.SUBMITTED);
        Task notSubmitted = new Task(UUID.randomUUID(), studentId, 3, TaskStatus.NOT_SUBMITTED);

        taskRepository.save(submitted1);
        taskRepository.save(submitted2);
        taskRepository.save(notSubmitted);

        // Act
        List<Task> submittedTasks = taskService.getTasksByStatus(TaskStatus.SUBMITTED);

        // Assert
        assertEquals(2, submittedTasks.size());
        assertTrue(submittedTasks.stream().allMatch(t -> t.getStatus() == TaskStatus.SUBMITTED));
    }

    @Test
    @DisplayName("Получение задачи по студенту и номеру")
    void getTaskByStudentAndNumber_ExistingTask_ReturnsTask() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        Task expectedTask = new Task(UUID.randomUUID(), studentId, 2, TaskStatus.SUBMITTED);
        taskRepository.save(expectedTask);

        // Act
        Task actualTask = taskService.getTaskByStudentAndNumber(studentId, 2);

        // Assert
        assertEquals(expectedTask.getId(), actualTask.getId());
        assertEquals(studentId, actualTask.getStudentId());
        assertEquals(2, actualTask.getNumber());
        assertEquals(TaskStatus.SUBMITTED, actualTask.getStatus());
    }

    @Test
    @DisplayName("Получение несуществующей задачи по студенту и номеру")
    void getTaskByStudentAndNumber_NonExistingTask_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                taskService.getTaskByStudentAndNumber(UUID.randomUUID(), 1));
    }

    @Test
    @DisplayName("Проверка существования задачи по студенту и номеру")
    void taskExistsByStudentAndNumber_ExistingTask_ReturnsTrue() {
        // Arrange
        UUID studentId = UUID.randomUUID();
        Task task = new Task(UUID.randomUUID(), studentId, 3, TaskStatus.NOT_SUBMITTED);
        taskRepository.save(task);

        // Act
        boolean exists = taskService.taskExistsByStudentAndNumber(studentId, 3);

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Проверка существования несуществующей задачи по студенту и номеру")
    void taskExistsByStudentAndNumber_NonExistingTask_ReturnsFalse() {
        // Act
        boolean exists = taskService.taskExistsByStudentAndNumber(UUID.randomUUID(), 1);

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Удаление всех задач студента")
    void deleteStudentTasks_ExistingStudent_DeletesAllTasks() {
        // Arrange
        UUID studentId = UUID.randomUUID();

        Task task1 = new Task(UUID.randomUUID(), studentId, 1, TaskStatus.SUBMITTED);
        Task task2 = new Task(UUID.randomUUID(), studentId, 2, TaskStatus.NOT_SUBMITTED);
        Task task3 = new Task(UUID.randomUUID(), UUID.randomUUID(), 1, TaskStatus.SUBMITTED); // другой студент

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        // Act
        int deletedCount = taskService.deleteStudentTasks(studentId);

        // Assert
        assertEquals(2, deletedCount);

        // Проверяем, что задачи удалены
        Params<Task> params = new Params<>(Task.class, "studentId", studentId);
        List<Task> remainingTasks = taskRepository.find(params);
        assertTrue(remainingTasks.isEmpty());

        // Проверяем, что задача другого студента осталась
        assertEquals(1, taskRepository.findAll().size());
    }

    @Test
    @DisplayName("Удаление задач несуществующего студента")
    void deleteStudentTasks_NonExistingStudent_ReturnsZero() {
        // Act
        int deletedCount = taskService.deleteStudentTasks(UUID.randomUUID());

        // Assert
        assertEquals(0, deletedCount);
    }

    @Test
    @DisplayName("Получение общего количества задач")
    void getTotalTasksCount_WithTasks_ReturnsCount() {
        // Arrange
        taskRepository.save(new Task(UUID.randomUUID(), UUID.randomUUID(), 1, TaskStatus.SUBMITTED));
        taskRepository.save(new Task(UUID.randomUUID(), UUID.randomUUID(), 2, TaskStatus.NOT_SUBMITTED));
        taskRepository.save(new Task(UUID.randomUUID(), UUID.randomUUID(), 3, TaskStatus.SUBMITTED));

        // Act
        int totalCount = taskService.getTotalTasksCount();

        // Assert
        assertEquals(3, totalCount);
    }

    @Test
    @DisplayName("Получение количества задач по статусу")
    void getTasksCountByStatus_WithTasks_ReturnsCount() {
        // Arrange
        taskRepository.save(new Task(UUID.randomUUID(), UUID.randomUUID(), 1, TaskStatus.SUBMITTED));
        taskRepository.save(new Task(UUID.randomUUID(), UUID.randomUUID(), 2, TaskStatus.SUBMITTED));
        taskRepository.save(new Task(UUID.randomUUID(), UUID.randomUUID(), 3, TaskStatus.NOT_SUBMITTED));

        // Act
        int submittedCount = taskService.getTasksCountByStatus(TaskStatus.SUBMITTED);
        int notSubmittedCount = taskService.getTasksCountByStatus(TaskStatus.NOT_SUBMITTED);

        // Assert
        assertEquals(2, submittedCount);
        assertEquals(1, notSubmittedCount);
    }

    @Test
    @DisplayName("Получение репозитория задач")
    void taskRepository_ReturnsCorrectRepository() {
        // Act & Assert
        assertNotNull(taskService.taskRepository());
        assertEquals(taskRepository, taskService.taskRepository());
    }
}