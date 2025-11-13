package ru.vsu.cs.odinaev.repository;

import ru.vsu.cs.odinaev.database.DatabaseManager;
import ru.vsu.cs.odinaev.model.Task;
import ru.vsu.cs.odinaev.model.TaskStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskRepository implements ITaskRepository {
    private final DatabaseManager dbManager;

    public TaskRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void save(Task task) {
        String sql = "INSERT INTO tasks (id, student_id, number, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getId().toString());
            stmt.setString(2, task.getStudentId().toString());
            stmt.setInt(3, task.getNumber());
            stmt.setString(4, task.getStatus().name());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save task", e);
        }
    }

    public Optional<Task> findById(UUID id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapTask(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find task by id", e);
        }
    }

    public List<Task> findByStudentId(UUID studentId) {
        String sql = "SELECT * FROM tasks WHERE student_id = ? ORDER BY number";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapTask(rs));
            }
            return tasks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tasks by student id", e);
        }
    }

    public List<Task> findByStatus(TaskStatus status) {
        String sql = "SELECT * FROM tasks WHERE status = ? ORDER BY number";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapTask(rs));
            }
            return tasks;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tasks by status", e);
        }
    }

    public void updateStatus(UUID taskId, TaskStatus status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, taskId.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task status", e);
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task", e);
        }
    }

    public Optional<Task> findByStudentIdAndNumber(UUID studentId, int number) {
        String sql = "SELECT * FROM tasks WHERE student_id = ? AND number = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId.toString());
            stmt.setInt(2, number);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTask(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find task by student and number", e);
        }
    }

    public void deleteByStudentId(UUID studentId) {
        String sql = "DELETE FROM tasks WHERE student_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete tasks by student id", e);
        }
    }

    public int countByStudentIdAndStatus(UUID studentId, TaskStatus status) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE student_id = ? AND status = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId.toString());
            stmt.setString(2, status.name());

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count tasks", e);
        }
    }

    private Task mapTask(ResultSet rs) throws SQLException {
        return new Task(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("student_id")),
                rs.getInt("number"),
                TaskStatus.valueOf(rs.getString("status"))
        );
    }
}