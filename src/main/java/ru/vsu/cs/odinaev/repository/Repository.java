package ru.vsu.cs.odinaev.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T> {

    void save(T entity);

    Optional<T> findById(UUID id);

    boolean deleteById(UUID id);

    List<T> findAll();

    boolean existsById(UUID id);
}
