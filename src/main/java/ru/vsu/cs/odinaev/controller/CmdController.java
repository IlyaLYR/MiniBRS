package ru.vsu.cs.odinaev.controller;

import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.InfoCmp;
import ru.vsu.cs.odinaev.model.*;
import ru.vsu.cs.odinaev.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Контроллер командной строки с интерактивным режимом на основе JLine.
 * Предоставляет автодополнение, историю команд и подсветку.
 */
public class CmdController implements Controller {
    private final GroupService groupService;
    private final StudentService studentService;
    private final TaskService taskService;
    private final LineReader reader;
    private final Terminal terminal;

    /**
     * Создает контроллер с поддержкой интерактивного режима.
     */
    public CmdController(GroupService groupService, StudentService studentService,
                         TaskService taskService) {
        this.groupService = groupService;
        this.studentService = studentService;
        this.taskService = taskService;

        try {
            this.terminal = TerminalBuilder.builder()
                    .system(true)
                    .dumb(true)
                    .build();

            // Настройка автодополнения
            Completer completer = new StringsCompleter(
                    "create-group", "cg",
                    "list-groups", "lg",
                    "delete-group", "dg",
                    "report-group", "rg",
                    "update-group", "ug",
                    "create-student", "cs",
                    "list-students", "ls",
                    "delete-student", "ds",
                    "update-student", "us",
                    "mark-task", "mt",
                    "list-tasks", "lt",
                    "reset-task", "rt",
                    "help", "exit", "clear"
            );

            this.reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .parser(new DefaultParser())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize terminal", e);
        }
    }

    /**
     * Запускает интерактивный режим работы с системой.
     */
    public void execute(String[] args) {
        printWelcomeBanner();

        while (true) {
            try {
                String line = reader.readLine("minibrs> ");

                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                if ("exit".equalsIgnoreCase(line.trim()) || "quit".equalsIgnoreCase(line.trim())) {
                    terminal.writer().println("До свидания!");
                    break;
                }

                processCommand(line.trim());

            } catch (UserInterruptException e) {
                // Ctrl+C
                terminal.writer().println("\nДо свидания!");
                break;
            } catch (EndOfFileException e) {
                // Ctrl+D
                terminal.writer().println("\nДо свидания!");
                break;
            } catch (Exception e) {
                terminal.writer().println("ОШИБКА: " + e.getMessage());
            }
        }
    }

    /**
     * Обрабатывает введенную команду с поддержкой флагов.
     */
    /**
     * Обрабатывает введенную команду с поддержкой флагов.
     */
    private void processCommand(String commandLine) {
        String[] args = parseCommandLine(commandLine);

        if (args.length == 0) {
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "create-group", "cg" -> handleCreateGroup(extractArgs(args));
            case "list-groups", "lg" -> handleListGroups();
            case "delete-group", "dg" -> handleDeleteGroup(extractArgs(args));
            case "report-group", "rg" -> handleGroupReport(extractArgs(args));
            case "update-group", "ug" -> handleUpdateGroup(extractArgs(args));

            case "create-student", "cs" -> handleCreateStudent(extractArgs(args));
            case "list-students", "ls" -> handleListStudents(extractArgs(args));
            case "delete-student", "ds" -> handleDeleteStudent(extractArgs(args));
            case "update-student", "us" -> handleUpdateStudent(extractArgs(args));

            case "mark-task", "mt" -> handleMarkTask(extractArgs(args));
            case "list-tasks", "lt" -> handleListTasks(extractArgs(args));
            case "reset-task", "rt" -> handleResetTask(extractArgs(args));

            case "help" -> printHelp();
            case "clear" -> clearScreen();

            default -> terminal.writer().println("НЕИЗВЕСТНАЯ КОМАНДА: " + command + ". Введите 'help' для справки.");
        }
    }

