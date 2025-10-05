package ru.vsu.cs.odinaev.repository;

import org.junit.jupiter.api.*;
import ru.vsu.cs.odinaev.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocalRepositoryTest {

    private static final String TEST_RESOURCES_DIR = "src/test/resources/";
    private static final String GROUPS_FILE = TEST_RESOURCES_DIR + "test_groups.json";
    private static final String STUDENTS_FILE = TEST_RESOURCES_DIR + "test_students.json";
    private static final String TASKS_FILE = TEST_RESOURCES_DIR + "test_tasks.json";

    private LocalRepository<Group> groupRepository;
    private LocalRepository<Student> studentRepository;
    private LocalRepository<Task> taskRepository;

    @BeforeEach
    void setUp() throws IOException {
        // Создаем директорию если не существует
        Files.createDirectories(Paths.get(TEST_RESOURCES_DIR));

        // Перед каждым тестом создаем новые репозитории
        groupRepository = new LocalRepository<>(GROUPS_FILE, Group.class);
        studentRepository = new LocalRepository<>(STUDENTS_FILE, Student.class);
        taskRepository = new LocalRepository<>(TASKS_FILE, Task.class);
    }

    @AfterEach
    void tearDown() {
        // Очищаем данные после каждого теста
        groupRepository.deleteAll();
        studentRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Создание и поиск группы")
    void testSaveAndFindGroup() {
        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, "ИТ-21", 2);
        groupRepository.save(group);

        Optional<Group> found = groupRepository.findById(groupId);

        assertTrue(found.isPresent());
        assertEquals("ИТ-21", found.get().getName());
        assertEquals(2, found.get().getCourseNumber());
    }

    @Test
    @DisplayName("Поиск группы по названию с использованием Params")
    void testFindGroupByName() {
        Group group1 = new Group(UUID.randomUUID(), "ИТ-21", 2);
        Group group2 = new Group(UUID.randomUUID(), "ПИ-31", 3);
        groupRepository.save(group1);
        groupRepository.save(group2);

        Params<Group> params = new Params<>(Group.class, "name", "ИТ-21");
        List<Group> found = groupRepository.find(params);

        assertEquals(1, found.size());
        assertEquals("ИТ-21", found.get(0).getName());
    }

    @Test
    @DisplayName("Поиск группы по номеру курса с использованием Params")
    void testFindGroupByCourseNumber() {
        Group group1 = new Group(UUID.randomUUID(), "ИТ-21", 2);
        Group group2 = new Group(UUID.randomUUID(), "ПИ-31", 3);
        Group group3 = new Group(UUID.randomUUID(), "ИТ-22", 2);
        groupRepository.save(group1);
        groupRepository.save(group2);
        groupRepository.save(group3);

        Params<Group> params = new Params<>(Group.class, "courseNumber", 2);
        List<Group> found = groupRepository.find(params);

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(g -> g.getCourseNumber() == 2));
    }

    @Test
    @DisplayName("Обновление названия группы")
    void testUpdateGroupName() {
        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, "Старое название", 1);
        groupRepository.save(group);

        group.setName("Новое название");
        groupRepository.save(group);

        Optional<Group> found = groupRepository.findById(groupId);
        assertTrue(found.isPresent());
        assertEquals("Новое название", found.get().getName());
    }

    @Test
    @DisplayName("Создание студента с привязкой к группе")
    void testSaveStudentWithGroup() {
        // Создаем группу
        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, "ПИ-31", 3);
        groupRepository.save(group);

        // Создаем студента в этой группе
        UUID studentId = UUID.randomUUID();
        Student student = new Student(studentId, "Анна Петрова", groupId);
        studentRepository.save(student);

        Optional<Student> foundStudent = studentRepository.findById(studentId);
        assertTrue(foundStudent.isPresent());
        assertEquals("Анна Петрова", foundStudent.get().getName());
        assertEquals(groupId, foundStudent.get().getGroupId());
    }

    @Test
    @DisplayName("Поиск студентов по группе с использованием Params")
    void testFindStudentsByGroup() {
        UUID groupId = UUID.randomUUID();
        UUID otherGroupId = UUID.randomUUID();

        // Создаем группы
        groupRepository.save(new Group(groupId, "ПИ-21", 2));
        groupRepository.save(new Group(otherGroupId, "ИТ-31", 3));

        // Создаем студентов в группе
        Student s1 = new Student(UUID.randomUUID(), "Елена Смирнова", groupId);
        Student s2 = new Student(UUID.randomUUID(), "Дмитрий Попов", groupId);
        Student s3 = new Student(UUID.randomUUID(), "Ольга Новикова", groupId);
        // И одного студента в другой группе
        Student s4 = new Student(UUID.randomUUID(), "Иван ДругаяГруппа", otherGroupId);

        studentRepository.save(s1);
        studentRepository.save(s2);
        studentRepository.save(s3);
        studentRepository.save(s4);

        // Ищем студентов конкретной группы с использованием Params
        Params<Student> params = new Params<>(Student.class, "groupId", groupId);
        List<Student> groupStudents = studentRepository.find(params);

        assertEquals(3, groupStudents.size());
        assertTrue(groupStudents.stream().allMatch(s -> s.getGroupId().equals(groupId)));
    }

    @Test
    @DisplayName("Поиск студентов по имени с использованием Params")
    void testFindStudentsByName() {
        Student s1 = new Student(UUID.randomUUID(), "Иван Иванов", UUID.randomUUID());
        Student s2 = new Student(UUID.randomUUID(), "Петр Петров", UUID.randomUUID());
        Student s3 = new Student(UUID.randomUUID(), "Иван Сидоров", UUID.randomUUID());

        studentRepository.save(s1);
        studentRepository.save(s2);
        studentRepository.save(s3);

        Params<Student> params = new Params<>(Student.class, "name", "Иван Сидоров");
        List<Student> found = studentRepository.find(params);

        assertEquals(1, found.size());
        assertTrue(found.stream().allMatch(s -> s.getName().contains("Иван Сидоров")));
    }

    @Test
    @DisplayName("Перевод студента в другую группу")
    void testChangeStudentGroup() {
        UUID group1Id = UUID.randomUUID();
        UUID group2Id = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        // Создаем группы
        groupRepository.save(new Group(group1Id, "Группа А", 1));
        groupRepository.save(new Group(group2Id, "Группа Б", 1));

        // Создаем студента в первой группе
        Student student = new Student(studentId, "Иван Сидоров", group1Id);
        studentRepository.save(student);

        // Переводим студента во вторую группу
        Student updatedStudent = new Student(studentId, "Иван Сидоров", group2Id);
        studentRepository.save(updatedStudent);

        Optional<Student> found = studentRepository.findById(studentId);
        assertTrue(found.isPresent());
        assertEquals(group2Id, found.get().getGroupId());
    }

    @Test
    @DisplayName("Создание задания для студента")
    void testCreateTaskForStudent() {
        UUID studentId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        // Создаем студента
        studentRepository.save(new Student(studentId, "Мария Иванова", UUID.randomUUID()));

        // Создаем задание
        Task task = new Task(taskId, studentId, 1, TaskStatus.NOT_SUBMITTED);
        taskRepository.save(task);

        Optional<Task> foundTask = taskRepository.findById(taskId);
        assertTrue(foundTask.isPresent());
        assertEquals(studentId, foundTask.get().getStudentId());
        assertEquals(1, foundTask.get().getNumber());
        assertEquals(TaskStatus.NOT_SUBMITTED, foundTask.get().getStatus());
    }

    @Test
    @DisplayName("Поиск задач по студенту с использованием Params")
    void testFindTasksByStudent() {
        UUID student1Id = UUID.randomUUID();
        UUID student2Id = UUID.randomUUID();

        // Создаем задачи для разных студентов
        Task t1 = new Task(UUID.randomUUID(), student1Id, 1, TaskStatus.SUBMITTED);
        Task t2 = new Task(UUID.randomUUID(), student1Id, 2, TaskStatus.NOT_SUBMITTED);
        Task t3 = new Task(UUID.randomUUID(), student2Id, 1, TaskStatus.SUBMITTED);

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);

        // Ищем задачи конкретного студента
        Params<Task> params = new Params<>(Task.class, "studentId", student1Id);
        List<Task> studentTasks = taskRepository.find(params);

        assertEquals(2, studentTasks.size());
        assertTrue(studentTasks.stream().allMatch(t -> t.getStudentId().equals(student1Id)));
    }

    @Test
    @DisplayName("Поиск задач по статусу с использованием Params")
    void testFindTasksByStatus() {
        UUID studentId = UUID.randomUUID();

        Task t1 = new Task(UUID.randomUUID(), studentId, 1, TaskStatus.SUBMITTED);
        Task t2 = new Task(UUID.randomUUID(), studentId, 2, TaskStatus.NOT_SUBMITTED);
        Task t3 = new Task(UUID.randomUUID(), studentId, 3, TaskStatus.SUBMITTED);

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);

        Params<Task> params = new Params<>(Task.class, "status", TaskStatus.SUBMITTED);
        List<Task> submittedTasks = taskRepository.find(params);

        assertEquals(2, submittedTasks.size());
        assertTrue(submittedTasks.stream().allMatch(t -> t.getStatus() == TaskStatus.SUBMITTED));
    }

    @Test
    @DisplayName("Поиск задач по нескольким параметрам с использованием Params")
    void testFindTasksByMultipleParams() {
        UUID student1Id = UUID.randomUUID();
        UUID student2Id = UUID.randomUUID();

        Task t1 = new Task(UUID.randomUUID(), student1Id, 1, TaskStatus.SUBMITTED);
        Task t2 = new Task(UUID.randomUUID(), student1Id, 2, TaskStatus.NOT_SUBMITTED);
        Task t3 = new Task(UUID.randomUUID(), student2Id, 1, TaskStatus.SUBMITTED);

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);

        // Ищем сданные задачи конкретного студента
        java.util.Map<String, Object> filterParams = new java.util.HashMap<>();
        filterParams.put("studentId", student1Id);
        filterParams.put("status", TaskStatus.SUBMITTED);

        Params<Task> params = new Params<>(Task.class, filterParams);
        List<Task> found = taskRepository.find(params);

        assertEquals(1, found.size());
        assertEquals(student1Id, found.get(0).getStudentId());
        assertEquals(TaskStatus.SUBMITTED, found.get(0).getStatus());
    }

    @Test
    @DisplayName("Изменение статуса задания")
    void testUpdateTaskStatus() {
        UUID taskId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        Task task = new Task(taskId, studentId, 2, TaskStatus.NOT_SUBMITTED);
        taskRepository.save(task);

        // Обновляем статус задания
        Task updatedTask = new Task(taskId, studentId, 2, TaskStatus.SUBMITTED);
        taskRepository.save(updatedTask);

        Optional<Task> found = taskRepository.findById(taskId);
        assertTrue(found.isPresent());
        assertEquals(TaskStatus.SUBMITTED, found.get().getStatus());
    }

    @Test
    @DisplayName("Проверка существования с использованием Params")
    void testExistsWithParams() {
        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, "ИТ-21", 2);
        groupRepository.save(group);

        Params<Group> params = new Params<>(Group.class, "name", "ИТ-21");
        assertTrue(groupRepository.exists(params));

        Params<Group> nonExistentParams = new Params<>(Group.class, "name", "Несуществующая");
        assertFalse(groupRepository.exists(nonExistentParams));
    }

    @Test
    @DisplayName("Удаление по параметрам с использованием Params")
    void testDeleteWithParams() {
        UUID groupId = UUID.randomUUID();
        Group group1 = new Group(groupId, "ИТ-21", 2);
        Group group2 = new Group(UUID.randomUUID(), "ПИ-31", 3);
        groupRepository.save(group1);
        groupRepository.save(group2);

        // Удаляем группу по названию
        Params<Group> params = new Params<>(Group.class, "name", "ИТ-21");
        int deletedCount = groupRepository.delete(params);

        assertEquals(1, deletedCount);
        assertFalse(groupRepository.existsById(groupId));
        assertEquals(1, groupRepository.findAll().size());
    }

    @Test
    @DisplayName("Полная цепочка: Группа -> Студент -> Задание")
    void testGroupStudentTaskChain() {
        // Создаем группу
        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, "ИТ-41", 4);
        groupRepository.save(group);

        // Создаем студента в группе
        UUID studentId = UUID.randomUUID();
        Student student = new Student(studentId, "Алексей Козлов", groupId);
        studentRepository.save(student);

        // Создаем несколько заданий для студента
        Task task1 = new Task(UUID.randomUUID(), studentId, 1, TaskStatus.SUBMITTED);
        Task task2 = new Task(UUID.randomUUID(), studentId, 2, TaskStatus.NOT_SUBMITTED);
        Task task3 = new Task(UUID.randomUUID(), studentId, 3, TaskStatus.SUBMITTED);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);

        // Проверяем целостность данных
        Optional<Group> foundGroup = groupRepository.findById(groupId);
        Optional<Student> foundStudent = studentRepository.findById(studentId);

        Params<Task> taskParams = new Params<>(Task.class, "studentId", studentId);
        List<Task> studentTasks = taskRepository.find(taskParams);

        assertTrue(foundGroup.isPresent());
        assertTrue(foundStudent.isPresent());
        assertEquals(3, studentTasks.size());
        assertEquals(groupId, foundStudent.get().getGroupId());
    }

    @Test
    @DisplayName("Статистика по заданиям студента с использованием Params")
    void testStudentTaskStatistics() {
        UUID studentId = UUID.randomUUID();

        // Создаем задания с разными статусами
        Task submitted1 = new Task(UUID.randomUUID(), studentId, 1, TaskStatus.SUBMITTED);
        Task submitted2 = new Task(UUID.randomUUID(), studentId, 2, TaskStatus.SUBMITTED);
        Task notSubmitted1 = new Task(UUID.randomUUID(), studentId, 3, TaskStatus.NOT_SUBMITTED);
        Task notSubmitted2 = new Task(UUID.randomUUID(), studentId, 4, TaskStatus.NOT_SUBMITTED);
        Task notSubmitted3 = new Task(UUID.randomUUID(), studentId, 5, TaskStatus.NOT_SUBMITTED);

        taskRepository.save(submitted1);
        taskRepository.save(submitted2);
        taskRepository.save(notSubmitted1);
        taskRepository.save(notSubmitted2);
        taskRepository.save(notSubmitted3);

        // Используем Params для фильтрации
        Params<Task> allParams = new Params<>(Task.class, "studentId", studentId);
        List<Task> studentTasks = taskRepository.find(allParams);

        // Для фильтрации по нескольким параметрам используем Map
        java.util.Map<String, Object> submittedFilter = new java.util.HashMap<>();
        submittedFilter.put("studentId", studentId);
        submittedFilter.put("status", TaskStatus.SUBMITTED);
        Params<Task> submittedParams = new Params<>(Task.class, submittedFilter);
        List<Task> submittedTasks = taskRepository.find(submittedParams);

        java.util.Map<String, Object> notSubmittedFilter = new java.util.HashMap<>();
        notSubmittedFilter.put("studentId", studentId);
        notSubmittedFilter.put("status", TaskStatus.NOT_SUBMITTED);
        Params<Task> notSubmittedParams = new Params<>(Task.class, notSubmittedFilter);
        List<Task> notSubmittedTasks = taskRepository.find(notSubmittedParams);

        assertEquals(5, studentTasks.size());
        assertEquals(2, submittedTasks.size());
        assertEquals(3, notSubmittedTasks.size());
    }

    @Test
    @DisplayName("Сохранение данных между сессиями")
    void testDataPersistenceBetweenSessions() {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        // Сохраняем данные в первой "сессии"
        groupRepository.save(new Group(groupId, "Сохраняемая группа", 3));
        studentRepository.save(new Student(studentId, "Сохраняемый студент", groupId));
        taskRepository.save(new Task(taskId, studentId, 7, TaskStatus.SUBMITTED));

        // Создаем новые репозитории (имитируем перезапуск приложения)
        LocalRepository<Group> newGroupRepo = new LocalRepository<>(GROUPS_FILE, Group.class);
        LocalRepository<Student> newStudentRepo = new LocalRepository<>(STUDENTS_FILE, Student.class);
        LocalRepository<Task> newTaskRepo = new LocalRepository<>(TASKS_FILE, Task.class);

        // Проверяем что данные сохранились
        assertTrue(newGroupRepo.existsById(groupId));
        assertTrue(newStudentRepo.existsById(studentId));
        assertTrue(newTaskRepo.existsById(taskId));

        // Проверяем поиск по параметрам в новой сессии
        Params<Group> groupParams = new Params<>(Group.class, "name", "Сохраняемая группа");
        Params<Student> studentParams = new Params<>(Student.class, "groupId", groupId);
        Params<Task> taskParams = new Params<>(Task.class, "studentId", studentId);

        assertTrue(newGroupRepo.exists(groupParams));
        assertEquals(1, newStudentRepo.find(studentParams).size());
        assertEquals(1, newTaskRepo.find(taskParams).size());
    }

    @Test
    @DisplayName("Работа с пустыми репозиториями")
    void testEmptyRepositories() {
        assertTrue(groupRepository.findAll().isEmpty());
        assertTrue(studentRepository.findAll().isEmpty());
        assertTrue(taskRepository.findAll().isEmpty());

        // Поиск несуществующих entities
        assertFalse(groupRepository.findById(UUID.randomUUID()).isPresent());
        assertFalse(studentRepository.findById(UUID.randomUUID()).isPresent());
        assertFalse(taskRepository.findById(UUID.randomUUID()).isPresent());

        // Проверка существования по параметрам
        Params<Group> groupParams = new Params<>(Group.class, "name", "Несуществующая");
        assertFalse(groupRepository.exists(groupParams));

        // Удаление по параметрам (ничего не должно удалиться)
        Params<Student> studentParams = new Params<>(Student.class, "name", "Несуществующий");
        assertEquals(0, studentRepository.delete(studentParams));
    }
}