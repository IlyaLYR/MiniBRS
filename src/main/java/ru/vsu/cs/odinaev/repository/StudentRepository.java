package ru.vsu.cs.odinaev.repository;

import ru.vsu.cs.odinaev.database.DatabaseManager;
import ru.vsu.cs.odinaev.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StudentRepository {
    private final DatabaseManager dbManager;

    public StudentRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void save(Student student) {
        String sql = "INSERT INTO students (id, name, group_id) VALUES (?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getId().toString());
            stmt.setString(2, student.getName());
            stmt.setString(3, student.getGroupId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save student", e);
        }
    }

    public Optional<Student> findById(UUID id) {
        String sql = "SELECT * FROM students WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapStudent(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find student by id", e);
        }
    }

    public List<Student> findByGroupId(UUID groupId) {
        String sql = "SELECT * FROM students WHERE group_id = ? ORDER BY name";
        List<Student> students = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, groupId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(mapStudent(rs));
            }
            return students;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find students by group id", e);
        }
    }

    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY name";
        List<Student> students = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapStudent(rs));
            }
            return students;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all students", e);
        }
    }

    public void update(Student student) {
        String sql = "UPDATE students SET name = ?, group_id = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getGroupId().toString());
            stmt.setString(3, student.getId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update student", e);
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete student", e);
        }
    }

    public boolean existsById(UUID studentId){
        return findById(studentId).isPresent();

    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        return new Student(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                UUID.fromString(rs.getString("group_id"))
        );
    }
}