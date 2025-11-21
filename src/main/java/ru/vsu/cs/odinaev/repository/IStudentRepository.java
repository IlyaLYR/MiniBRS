package ru.vsu.cs.odinaev.repository;

import ru.vsu.cs.odinaev.model.Student;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IStudentRepository {
    void save(Student student);
    Optional<Student> findById(UUID id);
    List<Student> findByGroupId(UUID groupId);
    List<Student> findAll();
    void update(Student student);
    void delete(UUID id);
    boolean existsById(UUID studentId);
}