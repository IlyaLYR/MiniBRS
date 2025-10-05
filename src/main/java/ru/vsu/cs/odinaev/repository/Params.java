package ru.vsu.cs.odinaev.repository;

import java.util.HashMap;
import java.util.Map;

public class Params<T> {
    private final Map<String, Object> parameters = new HashMap<>();
    private final Class<T> entityClass;

    public Params(Class<T> entityClass, String fieldName, Object value) {
        this.entityClass = entityClass;
        checkFieldExists(fieldName); // Проверяем при создании
        parameters.put(fieldName, value);
    }

    public Params(Class<T> entityClass, Map<String, Object> params) {
        this.entityClass = entityClass;
        for (String fieldName : params.keySet()) {
            checkFieldExists(fieldName); // Проверяем все поля
        }
        parameters.putAll(params);
    }

    // Только геттер
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    /**
     * Проверяет, что поле существует в классе T
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
                throw new IllegalArgumentException(
                        "Field '" + fieldName + "' does not exist in class " + entityClass.getSimpleName()
                );
            }
        }
    }

    @Override
    public String toString() {
        return "Params" + parameters;
    }
}