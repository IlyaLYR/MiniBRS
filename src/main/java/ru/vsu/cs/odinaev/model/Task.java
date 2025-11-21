package ru.vsu.cs.odinaev.model;

import java.util.UUID;

/**
 * Класс, представляющий учебную задачу студента.
 * Каждая задача связана с конкретным студентом и имеет номер и статус выполнения.
 * Задачи автоматически создаются при добавлении студента в систему.
 */
public class Task {

    /**
     * Уникальный идентификатор задачи
     */
    private final UUID id;

    /**
     * Идентификатор студента, которому принадлежит задача
     */
    private final UUID studentId;

    /**
     * Номер задачи (обычно от 1 до 3)
     */
    private final int number;

    /**
     * Статус выполнения задачи
     */
    private TaskStatus status;

    /**
     * Создает новую задачу для студента.
     *
     * @param id        уникальный идентификатор задачи
     * @param studentId идентификатор студента, не должен быть null
     * @param number    номер задачи, должен быть положительным числом
     * @param status    начальный статус задачи, не должен быть null
     * @throws IllegalArgumentException если studentId или status null,
     *                                  или number не положительный
     */
    public Task(UUID id, UUID studentId, int number, TaskStatus status) {
        this.id = id;
        this.studentId = studentId;
        this.number = number;
        this.status = status;
    }

    /**
     * Возвращает уникальный идентификатор задачи.
     *
     * @return UUID идентификатор задачи
     */
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает идентификатор студента, которому принадлежит задача.
     *
     * @return UUID идентификатор студента
     */
    public UUID getStudentId() {
        return studentId;
    }

    /**
     * Возвращает номер задачи.
     * Номер определяет порядок задач для студента.
     *
     * @return номер задачи
     */
    public int getNumber() {
        return number;
    }

    /**
     * Возвращает текущий статус выполнения задачи.
     *
     * @return статус задачи
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает новый статус выполнения задачи.
     * Используется для отметки задачи как сданной или сброса статуса.
     *
     * @param status новый статус задачи, не должен быть null
     * @throws IllegalArgumentException если status null
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * Проверяет, является ли задача сданной.
     *
     * @return true если статус задачи SUBMITTED
     */
    public boolean isSubmitted() {
        return status == TaskStatus.SUBMITTED;
    }

    /**
     * Проверяет, является ли задача несданной.
     *
     * @return true если статус задачи NOT_SUBMITTED
     */
    public boolean isNotSubmitted() {
        return status == TaskStatus.NOT_SUBMITTED;
    }

    /**
     * Возвращает строковое представление задачи в формате:
     * "Задача{ID: [первые_8_символов_ID]..., №[number], Статус: [эмодзи] [текст_статуса], Студент: [первые_8_символов_studentId]...}"
     * <p>
     * Пример: "Задача{ID: 123e4567..., №1, Статус: ✅ СДАНО, Студент: 123e4567...}"
     *
     * @return строковое представление задачи
     */
    @Override
    public String toString() {
        String statusEmoji = status == TaskStatus.SUBMITTED ? "✅" : "❌";
        String statusText = status == TaskStatus.SUBMITTED ? "СДАНО" : "НЕ СДАНО";

        return String.format("Задача{ID: %s, №%d, Статус: %s %s, Студент: %s}", id.toString().substring(0, 8) + "...", number, statusEmoji, statusText, studentId.toString().substring(0, 8) + "...");
    }

    /**
     * Сравнивает задачу с другим объектом на основе идентификатора.
     *
     * @param obj объект для сравнения
     * @return true если объекты имеют одинаковый идентификатор
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id.equals(task.id);
    }

    /**
     * Возвращает хэш-код задачи на основе идентификатора.
     *
     * @return хэш-код задачи
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}