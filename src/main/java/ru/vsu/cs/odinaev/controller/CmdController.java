package ru.vsu.cs.odinaev.controller;

import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ru.vsu.cs.odinaev.model.Group;
import ru.vsu.cs.odinaev.model.Student;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.model.TaskStatus;
import ru.vsu.cs.odinaev.service.GroupService;
import ru.vsu.cs.odinaev.service.StudentService;
import ru.vsu.cs.odinaev.service.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MiniBRS - CLI на основе JLine + Picocli
 * Поддерживает автодополнение, help и обработку кавычек.
 */
@Command(name = "", description = {"MiniBRS — система управления учебными заданиями студентов.", "", "Использование:", "  minibrs <команда> [аргументы]", "", "Для справки по конкретной команде:", "  minibrs help <команда>"}, mixinStandardHelpOptions = true)
public class CmdController implements Controller {

    private final GroupService groupService;
    private final StudentService studentService;
    private final TaskService taskService;
    private final LineReader reader;
    private final Terminal terminal;
    private final CommandLine picocli;

    public CmdController(GroupService groupService, StudentService studentService, TaskService taskService) {
        this.groupService = groupService;
        this.studentService = studentService;
        this.taskService = taskService;

        try {
            this.terminal = TerminalBuilder.builder().system(true).dumb(true).build();

            // автодополнение базовых команд
            Completer completer = new StringsCompleter("create-group", "cg", "list-groups", "lg", "delete-group", "dg", "report-group", "rg", "update-group", "ug", "create-student", "cs", "list-students", "ls", "delete-student", "ds", "update-student", "us", "mark-task", "mt", "list-tasks", "lt", "reset-task", "rt", "help", "exit", "clear");

            this.reader = LineReaderBuilder.builder().terminal(terminal).completer(completer).parser(new DefaultParser()).build();

            this.picocli = new CommandLine(this);
            picocli.setUsageHelpAutoWidth(true);
            picocli.setUsageHelpLongOptionsMaxWidth(30);
            picocli.setUsageHelpWidth(100);
            picocli.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));

