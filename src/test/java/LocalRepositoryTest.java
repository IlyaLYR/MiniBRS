
import org.junit.jupiter.api.*;
import ru.vsu.cs.odinaev.model.*;
import ru.vsu.cs.odinaev.repository.LocalRepository;

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

//    @AfterEach
//    void tearDown() {
//        // Очищаем данные после каждого теста
//        taskRepository.findAll().forEach(task -> taskRepository.deleteById(task.getId()));
//        studentRepository.findAll().forEach(student -> studentRepository.deleteById(student.getId()));
//        groupRepository.findAll().forEach(group -> groupRepository.deleteById(group.getId()));
//    }

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
    @DisplayName("Обновление названия группы")
    void testUpdateGroupName() {
        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, "Старое название", 1);
        groupRepository.save(group);

        group.setName("Новое название");

//        Group updatedGroup = new Group(groupId, "Новое название", 1);
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
        List<Task> studentTasks = taskRepository.findAll().stream()
                .filter(task -> task.getStudentId().equals(studentId))
                .toList();

        assertTrue(foundGroup.isPresent());
        assertTrue(foundStudent.isPresent());
        assertEquals(3, studentTasks.size());
        assertEquals(groupId, foundStudent.get().getGroupId());
    }

    @Test
    @DisplayName("Поиск всех студентов группы")
    void testFindStudentsByGroup() {
        UUID groupId = UUID.randomUUID();

        // Создаем группу
        groupRepository.save(new Group(groupId, "ПИ-21", 2));

        // Создаем студентов в группе
        Student s1 = new Student(UUID.randomUUID(), "Елена Смирнова", groupId);
        Student s2 = new Student(UUID.randomUUID(), "Дмитрий Попов", groupId);
        Student s3 = new Student(UUID.randomUUID(), "Ольга Новикова", groupId);

        // И одного студента в другой группе
        Student s4 = new Student(UUID.randomUUID(), "Иван ДругаяГруппа", UUID.randomUUID());

        studentRepository.save(s1);
        studentRepository.save(s2);
        studentRepository.save(s3);
        studentRepository.save(s4);

        // Ищем студентов конкретной группы
        List<Student> groupStudents = studentRepository.findAll().stream()
                .filter(student -> student.getGroupId().equals(groupId))
                .toList();

        assertEquals(3, groupStudents.size());
        assertTrue(groupStudents.stream().allMatch(s -> s.getGroupId().equals(groupId)));
    }

    @Test
    @DisplayName("Статистика по заданиям студента")
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

        List<Task> studentTasks = taskRepository.findAll().stream()
                .filter(task -> task.getStudentId().equals(studentId))
                .toList();

        long submittedCount = studentTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.SUBMITTED)
                .count();

        long notSubmittedCount = studentTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.NOT_SUBMITTED)
                .count();

        assertEquals(5, studentTasks.size());
        assertEquals(2, submittedCount);
        assertEquals(3, notSubmittedCount);
    }

    @Test
    @DisplayName("Удаление группы с каскадным удалением")
    void testDeleteGroupCascade() {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        // Создаем цепочку данных
        groupRepository.save(new Group(groupId, "Удаляемая группа", 1));
        studentRepository.save(new Student(studentId, "Студент", groupId));
        taskRepository.save(new Task(taskId, studentId, 1, TaskStatus.SUBMITTED));

        // Удаляем группу
        groupRepository.deleteById(groupId);

        // Проверяем что группа удалена, но студент и задание остались
        assertFalse(groupRepository.existsById(groupId));
        assertTrue(studentRepository.existsById(studentId));
        assertTrue(taskRepository.existsById(taskId));

        // Студент теперь ссылается на несуществующую группу
        Optional<Student> student = studentRepository.findById(studentId);
        assertTrue(student.isPresent());
        assertEquals(groupId, student.get().getGroupId()); // Ссылка сохранилась
    }

    @Test
    @DisplayName("Удаление студента с его заданиями")
    void testDeleteStudentWithTasks() {
        UUID studentId = UUID.randomUUID();
        UUID task1Id = UUID.randomUUID();
        UUID task2Id = UUID.randomUUID();

        // Создаем студента и его задания
        studentRepository.save(new Student(studentId, "Студент для удаления", UUID.randomUUID()));
        taskRepository.save(new Task(task1Id, studentId, 1, TaskStatus.SUBMITTED));
        taskRepository.save(new Task(task2Id, studentId, 2, TaskStatus.NOT_SUBMITTED));

        // Удаляем студента
        studentRepository.deleteById(studentId);

        // Проверяем что студент удален, но задания остались
        assertFalse(studentRepository.existsById(studentId));
        assertTrue(taskRepository.existsById(task1Id));
        assertTrue(taskRepository.existsById(task2Id));

        // Задания все еще ссылаются на удаленного студента
        Optional<Task> task1 = taskRepository.findById(task1Id);
        assertTrue(task1.isPresent());
        assertEquals(studentId, task1.get().getStudentId());
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

        Optional<Group> reloadedGroup = newGroupRepo.findById(groupId);
        Optional<Student> reloadedStudent = newStudentRepo.findById(studentId);
        Optional<Task> reloadedTask = newTaskRepo.findById(taskId);

        assertTrue(reloadedGroup.isPresent());
        assertEquals("Сохраняемая группа", reloadedGroup.get().getName());

        assertTrue(reloadedStudent.isPresent());
        assertEquals("Сохраняемый студент", reloadedStudent.get().getName());

        assertTrue(reloadedTask.isPresent());
        assertEquals(7, reloadedTask.get().getNumber());
        assertEquals(TaskStatus.SUBMITTED, reloadedTask.get().getStatus());
    }

    @Test
    @DisplayName("Проверка уникальности ID")
    void testUniqueIds() {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Group group1 = new Group(groupId, "Группа 1", 1);
        Group group2 = new Group(groupId, "Группа 2", 2); // Тот же ID

        groupRepository.save(group1);
        groupRepository.save(group2); // Должно перезаписать

        List<Group> allGroups = groupRepository.findAll();
        assertEquals(1, allGroups.size());
        assertEquals("Группа 2", allGroups.get(0).getName());
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

        // Удаление несуществующих entities
        assertFalse(groupRepository.deleteById(UUID.randomUUID()));
        assertFalse(studentRepository.deleteById(UUID.randomUUID()));
        assertFalse(taskRepository.deleteById(UUID.randomUUID()));
    }
}