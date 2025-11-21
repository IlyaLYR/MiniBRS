package ru.vsu.cs.odinaev;

import ru.vsu.cs.odinaev.controller.CmdController;
import ru.vsu.cs.odinaev.database.DatabaseManager;
import ru.vsu.cs.odinaev.repository.GroupRepository;
import ru.vsu.cs.odinaev.repository.StudentRepository;
import ru.vsu.cs.odinaev.repository.TaskRepository;
import ru.vsu.cs.odinaev.service.GroupService;
import ru.vsu.cs.odinaev.service.StudentService;
import ru.vsu.cs.odinaev.service.TaskService;

public class Application {
    private final CmdController controller;
    private final DatabaseManager dbManager;

    public Application() {
        // Инициализируем DatabaseManager (создает пул соединений и таблицы)
        this.dbManager = DatabaseManager.getInstance();

        // Инициализируем репозитории БД
        GroupRepository groupRepository = new GroupRepository();
        StudentRepository studentRepository = new StudentRepository();
        TaskRepository taskRepository = new TaskRepository();

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
        try {
            controller.execute(args);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    public static void main(String[] args) {
        Application app = new Application();
        app.run(args);
    }
}