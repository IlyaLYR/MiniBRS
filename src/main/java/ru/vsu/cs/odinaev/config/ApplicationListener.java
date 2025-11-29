package ru.vsu.cs.odinaev.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ru.vsu.cs.odinaev.database.DatabaseManager;
import ru.vsu.cs.odinaev.repository.GroupRepository;
import ru.vsu.cs.odinaev.repository.StudentRepository;
import ru.vsu.cs.odinaev.repository.TaskRepository;
import ru.vsu.cs.odinaev.service.GroupService;
import ru.vsu.cs.odinaev.service.StudentService;
import ru.vsu.cs.odinaev.service.TaskService;

@WebListener
public class ApplicationListener implements ServletContextListener {
    private DatabaseManager database;


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.database = DatabaseManager.getInstance();
        GroupRepository groupRepository = new GroupRepository();
        StudentRepository studentRepository = new StudentRepository();
        TaskRepository taskRepository = new TaskRepository();

        TaskService taskService = new TaskService(taskRepository);
        GroupService groupService = new GroupService(groupRepository, null);
        StudentService studentService = new StudentService(studentRepository, groupService, taskService);

        groupService = new GroupService(groupRepository, studentService);

        ServletContext context = sce.getServletContext();
        context.setAttribute("groupService", groupService);
        context.setAttribute("studentService", studentService);
        context.setAttribute("taskService", taskService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        database.close();
    }
}