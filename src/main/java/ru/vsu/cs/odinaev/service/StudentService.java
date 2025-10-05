package ru.vsu.cs.odinaev.service;

import ru.vsu.cs.odinaev.model.Student;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.repository.Params;
import ru.vsu.cs.odinaev.repository.Repository;

import java.util.List;
import java.util.UUID;

public record StudentService(Repository<Student> studentRepository, GroupService groupService,
                             TaskService taskService) implements Service {

    public Student createStudent(String name, UUID groupId) {
        validateStudentName(name);

        if (!groupService.groupExists(groupId)) {
            throw new IllegalArgumentException("Группа с ID " + groupId + " не найдена");
        }

        UUID studentId = UUID.randomUUID();
        Student student = new Student(studentId, name.trim(), groupId);

        studentRepository.save(student);
        taskService.initializeStudentTasks(studentId);

        return student;
    }

    public void deleteStudent(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("Студент с ID " + studentId + " не найден");
        }

        taskService.deleteStudentTasks(studentId);
        studentRepository.deleteById(studentId);
        //TODO НУЖНО ЛИ КАСКАДНОЕ УДАЛЕНИЕ ТАСКОВ?????
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> getStudentsByGroup(UUID groupId) {
        if (!groupService.groupExists(groupId)) {
            throw new IllegalArgumentException("Группа с ID " + groupId + " не найдена");
        }

        Params<Student> params = new Params<>(Student.class, "groupId", groupId);
        return studentRepository.find(params);
    }

    public Student getStudentById(UUID studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Студент с ID " + studentId + " не найден"));
    }

    public Student updateStudent(UUID studentId, String newName, UUID newGroupId) {
        Student existingStudent = getStudentById(studentId);

        validateStudentName(newName);
        existingStudent.setName(newName.trim());

        if (newGroupId != null) {
            if (!groupService.groupExists(newGroupId)) {
                throw new IllegalArgumentException("Группа с ID " + newGroupId + " не найдена");
            }
            existingStudent.setGroupId(newGroupId);
        }

        studentRepository.save(existingStudent);
        return existingStudent;
    }

    public List<Task> getStudentTasks(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("Студент с ID " + studentId + " не найден");
        }

        return taskService.getTasksByStudent(studentId);
    }

    private void validateStudentName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя студента не может быть пустым");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Имя студента слишком длинное");
        }
    }

    /**
     * Поиск студентов по имени (использует Repository.find())
     */
    public List<Student> findStudentsByName(String name) {
        Params<Student> params = new Params<>(Student.class, "name", name);
        return studentRepository.find(params);
    }

    /**
     * Получение количества студентов в группе (использует Repository.find())
     */
    public int getStudentsCountByGroup(UUID groupId) {
        Params<Student> params = new Params<>(Student.class, "groupId", groupId);
        return studentRepository.find(params).size();
    }

    /**
     * Проверка существования студента по имени и группе (использует Repository.exists())
     */
    public boolean studentExistsByNameAndGroup(String name, UUID groupId) {
        java.util.Map<String, Object> filterParams = new java.util.HashMap<>();
        filterParams.put("name", name);
        filterParams.put("groupId", groupId);

        Params<Student> params = new Params<>(Student.class, filterParams);
        return studentRepository.exists(params);
    }
}