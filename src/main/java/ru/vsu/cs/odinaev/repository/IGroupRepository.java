package ru.vsu.cs.odinaev.repository;

import ru.vsu.cs.odinaev.model.Group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IGroupRepository {
    void save(Group group);
    Optional<Group> findById(UUID id);
    List<Group> findAll();
    void update(Group group);
    void delete(UUID id);
    boolean existsById(UUID id);
}