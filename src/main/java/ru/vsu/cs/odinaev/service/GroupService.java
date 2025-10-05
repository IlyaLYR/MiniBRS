package ru.vsu.cs.odinaev.service;

import ru.vsu.cs.odinaev.model.Group;
import ru.vsu.cs.odinaev.model.Student;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.repository.Params;
import ru.vsu.cs.odinaev.repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record GroupService(Repository<Group> groupRepository, StudentService studentService) implements Service {

    public Group createGroup(String name, int courseNumber) {
        validateGroupName(name);
        validateCourseNumber(courseNumber);

        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, name, courseNumber);

        groupRepository.save(group);
        return group;
    }

    public void deleteGroup(UUID groupId) {
        if (!groupExists(groupId)) {
            throw new IllegalArgumentException("Группа с ID " + groupId + " не найдена");
        }


        List<Student> studentsInGroup = studentService.getStudentsByGroup(groupId);
        for (Student student : studentsInGroup) {
            studentService.deleteStudent(student.getId());
        }

        groupRepository.deleteById(groupId);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Группа с ID " + groupId + " не найдена"));
    }

    public Group updateGroup(UUID groupId, String newName, Integer newCourseNumber) {
        Group existingGroup = getGroupById(groupId);

        validateGroupName(newName);
        validateCourseNumber(newCourseNumber);

        existingGroup.setName(newName);
        existingGroup.setCourseNumber(newCourseNumber);

        groupRepository.save(existingGroup);
        return existingGroup;
    }

    public boolean groupExists(UUID groupId) {
        return groupRepository.existsById(groupId);
    }

    private void validateGroupName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название группы не может быть пустым");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Название группы слишком длинное");
        }
    }

    private void validateCourseNumber(int courseNumber) {
        if (courseNumber < 1 || courseNumber > 6) {
            throw new IllegalArgumentException("Номер курса должен быть от 1 до 6");
        }
    }

    public List<Object[]> getGroupReport(UUID groupId) {
        if (!groupExists(groupId)) {
            throw new IllegalArgumentException("Группа с ID " + groupId + " не найдена");
        }

        // Получаем студентов группы с использованием Params
        List<Student> students = studentService.getStudentsByGroup(groupId);
        List<Object[]> report = new ArrayList<>();

        for (Student student : students) {
            // Получаем задачи студента с использованием Params
            List<Task> tasks = studentService.getStudentTasks(student.getId());
            Object[] studentWithTasks = new Object[]{student, tasks};
            report.add(studentWithTasks);
        }

        return report;
    }

    /**
     * Поиск групп по названию (использует Params)
     */
    public List<Group> findGroupsByName(String name) {
        Params<Group> params = new Params<>(Group.class, "name", name);
        return groupRepository.find(params);
    }

    /**
     * Поиск групп по номеру курса (использует Params)
     */
    public List<Group> findGroupsByCourseNumber(int courseNumber) {
        Params<Group> params = new Params<>(Group.class, "courseNumber", courseNumber);
        return groupRepository.find(params);
    }
}