    /**
     * Извлекает аргументы из массива (все кроме первого элемента).
     */
    private String[] extractArgs(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] result = new String[args.length - 1];
        System.arraycopy(args, 1, result, 0, result.length);
        return result;
    }

    private void printWelcomeBanner() {
        terminal.writer().println("""
                \n
                MiniBRS - Student Task Management System
                ===========================================
                Интерактивный режим с автодополнением
                Введите 'help' для списка команд
                """);
    }

    private void printHelp() {
        terminal.writer().println("""
                \nКОМАНДЫ MINIBRS:
                
                ГРУППЫ:
                  create-group, cg  <name> <course>    - Создать группу
                  list-groups, lg                      - Показать все группы
                  report-group, rg  <groupId>          - Отчет по группу
                  delete-group, dg  <groupId>          - Удалить группу
                  update-group, ug  <id> <name> <course> - Обновить группу
                
                СТУДЕНТЫ:
                  create-student, cs <name> <groupId>  - Создать студента
                  list-students, ls  <groupId>         - Список студентов группы
                  delete-student, ds <studentId>       - Удалить студента
                  update-student, us <id> <name> <groupId> - Обновить студента
                
                ЗАДАЧИ:
                  list-tasks, lt    <studentId>        - Список задач студента
                  mark-task, mt     <studentId> <number> - Отметить задачу сданной
                  reset-task, rt    <studentId> <number> - Сбросить статус задачи
                
                СИСТЕМА:
                  help                                 - Показать справку
                  clear                                - Очистить экран
                  exit, quit                           - Выйти из программы
                """);
    }

    private void clearScreen() {
        try {
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
        } catch (Exception e) {
            // Fallback
            for (int i = 0; i < 50; i++) {
                terminal.writer().println();
            }
        }
    }

    // === ОБРАБОТЧИКИ КОМАНД ===

    private void handleCreateGroup(String[] args) {
        if (args.length != 2) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: create-group <название> <курс>");
            return;
        }

        try {
            String name = args[0];
            int course = Integer.parseInt(args[1]);

            Group group = groupService.createGroup(name, course);
            terminal.writer().println("OK: Создана группа: " + group.getName() + " (ID: " + group.getId() + ")");

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА создания группы: " + e.getMessage());
        }
    }

    private void handleListGroups() {
        try {
            List<Group> groups = groupService.getAllGroups();
            if (groups.isEmpty()) {
                terminal.writer().println("Группы не найдены");
                return;
            }

            terminal.writer().println("СПИСОК ГРУПП:");
            for (Group group : groups) {
                int studentCount = studentService.getStudentsCountByGroup(group.getId());
                terminal.writer().printf("- %s (Курс %d) - %d студентов [ID: %s]%n",
                        group.getName(), group.getCourseNumber(), studentCount, group.getId());
            }

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА получения списка групп: " + e.getMessage());
        }
    }

    private void handleDeleteGroup(String[] args) {
        if (args.length != 1) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: delete-group <groupId>");
            return;
        }

        try {
            UUID id = UUID.fromString(args[0]);
            Group group = groupService.getGroupById(id);
            groupService.deleteGroup(id);
            terminal.writer().println("OK: Удалена группа: " + group.getName());

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА удаления группы: " + e.getMessage());
        }
    }

    private void handleGroupReport(String[] args) {
        if (args.length != 1) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: report-group <groupId>");
            return;
        }

        try {
            UUID id = UUID.fromString(args[0]);
            List<Object[]> groupReport = groupService.getGroupReport(id);

            if (groupReport.isEmpty()) {
                terminal.writer().println("Группа не найдена или в ней нет студентов");
                return;
            }

            Group group = groupService.getGroupById(id);

            terminal.writer().println("ОТЧЕТ ПО ГРУППЕ: " + group.getName());
            terminal.writer().println("Курс: " + group.getCourseNumber());
            terminal.writer().println("Количество студентов: " + groupReport.size());
            terminal.writer().println();

            int totalCompleted = 0;
            int totalTasks = groupReport.size() * 3; // Предполагаем, что у каждого студента должно быть 3 задачи

            for (Object[] studentWithTasks : groupReport) {
                Student student = (Student) studentWithTasks[0];
                List<Task> tasks = (List<Task>) studentWithTasks[1];

                int completed = (int) tasks.stream()
                        .filter(Task::isSubmitted)
                        .count();

                totalCompleted += completed;

                String progress = String.format("%d/%d", completed, 3);
                String status = completed == 3 ? "[ВСЕ СДАНО]" :
                        completed == 0 ? "[НЕ СДАНО]" : "[ЧАСТИЧНО]";

                terminal.writer().printf("- %s: %s задач сдано %s%n",
                        student.getName(), progress, status);
            }

            terminal.writer().println();
            terminal.writer().printf("ОБЩАЯ СТАТИСТИКА:%n");
            terminal.writer().printf("Всего сдано задач: %d/%d (%.1f%%)%n",
                    totalCompleted, totalTasks, (totalCompleted * 100.0 / totalTasks));

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА генерации отчета: " + e.getMessage());
        }
    }

    private void handleUpdateGroup(String[] args) {
        if (args.length != 3) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: update-group <groupId> <новое_название> <новый_курс>");
            return;
        }

        try {
            UUID groupId = UUID.fromString(args[0]);
            String newName = args[1];
            int newCourse = Integer.parseInt(args[2]);

            Group updatedGroup = groupService.updateGroup(groupId, newName, newCourse);
            terminal.writer().println("OK: Группа обновлена: " + updatedGroup.getName() + " (Курс: " + updatedGroup.getCourseNumber() + ")");

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА обновления группы: " + e.getMessage());
        }
    }

    private void handleCreateStudent(String[] args) {
        if (args.length != 2) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: create-student <имя> <groupId>");
            return;
        }

        try {
            String name = args[0];
            UUID groupId = UUID.fromString(args[1]);

            Student student = studentService.createStudent(name, groupId);
            terminal.writer().println("OK: Создан студент: " + student.getName() + " (ID: " + student.getId() + ")");

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА создания студента: " + e.getMessage());
        }
    }

    private void handleDeleteStudent(String[] args) {
        if (args.length != 1) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: delete-student <studentId>");
            return;
        }

        try {
            UUID id = UUID.fromString(args[0]);
            Student student = studentService.getStudentById(id);
            studentService.deleteStudent(id);
            terminal.writer().println("OK: Удален студент: " + student.getName());

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА удаления студента: " + e.getMessage());
        }
    }

    private void handleListStudents(String[] args) {
        if (args.length != 1) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: list-students <groupId>");
            return;
        }

        try {
            UUID id = UUID.fromString(args[0]);
            List<Student> students = studentService.getStudentsByGroup(id);

            if (students.isEmpty()) {
                terminal.writer().println("В группе нет студентов");
                return;
            }

            Group group = groupService.getGroupById(id);
            terminal.writer().println("СТУДЕНТЫ ГРУППЫ " + group.getName() + ":");
            for (Student student : students) {
                int completed = taskService.getCompletedTasksCount(student.getId());
                terminal.writer().printf("- %s [Сдано: %d/3] [ID: %s]%n",
                        student.getName(), completed, student.getId());
            }

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА получения списка студентов: " + e.getMessage());
        }
    }

    private void handleUpdateStudent(String[] args) {
        if (args.length != 3) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: update-student <studentId> <новое_имя> <groupId>");
            return;
        }

        try {
            UUID studentId = UUID.fromString(args[0]);
            String newName = args[1];
            UUID newGroupId = UUID.fromString(args[2]);

            Student updatedStudent = studentService.updateStudent(studentId, newName, newGroupId);
            terminal.writer().println("OK: Студент обновлен: " + updatedStudent.getName());

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА обновления студента: " + e.getMessage());
        }
    }

    private void handleMarkTask(String[] args) {
        if (args.length != 2) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: mark-task <studentId> <номер_задачи>");
            return;
        }

        try {
            UUID studentId = UUID.fromString(args[0]);
            int taskNumber = Integer.parseInt(args[1]);

            if (taskNumber < 1 || taskNumber > 3) {
                terminal.writer().println("ОШИБКА: Номер задачи должен быть от 1 до 3");
                return;
            }

            Task task = taskService.getTaskByStudentAndNumber(studentId, taskNumber);
            taskService.updateTaskStatus(task.getId(), TaskStatus.SUBMITTED);

            Student student = studentService.getStudentById(studentId);
            terminal.writer().println("OK: Задача " + taskNumber + " отмечена как сданная для студента: " + student.getName());

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА отметки задачи: " + e.getMessage());
        }
    }

    private void handleListTasks(String[] args) {
        if (args.length != 1) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: list-tasks <studentId>");
            return;
        }

        try {
            UUID id = UUID.fromString(args[0]);
            Student student = studentService.getStudentById(id);
            List<Task> tasks = taskService.getTasksByStudent(id);

            terminal.writer().println("ЗАДАЧИ СТУДЕНТА " + student.getName() + ":");
            for (Task task : tasks) {
                String status = task.getStatus() == TaskStatus.SUBMITTED ? "СДАНО" : "НЕ СДАНО";
                terminal.writer().printf("- Задача %d: %s%n", task.getNumber(), status);
            }

            int completed = taskService.getCompletedTasksCount(id);
            terminal.writer().printf("ИТОГО: %d/3 задач сдано%n", completed);

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА получения списка задач: " + e.getMessage());
        }
    }

    private void handleResetTask(String[] args) {
        if (args.length != 2) {
            terminal.writer().println("ИСПОЛЬЗОВАНИЕ: reset-task <studentId> <номер_задачи>");
            return;
        }

        try {
            UUID studentId = UUID.fromString(args[0]);
            int taskNumber = Integer.parseInt(args[1]);

            if (taskNumber < 1 || taskNumber > 3) {
                terminal.writer().println("ОШИБКА: Номер задачи должен быть от 1 до 3");
                return;
            }

            Task task = taskService.getTaskByStudentAndNumber(studentId, taskNumber);
            taskService.updateTaskStatus(task.getId(), TaskStatus.NOT_SUBMITTED);

            Student student = studentService.getStudentById(studentId);
            terminal.writer().println("OK: Статус задачи " + taskNumber + " сброшен для студента: " + student.getName());

        } catch (Exception e) {
            terminal.writer().println("ОШИБКА сброса задачи: " + e.getMessage());
        }
    }

    /**
     * Парсит командную строку, учитывая кавычки.
     * Пример: create-group "ИТ-21" 2 -> ["create-group", "ИТ-21", "2"]
     */
    private String[] parseCommandLine(String commandLine) {
        List<String> args;
        args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < commandLine.length(); i++) {
            char c = commandLine.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                // Конец аргумента
                if (!currentArg.isEmpty()) {
                    args.add(currentArg.toString());
                    currentArg.setLength(0);
                }
            } else {
                currentArg.append(c);
            }
        }

        // Добавляем последний аргумент
        if (!currentArg.isEmpty()) {
            args.add(currentArg.toString());
        }

        return args.toArray(new String[0]);
    }
}