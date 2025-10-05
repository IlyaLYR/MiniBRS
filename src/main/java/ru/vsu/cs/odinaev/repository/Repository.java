package ru.vsu.cs.odinaev.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T> {

    /**
     * Сохранить сущность
     */
    void save(T entity);

    /**
     * Найти все сущности, удовлетворяющие параметрам
     * Если параметры null - вернуть все сущности
     */
    List<T> find(Params<T> params);

    /**
     * Найти первую сущность, удовлетворяющую параметрам
     */
    Optional<T> findFirst(Params<T> params);

    /**
     * Проверить существование сущности по параметрам
     */
    boolean exists(Params<T> params);

    /**
     * Удалить сущности по параметрам
     * @return количество удаленных сущностей
     */
    int delete(Params<T> params);

    /**
     * Получить класс сущности
     */
    Class<T> getEntityClass();

    // === УДОБНЫЕ DEFAULT МЕТОДЫ ===

    /**
     * Найти все сущности
     */
    default List<T> findAll() {
        return find(null);
    }

    /**
     * Найти сущность по ID
     */
    default Optional<T> findById(UUID id) {
        return findFirst(new Params<>(getEntityClass(), "id", id));
    }

    /**
     * Проверить существование сущности по ID
     */
    default boolean existsById(UUID id) {
        return exists(new Params<>(getEntityClass(), "id", id));
    }

    /**
     * Удалить сущность по ID
     */
    default boolean deleteById(UUID id) {
        int deleted = delete(new Params<>(getEntityClass(), "id", id));
        return deleted > 0;
    }

    /**
     * Удалить все сущности
     */
    default int deleteAll() {
        return delete(null);
    }
}