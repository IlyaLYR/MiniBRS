package ru.vsu.cs.odinaev;

import ru.vsu.cs.odinaev.controller.CmdController;
import ru.vsu.cs.odinaev.model.Group;
import ru.vsu.cs.odinaev.model.Student;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.repository.LocalRepository;
import ru.vsu.cs.odinaev.service.GroupService;
import ru.vsu.cs.odinaev.service.StudentService;
import ru.vsu.cs.odinaev.service.TaskService;

public class Application {
    private final CmdController controller;

    public Application() {
        // Инициализируем репозитории
        LocalRepository<Group> groupRepository = new LocalRepository<>("data/groups.json", Group.class);
        LocalRepository<Student> studentRepository = new LocalRepository<>("data/students.json", Student.class);
        LocalRepository<Task> taskRepository = new LocalRepository<>("data/tasks.json", Task.class);

        // Инициализируем сервисы
        TaskService taskService = new TaskService(taskRepository);
        GroupService groupService = new GroupService(groupRepository, null);
        StudentService studentService = new StudentService(studentRepository, groupService, taskService);

        // Обновляем GroupService с правильной ссылкой
        groupService = new GroupService(groupRepository, studentService);

        // Создаем контроллер
        this.controller = new CmdController(groupService, studentService, taskService);
    }

    public void run(String[] args) {
        controller.execute(args);
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);
    }
}