package ru.vsu.cs.odinaev.model;

import java.util.UUID;

/**
 * Класс, представляющий студента в системе.
 * Студент принадлежит к определенной учебной группе и имеет задачи для выполнения.
 * Каждый студент имеет уникальный идентификатор и привязан к группе через groupId.
 */
public class Student {

    /**
     * Уникальный идентификатор студента
     */
    private final UUID id;

    /**
     * Имя студента
     */
    private String name;

    /**
     * Идентификатор группы, к которой принадлежит студент
     */
    private UUID groupId;

    /**
     * Создает нового студента.
     *
     * @param id      уникальный идентификатор студента
     * @param name    имя студента, не должно быть null или пустым
     * @param groupId идентификатор группы, к которой принадлежит студент
     * @throws IllegalArgumentException если name null или пустое,
     *                                  или groupId null
     */
    public Student(UUID id, String name, UUID groupId) {
        this.id = id;
        this.name = name;
        this.groupId = groupId;
    }

    /**
     * Возвращает уникальный идентификатор студента.
     *
     * @return UUID идентификатор студента
     */
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает имя студента.
     *
     * @return имя студента
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает идентификатор группы студента.
     *
     * @return UUID идентификатор группы
     */
    public UUID getGroupId() {
        return groupId;
    }

    /**
     * Устанавливает новое имя студента.
     *
     * @param name новое имя студента, не должно быть null или пустым
     * @throws IllegalArgumentException если name null или пустое
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Устанавливает новый идентификатор группы для студента.
     * Используется при переводе студента в другую группу.
     *
     * @param groupId новый идентификатор группы, не должен быть null
     * @throws IllegalArgumentException если groupId null
     */
    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    /**
     * Возвращает строковое представление студента в формате:
     * "Студент{ID: [первые_8_символов_ID]..., Имя: '[name]', Группа: [первые_8_символов_groupId]...}"
     *
     * @return строковое представление студента
     */
    @Override
    public String toString() {
        return String.format("Студент{ID: %s, Имя: '%s', Группа: %s}", id.toString().substring(0, 8) + "...", name, groupId.toString().substring(0, 8) + "...");
    }

    /**
     * Сравнивает студента с другим объектом на основе идентификатора.
     *
     * @param obj объект для сравнения
     * @return true если объекты имеют одинаковый идентификатор
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return id.equals(student.id);
    }

    /**
     * Возвращает хэш-код студента на основе идентификатора.
     *
     * @return хэш-код студента
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}