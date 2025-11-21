package ru.vsu.cs.odinaev.service;

import ru.vsu.cs.odinaev.model.Student;
import ru.vsu.cs.odinaev.model.Task;

import java.util.List;
import java.util.UUID;

public interface IStudentService {
    Student createStudent(String name, UUID groupId);
    void deleteStudent(UUID studentId);
    List<Student> getAllStudents();
    List<Student> getStudentsByGroup(UUID groupId);
    Student getStudentById(UUID studentId);
    Student updateStudent(UUID studentId, String newName, UUID newGroupId);
    List<Task> getStudentTasks(UUID studentId);
    int getStudentsCountByGroup(UUID groupId);
}