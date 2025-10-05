package ru.vsu.cs.odinaev.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Базовый интерфейс репозитория для работы с сущностями.
 * Предоставляет CRUD операции и методы для поиска с использованием параметров.
 *
 * @param <T> тип сущности, с которой работает репозиторий
 */
public interface Repository<T> {

    /**
     * Сохраняет сущность в хранилище.
     * Если сущность с таким ID уже существует, она будет обновлена.
     *
     * @param entity сущность для сохранения
     * @throws IllegalArgumentException если сущность равна null
     */
    void save(T entity);

    /**
     * Находит все сущности, соответствующие заданным параметрам фильтрации.
     * Если параметры не указаны (null), возвращает все сущности.
     *
     * @param params параметры фильтрации, может быть null
     * @return список сущностей, удовлетворяющих параметрам фильтрации
     */
    List<T> find(Params<T> params);

    /**
     * Находит первую сущность, соответствующую заданным параметрам фильтрации.
     *
     * @param params параметры фильтрации, не может быть null
     * @return Optional с найденной сущностью или empty, если ничего не найдено
     * @throws IllegalArgumentException если params равен null
     */
    Optional<T> findFirst(Params<T> params);

    /**
     * Проверяет существование сущностей, соответствующих заданным параметрам фильтрации.
     *
     * @param params параметры фильтрации, не может быть null
     * @return true если есть хотя бы одна сущность, удовлетворяющая параметрам
     * @throws IllegalArgumentException если params равен null
     */
    boolean exists(Params<T> params);

    /**
     * Удаляет сущности, соответствующие заданным параметрам фильтрации.
     *
     * @param params параметры фильтрации для определения удаляемых сущностей
     * @return количество удаленных сущностей
     */
    int delete(Params<T> params);

    /**
     * Возвращает класс сущности, с которой работает репозиторий.
     * Используется для типобезопасной работы с параметрами.
     *
     * @return класс сущности
     */
    Class<T> getEntityClass();

    // === УДОБНЫЕ DEFAULT МЕТОДЫ ===

    /**
     * Находит все сущности в хранилище.
     * Эквивалентно вызову find(null).
     *
     * @return список всех сущностей
     */
    default List<T> findAll() {
        return find(null);
    }

    /**
     * Находит сущность по её идентификатору.
     *
     * @param id идентификатор сущности
     * @return Optional с найденной сущностью или empty, если сущность не найдена
     * @throws IllegalArgumentException если id равен null
     */
    default Optional<T> findById(UUID id) {
        return findFirst(new Params<>(getEntityClass(), "id", id));
    }

    /**
     * Проверяет существование сущности с указанным идентификатором.
     *
     * @param id идентификатор сущности
     * @return true если сущность с таким ID существует
     * @throws IllegalArgumentException если id равен null
     */
    default boolean existsById(UUID id) {
        return exists(new Params<>(getEntityClass(), "id", id));
    }

    /**
     * Удаляет сущность по её идентификатору.
     * Если сущность с таким ID не существует, метод не делает ничего.
     *
     * @param id идентификатор сущности для удаления
     * @throws IllegalArgumentException если id равен null
     */
    default void deleteById(UUID id) {
        int deleted = delete(new Params<>(getEntityClass(), "id", id));
    }

    /**
     * Удаляет все сущности из хранилища.
     * Эквивалентно вызову delete(null).
     *
     */
    default void deleteAll() {
        delete(null);
    }
}