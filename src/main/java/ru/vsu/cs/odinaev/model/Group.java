package ru.vsu.cs.odinaev.model;

import java.util.UUID;

/**
 * Класс, представляющий учебную группу в системе.
 * Группа содержит студентов и характеризуется названием и номером курса.
 * Каждая группа имеет уникальный идентификатор.
 */
public class Group {

    /**
     * Уникальный идентификатор группы
     */
    private final UUID id;

    /**
     * Название группы (например, "ИТ-21", "ПИ-31")
     */
    private String name;

    /**
     * Номер курса, на котором обучается группа (от 1 до 6)
     */
    private int courseNumber;

    /**
     * Создает новую учебную группу.
     *
     * @param id           уникальный идентификатор группы
     * @param name         название группы, не должно быть null или пустым
     * @param courseNumber номер курса, должен быть в диапазоне от 1 до 6
     * @throws IllegalArgumentException если name null или пустое,
     *                                  или courseNumber вне допустимого диапазона
     */
    public Group(UUID id, String name, int courseNumber) {
        this.id = id;
        this.name = name;
        this.courseNumber = courseNumber;
    }

    /**
     * Возвращает уникальный идентификатор группы.
     *
     * @return UUID идентификатор группы
     */
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает название группы.
     *
     * @return название группы
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает номер курса группы.
     *
     * @return номер курса (от 1 до 6)
     */
    public int getCourseNumber() {
        return courseNumber;
    }

    /**
     * Устанавливает новое название группы.
     *
     * @param name новое название группы, не должно быть null или пустым
     * @throws IllegalArgumentException если name null или пустое
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Устанавливает новый номер курса для группы.
     *
     * @param courseNumber новый номер курса, должен быть в диапазоне от 1 до 6
     * @throws IllegalArgumentException если courseNumber вне допустимого диапазона
     */
    public void setCourseNumber(int courseNumber) {
        this.courseNumber = courseNumber;
    }

    /**
     * Возвращает строковое представление группы в формате:
     * "Группа{ID: [первые_8_символов_ID]..., Название: '[name]', Курс: [courseNumber]}"
     *
     * @return строковое представление группы
     */
    @Override
    public String toString() {
        return String.format("Группа{ID: %s, Название: '%s', Курс: %d}", id.toString().substring(0, 8) + "...", name, courseNumber);
    }

    /**
     * Сравнивает группу с другим объектом на основе идентификатора.
     *
     * @param obj объект для сравнения
     * @return true если объекты имеют одинаковый идентификатор
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Group group = (Group) obj;
        return id.equals(group.id);
    }

    /**
     * Возвращает хэш-код группы на основе идентификатора.
     *
     * @return хэш-код группы
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}