            // регистрация команд
            registerSubcommands();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации терминала", e);
        }
    }

    private void registerSubcommands() {
        picocli.addSubcommand("create-group", new CreateGroupCmd());
        picocli.addSubcommand("cg", new CreateGroupCmd());

        picocli.addSubcommand("list-groups", new ListGroupsCmd());
        picocli.addSubcommand("lg", new ListGroupsCmd());

        picocli.addSubcommand("delete-group", new DeleteGroupCmd());
        picocli.addSubcommand("dg", new DeleteGroupCmd());

        picocli.addSubcommand("report-group", new ReportGroupCmd());
        picocli.addSubcommand("rg", new ReportGroupCmd());

        picocli.addSubcommand("update-group", new UpdateGroupCmd());
        picocli.addSubcommand("ug", new UpdateGroupCmd());

        picocli.addSubcommand("create-student", new CreateStudentCmd());
        picocli.addSubcommand("cs", new CreateStudentCmd());

        picocli.addSubcommand("list-students", new ListStudentsCmd());
        picocli.addSubcommand("ls", new ListStudentsCmd());

        picocli.addSubcommand("delete-student", new DeleteStudentCmd());
        picocli.addSubcommand("ds", new DeleteStudentCmd());

        picocli.addSubcommand("update-student", new UpdateStudentCmd());
        picocli.addSubcommand("us", new UpdateStudentCmd());

        picocli.addSubcommand("mark-task", new MarkTaskCmd());
        picocli.addSubcommand("mt", new MarkTaskCmd());

        picocli.addSubcommand("list-tasks", new ListTasksCmd());
        picocli.addSubcommand("lt", new ListTasksCmd());

        picocli.addSubcommand("reset-task", new ResetTaskCmd());
        picocli.addSubcommand("rt", new ResetTaskCmd());

        // системные
        picocli.addSubcommand("clear", new ClearCmd());
        picocli.addSubcommand("exit", new ExitCmd());
        picocli.addSubcommand("help", new CommandLine.HelpCommand());
    }

    /**
     * Главный интерактивный цикл
     */
    public void execute(String[] args) {
        printBanner();

        while (true) {
            try {
                String line = reader.readLine("minibrs> ");
                if (line == null || line.trim().isEmpty()) continue;

                String trimmed = line.trim();
                if (trimmed.equalsIgnoreCase("exit") || trimmed.equalsIgnoreCase("quit")) {
                    terminal.writer().println("До свидания!");
                    break;
                }

                if (trimmed.equalsIgnoreCase("clear")) {
                    clearScreen();
                    continue;
                }

                try {
                    String[] argv = parseCommandLine(trimmed);
                    if (argv.length > 0) {
                        picocli.execute(argv);
                    }
                } catch (Exception inner) {
                    terminal.writer().println("Ошибка: " + inner.getMessage());
                }

            } catch (UserInterruptException | EndOfFileException e) {
                terminal.writer().println("\nДо свидания!");
                break;
            }
        }
    }

    private void printBanner() {
        terminal.writer().println("""
                
                ==========================================
                   MiniBRS — CLI система управления задачами
                ==========================================
                Подсказки:
                  help               — показать список команд
                  help <команда>     — справка по команде
                  exit               — выйти
                  clear              — очистить экран
                """);
    }

    private void clearScreen() {
        try {
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) terminal.writer().println();
        }
    }

    // ===========================
    // Подкоманды (улучшенный Help)
    // ===========================

    @Command(name = "create-group", description = "Создать новую учебную группу.", mixinStandardHelpOptions = true, headerHeading = "%nОписание:%n", synopsisHeading = "%nИспользование:%n", descriptionHeading = "%nПодробности:%n", parameterListHeading = "%nПараметры:%n", optionListHeading = "%nОпции:%n", commandListHeading = "%nКоманды:%n", footerHeading = "%nПример:%n%n  create-group \"Программная инженерия\" 3%n")
    class CreateGroupCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<name>", description = "Название группы.")
        String name;
        @Parameters(index = "1", paramLabel = "<course>", description = "Номер курса (1–4).")
        String course;

        @Override
        public void run() {
            handleCreateGroup(new String[]{name, course});
        }
    }

    @Command(name = "list-groups", description = "Показать все учебные группы.", mixinStandardHelpOptions = true)
    class ListGroupsCmd implements Runnable {
        @Override
        public void run() {
            handleListGroups();
        }
    }

    @Command(name = "delete-group", description = "Удалить группу по UUID.", mixinStandardHelpOptions = true, footer = "%nПример:%n  delete-group 123e4567-e89b-12d3-a456-426614174000%n")
    class DeleteGroupCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<groupId>", description = "UUID группы.")
        String id;

        @Override
        public void run() {
            handleDeleteGroup(new String[]{id});
        }
    }

    @Command(name = "update-group", description = "Обновить название и курс группы.", mixinStandardHelpOptions = true, footer = "%nПример:%n  update-group <uuid> \"Новая группа\" 2%n")
    class UpdateGroupCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<groupId>", description = "UUID группы.")
        String id;
        @Parameters(index = "1", paramLabel = "<name>", description = "Новое название.")
        String newName;
        @Parameters(index = "2", paramLabel = "<course>", description = "Новый курс.")
        String newCourse;

        @Override
        public void run() {
            handleUpdateGroup(new String[]{id, newName, newCourse});
        }
    }

    @Command(name = "report-group", description = "Показать отчёт по группе и прогресс студентов.", mixinStandardHelpOptions = true, footer = "%nПример:%n  report-group <uuid>%n")
    class ReportGroupCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<groupId>", description = "UUID группы.")
        String id;

        @Override
        public void run() {
            handleGroupReport(new String[]{id});
        }
    }

    @Command(name = "create-student", description = "Создать нового студента в группе.", mixinStandardHelpOptions = true, footer = "%nПример:%n  create-student \"Иван Петров\" <groupUuid>%n")
    class CreateStudentCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<name>", description = "Имя студента.")
        String name;
        @Parameters(index = "1", paramLabel = "<groupId>", description = "UUID группы.")
        String groupId;

        @Override
        public void run() {
            handleCreateStudent(new String[]{name, groupId});
        }
    }

    @Command(name = "list-students", description = "Показать всех студентов группы.", mixinStandardHelpOptions = true)
    class ListStudentsCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<groupId>", description = "UUID группы.")
        String groupId;

        @Override
        public void run() {
            handleListStudents(new String[]{groupId});
        }
    }

    @Command(name = "delete-student", description = "Удалить студента по UUID.", mixinStandardHelpOptions = true)
    class DeleteStudentCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<studentId>", description = "UUID студента.")
        String studentId;

        @Override
        public void run() {
            handleDeleteStudent(new String[]{studentId});
        }
    }

    @Command(name = "update-student", description = "Обновить данные студента.", mixinStandardHelpOptions = true, footer = "%nПример:%n  update-student <uuid> \"Новое имя\" <groupUuid>%n")
    class UpdateStudentCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<studentId>", description = "UUID студента.")
        String id;
        @Parameters(index = "1", paramLabel = "<name>", description = "Новое имя.")
        String name;
        @Parameters(index = "2", paramLabel = "<groupId>", description = "UUID новой группы.")
        String groupId;

        @Override
        public void run() {
            handleUpdateStudent(new String[]{id, name, groupId});
        }
    }

    @Command(name = "mark-task", description = "Отметить задачу как сданную.", mixinStandardHelpOptions = true)
    class MarkTaskCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<studentId>", description = "UUID студента.")
        String studentId;
        @Parameters(index = "1", paramLabel = "<taskNumber>", description = "Номер задачи (1–3).")
        String number;

        @Override
        public void run() {
            handleMarkTask(new String[]{studentId, number});
        }
    }

    @Command(name = "list-tasks", description = "Показать список задач студента.", mixinStandardHelpOptions = true)
    class ListTasksCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<studentId>", description = "UUID студента.")
        String studentId;

        @Override
        public void run() {
            handleListTasks(new String[]{studentId});
        }
    }

    @Command(name = "reset-task", description = "Сбросить статус задачи.", mixinStandardHelpOptions = true)
    class ResetTaskCmd implements Runnable {
        @Parameters(index = "0", paramLabel = "<studentId>", description = "UUID студента.")
        String studentId;
        @Parameters(index = "1", paramLabel = "<taskNumber>", description = "Номер задачи (1–3).")
        String number;

        @Override
        public void run() {
            handleResetTask(new String[]{studentId, number});
        }
    }

    @Command(name = "clear", description = "Очистить экран.", mixinStandardHelpOptions = true)
    class ClearCmd implements Runnable {
        @Override
        public void run() {
            clearScreen();
        }
    }

    @Command(name = "exit", description = "Выйти из программы.", mixinStandardHelpOptions = true)
    class ExitCmd implements Runnable {
        @Override
        public void run() {
            terminal.writer().println("До свидания!");
        }
    }

    // ===========================
    // Утилиты парсинга
    // ===========================

    private String[] parseCommandLine(String input) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (!current.isEmpty()) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) args.add(current.toString());
        return args.toArray(new String[0]);
    }

    // ====== Далее остаются handleXXX методы (без изменений) ======
    // [Ты можешь оставить свои обработчики, они совместимы с этим контроллером]

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
                terminal.writer().printf("- %s (Курс %d) - %d студентов [ID: %s]%n", group.getName(), group.getCourseNumber(), studentCount, group.getId());
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

                int completed = (int) tasks.stream().filter(Task::isSubmitted).count();

                totalCompleted += completed;

                String progress = String.format("%d/%d", completed, 3);
                String status = completed == 3 ? "[ВСЕ СДАНО]" : completed == 0 ? "[НЕ СДАНО]" : "[ЧАСТИЧНО]";

                terminal.writer().printf("- %s: %s задач сдано %s%n", student.getName(), progress, status);
            }

            terminal.writer().println();
            terminal.writer().printf("ОБЩАЯ СТАТИСТИКА:%n");
            terminal.writer().printf("Всего сдано задач: %d/%d (%.1f%%)%n", totalCompleted, totalTasks, (totalCompleted * 100.0 / totalTasks));

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
                terminal.writer().printf("- %s [Сдано: %d/3] [ID: %s]%n", student.getName(), completed, student.getId());
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
}
