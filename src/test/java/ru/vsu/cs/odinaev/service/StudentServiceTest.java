package ru.vsu.cs.odinaev.service;

import org.junit.jupiter.api.*;
import ru.vsu.cs.odinaev.model.Group;
import ru.vsu.cs.odinaev.model.Student;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.repository.LocalRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {

    private LocalRepository<Group> groupRepository;
    private LocalRepository<Student> studentRepository;
    private LocalRepository<Task> taskRepository;
    private GroupService groupService;
    private StudentService studentService;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        // Инициализируем репозитории
        groupRepository = new LocalRepository<>("src/test/resources/test_groups.json", Group.class);
        studentRepository = new LocalRepository<>("src/test/resources/test_students.json", Student.class);
        taskRepository = new LocalRepository<>("src/test/resources/test_tasks.json", Task.class);

        // Инициализируем сервисы в правильном порядке
        taskService = new TaskService(taskRepository);

        // Временный GroupService без StudentService
        GroupService tempGroupService = new GroupService(groupRepository, null);

        // StudentService с временным GroupService
        studentService = new StudentService(studentRepository, tempGroupService, taskService);

        // Финальный GroupService с правильным StudentService
        groupService = new GroupService(groupRepository, studentService);

        // Обновляем StudentService с финальным GroupService
        studentService = new StudentService(studentRepository, groupService, taskService);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        studentRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    @DisplayName("Создание студента с валидными данными")
    void createStudent_ValidData_Success() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        UUID groupId = group.getId();

        // Act
        Student student = studentService.createStudent("Иван Иванов", groupId);

        // Assert
        assertNotNull(student);
        assertNotNull(student.getId());
        assertEquals("Иван Иванов", student.getName());
        assertEquals(groupId, student.getGroupId());

        // Проверяем, что задачи инициализированы
        List<Task> tasks = taskService.getTasksByStudent(student.getId());
        assertEquals(3, tasks.size()); // 3 задачи по умолчанию
    }

    @Test
    @DisplayName("Создание студента с невалидным именем")
    void createStudent_InvalidName_ThrowsException() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                studentService.createStudent("", group.getId()));

        assertThrows(IllegalArgumentException.class, () ->
                studentService.createStudent(null, group.getId()));
    }

    @Test
    @DisplayName("Создание студента в несуществующей группе")
    void createStudent_NonExistingGroup_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                studentService.createStudent("Иван Иванов", UUID.randomUUID()));
    }

    @Test
    @DisplayName("Удаление существующего студента")
    void deleteStudent_ExistingStudent_Success() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        Student student = studentService.createStudent("Иван Иванов", group.getId());
        UUID studentId = student.getId();

        // Act
        studentService.deleteStudent(studentId);

        // Assert
        assertFalse(studentService.studentRepository().existsById(studentId));
    }

    @Test
    @DisplayName("Удаление несуществующего студента")
    void deleteStudent_NonExistingStudent_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                studentService.deleteStudent(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Получение всех студентов")
    void getAllStudents_WithStudents_ReturnsAllStudents() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        studentService.createStudent("Иван Иванов", group.getId());
        studentService.createStudent("Петр Петров", group.getId());

        // Act
        List<Student> students = studentService.getAllStudents();

        // Assert
        assertEquals(2, students.size());
    }

    @Test
    @DisplayName("Получение студентов по группе")
    void getStudentsByGroup_ExistingGroup_ReturnsStudents() {
        // Arrange
        Group group1 = groupService.createGroup("ИТ-21", 2);
        Group group2 = groupService.createGroup("ПИ-31", 3);

        studentService.createStudent("Иван Иванов", group1.getId());
        studentService.createStudent("Петр Петров", group1.getId());
        studentService.createStudent("Сергей Сергеев", group2.getId());

        // Act
        List<Student> students = studentService.getStudentsByGroup(group1.getId());

        // Assert
        assertEquals(2, students.size());
        assertTrue(students.stream().allMatch(s -> s.getGroupId().equals(group1.getId())));
    }

    @Test
    @DisplayName("Получение студентов по несуществующей группе")
    void getStudentsByGroup_NonExistingGroup_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                studentService.getStudentsByGroup(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Получение студента по ID")
    void getStudentById_ExistingStudent_ReturnsStudent() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        Student expectedStudent = studentService.createStudent("Иван Иванов", group.getId());

        // Act
        Student actualStudent = studentService.getStudentById(expectedStudent.getId());

        // Assert
        assertEquals(expectedStudent.getId(), actualStudent.getId());
        assertEquals("Иван Иванов", actualStudent.getName());
    }

    @Test
    @DisplayName("Получение несуществующего студента по ID")
    void getStudentById_NonExistingStudent_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                studentService.getStudentById(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Обновление студента")
    void updateStudent_ValidData_Success() {
        // Arrange
        Group group1 = groupService.createGroup("ИТ-21", 2);
        Group group2 = groupService.createGroup("ПИ-31", 3);
        Student student = studentService.createStudent("Старое Имя", group1.getId());

        // Act
        Student updatedStudent = studentService.updateStudent(
                student.getId(), "Новое Имя", group2.getId()
        );

        // Assert
        assertEquals("Новое Имя", updatedStudent.getName());
        assertEquals(group2.getId(), updatedStudent.getGroupId());
    }

    @Test
    @DisplayName("Обновление студента только именем")
    void updateStudent_OnlyName_Success() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        Student student = studentService.createStudent("Старое Имя", group.getId());

        // Act
        Student updatedStudent = studentService.updateStudent(
                student.getId(), "Новое Имя", null
        );

        // Assert
        assertEquals("Новое Имя", updatedStudent.getName());
        assertEquals(group.getId(), updatedStudent.getGroupId()); // Группа не изменилась
    }

    @Test
    @DisplayName("Получение задач студента")
    void getStudentTasks_ExistingStudent_ReturnsTasks() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        Student student = studentService.createStudent("Иван Иванов", group.getId());

        // Act
        List<Task> tasks = studentService.getStudentTasks(student.getId());

        // Assert
        assertEquals(3, tasks.size()); // 3 задачи инициализируются автоматически
        assertTrue(tasks.stream().allMatch(t -> t.getStudentId().equals(student.getId())));
    }

    @Test
    @DisplayName("Поиск студентов по имени с использованием Params")
    void findStudentsByName_ExistingName_ReturnsStudents() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        studentService.createStudent("Иван Иванов", group.getId());
        studentService.createStudent("Петр Иванов", group.getId());
        studentService.createStudent("Сергей Петров", group.getId());

        // Act
        List<Student> foundStudents = studentService.findStudentsByName("Иван Иванов");

        // Assert
        assertEquals(1, foundStudents.size());
        assertEquals("Иван Иванов", foundStudents.get(0).getName());
    }

    @Test
    @DisplayName("Получение количества студентов в группе")
    void getStudentsCountByGroup_ExistingGroup_ReturnsCount() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        studentService.createStudent("Иван Иванов", group.getId());
        studentService.createStudent("Петр Петров", group.getId());

        // Act
        int count = studentService.getStudentsCountByGroup(group.getId());

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Проверка существования студента по имени и группе")
    void studentExistsByNameAndGroup_ExistingStudent_ReturnsTrue() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        studentService.createStudent("Иван Иванов", group.getId());

        // Act
        boolean exists = studentService.studentExistsByNameAndGroup("Иван Иванов", group.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("Проверка существования студента по несуществующему имени и группе")
    void studentExistsByNameAndGroup_NonExisting_ReturnsFalse() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);

        // Act
        boolean exists = studentService.studentExistsByNameAndGroup("Несуществующий", group.getId());

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Получение зависимостей сервиса")
    void serviceDependencies_ReturnCorrectInstances() {
        // Act & Assert
        assertNotNull(studentService.studentRepository());
        assertNotNull(studentService.groupService());
        assertNotNull(studentService.taskService());
        assertEquals(studentRepository, studentService.studentRepository());
        assertEquals(groupService, studentService.groupService());
        assertEquals(taskService, studentService.taskService());
    }
}