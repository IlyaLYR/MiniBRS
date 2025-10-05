package ru.vsu.cs.odinaev.repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для хранения параметров фильтрации при поиске сущностей.
 * Обеспечивает типо-безопасность, проверяя существование полей в классе сущности.
 *
 * @param <T> тип сущности, для которой создаются параметры фильтрации
 */
public class Params<T> {

    /**
     * Map для хранения пар "имя поля - значение"
     */
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * Класс сущности для проверки существования полей
     */
    private final Class<T> entityClass;

    /**
     * Создает параметры с одним полем фильтрации.
     * Автоматически проверяет существование указанного поля в классе сущности.
     *
     * @param entityClass класс сущности для проверки полей
     * @param fieldName   имя поля для фильтрации (должно существовать в классе сущности)
     * @param value       значение для фильтрации
     * @throws IllegalArgumentException если поле не существует в классе сущности
     * @throws NullPointerException     если entityClass или fieldName равны null
     */
    public Params(Class<T> entityClass, String fieldName, Object value) {
        this.entityClass = entityClass;
        checkFieldExists(fieldName);
        parameters.put(fieldName, value);
    }

    /**
     * Создает параметры с несколькими полями фильтрации.
     * Автоматически проверяет существование всех указанных полей в классе сущности.
     *
     * @param entityClass класс сущности для проверки полей
     * @param params      Map с парами "имя поля - значение" для фильтрации
     * @throws IllegalArgumentException если какое-либо поле не существует в классе сущности
     * @throws NullPointerException     если entityClass или params равны null
     */
    public Params(Class<T> entityClass, Map<String, Object> params) {
        this.entityClass = entityClass;
        for (String fieldName : params.keySet()) {
            checkFieldExists(fieldName);
        }
        parameters.putAll(params);
    }

    /**
     * Возвращает копию Map с параметрами фильтрации.
     * Изменения в возвращаемой Map не влияют на оригинальные параметры.
     *
     * @return неизменяемая копия Map с параметрами фильтрации
     */
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    /**
     * Проверяет существование поля в классе сущности через рефлексию.
     * Ищет геттер для поля в формате getFieldName или isFieldName (для boolean).
     *
     * @param fieldName имя поля для проверки
     * @throws IllegalArgumentException если поле не существует в классе сущности
     */
    private void checkFieldExists(String fieldName) {
        try {
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            entityClass.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            // Пробуем для boolean полей
            try {
                String isGetterName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                entityClass.getMethod(isGetterName);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("Field '" + fieldName + "' does not exist in class " + entityClass.getSimpleName() + ". Expected getter methods: get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1) + " or is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            }
        }
    }

    /**
     * Возвращает строковое представление параметров в формате "Params{field1=value1, field2=value2}".
     *
     * @return строковое представление параметров
     */
    @Override
    public String toString() {
        return "Params" + parameters;
    }
}