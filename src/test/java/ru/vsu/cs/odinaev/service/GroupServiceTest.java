package ru.vsu.cs.odinaev.service;

import org.junit.jupiter.api.*;
import ru.vsu.cs.odinaev.model.Group;
import ru.vsu.cs.odinaev.model.Student;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.repository.LocalRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupServiceTest {

    private LocalRepository<Group> groupRepository;
    private LocalRepository<Student> studentRepository;
    private LocalRepository<Task> taskRepository;
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        // Инициализируем репозитории
        groupRepository = new LocalRepository<>("src/test/resources/test_groups.json", Group.class);
        studentRepository = new LocalRepository<>("src/test/resources/test_students.json", Student.class);
        taskRepository = new LocalRepository<>("src/test/resources/test_tasks.json", Task.class);

        // Инициализируем сервисы в правильном порядке
        TaskService taskService = new TaskService(taskRepository);
        groupService = new GroupService(groupRepository, null); // Временно без StudentService
        StudentService studentService = new StudentService(studentRepository, groupService, taskService);

        // Теперь обновляем GroupService с правильной ссылкой на StudentService
        groupService = new GroupService(groupRepository, studentService);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        studentRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    @DisplayName("Удаление существующей группы")
    void deleteGroup_ExistingGroup_Success() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);
        UUID groupId = group.getId();

        // Act
        groupService.deleteGroup(groupId);

        // Assert
        assertFalse(groupService.groupExists(groupId));
    }

    @Test
    @DisplayName("Создание группы с валидными данными")
    void createGroup_ValidData_Success() {
        // Act
        Group group = groupService.createGroup("ИТ-21", 2);

        // Assert
        assertNotNull(group);
        assertNotNull(group.getId());
        assertEquals("ИТ-21", group.getName());
        assertEquals(2, group.getCourseNumber());
        assertTrue(groupService.groupExists(group.getId()));
    }

    @Test
    @DisplayName("Получение всех групп")
    void getAllGroups_WithGroups_ReturnsAllGroups() {
        // Arrange
        groupService.createGroup("ИТ-21", 2);
        groupService.createGroup("ПИ-31", 3);

        // Act
        List<Group> groups = groupService.getAllGroups();

        // Assert
        assertEquals(2, groups.size());
    }

    @Test
    @DisplayName("Получение группы по ID")
    void getGroupById_ExistingGroup_ReturnsGroup() {
        // Arrange
        Group expectedGroup = groupService.createGroup("ИТ-21", 2);
        UUID groupId = expectedGroup.getId();

        // Act
        Group actualGroup = groupService.getGroupById(groupId);

        // Assert
        assertEquals(expectedGroup.getId(), actualGroup.getId());
        assertEquals("ИТ-21", actualGroup.getName());
    }

    @Test
    @DisplayName("Обновление группы")
    void updateGroup_ValidData_Success() {
        // Arrange
        Group originalGroup = groupService.createGroup("Старое название", 1);
        UUID groupId = originalGroup.getId();

        // Act
        Group updatedGroup = groupService.updateGroup(groupId, "Новое название", 2);

        // Assert
        assertEquals("Новое название", updatedGroup.getName());
        assertEquals(2, updatedGroup.getCourseNumber());
    }

    @Test
    @DisplayName("Поиск групп по названию")
    void findGroupsByName_ExistingName_ReturnsGroups() {
        // Arrange
        groupService.createGroup("ИТ-21", 2);
        groupService.createGroup("ПИ-31", 3);

        // Act
        List<Group> foundGroups = groupService.findGroupsByName("ИТ-21");

        // Assert
        assertEquals(1, foundGroups.size());
        assertEquals("ИТ-21", foundGroups.get(0).getName());
    }

    @Test
    @DisplayName("Поиск групп по номеру курса")
    void findGroupsByCourseNumber_ExistingCourse_ReturnsGroups() {
        // Arrange
        groupService.createGroup("ИТ-21", 2);
        groupService.createGroup("ПИ-22", 2);
        groupService.createGroup("ИТ-31", 3);

        // Act
        List<Group> foundGroups = groupService.findGroupsByCourseNumber(2);

        // Assert
        assertEquals(2, foundGroups.size());
        assertTrue(foundGroups.stream().allMatch(g -> g.getCourseNumber() == 2));
    }

    @Test
    @DisplayName("Проверка существования группы")
    void groupExists_ExistingGroup_ReturnsTrue() {
        // Arrange
        Group group = groupService.createGroup("ИТ-21", 2);

        // Act & Assert
        assertTrue(groupService.groupExists(group.getId()));
    }

    @Test
    @DisplayName("Проверка существования несуществующей группы")
    void groupExists_NonExistingGroup_ReturnsFalse() {
        // Act & Assert
        assertFalse(groupService.groupExists(UUID.randomUUID()));
    }
}