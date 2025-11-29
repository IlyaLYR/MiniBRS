package ru.vsu.cs.odinaev.repository;

import ru.vsu.cs.odinaev.database.DatabaseManager;
import ru.vsu.cs.odinaev.model.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GroupRepository implements IGroupRepository {
    private final DatabaseManager dbManager;

    public GroupRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void save(Group group) {
        String sql = "INSERT INTO groups (id, name, course_number) VALUES (?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, group.getId().toString());
            stmt.setString(2, group.getName());
            stmt.setInt(3, group.getCourse());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save group", e);
        }
    }

    public Optional<Group> findById(UUID id) {
        String sql = "SELECT * FROM groups WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapGroup(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find group by id", e);
        }
    }

    public List<Group> findAll() {
        String sql = "SELECT * FROM groups ORDER BY name";
        List<Group> groups = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                groups.add(mapGroup(rs));
            }
            return groups;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all groups", e);
        }
    }

    public void update(Group group) {
        String sql = "UPDATE groups SET name = ?, course_number = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, group.getName());
            stmt.setInt(2, group.getCourse());
            stmt.setString(3, group.getId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update group", e);
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM groups WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete group", e);
        }
    }

    private Group mapGroup(ResultSet rs) throws SQLException {
        return new Group(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getInt("course_number")
        );
    }

    public boolean existsById(UUID id){
        return findById(id).isPresent();
    }
}