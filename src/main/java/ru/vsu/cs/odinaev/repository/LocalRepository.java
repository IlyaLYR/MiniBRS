package ru.vsu.cs.odinaev.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LocalRepository<T> implements Repository<T> {
    private final Map<UUID, T> storage = new ConcurrentHashMap<>();
    private final File file;
    private final Class<T> entityClass;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LocalRepository(String fileName, Class<T> entityClass) {
        this.file = new File(fileName);
        this.entityClass = entityClass;
        load();
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public void save(T entity) {
        UUID id = extractUUID(entity);
        storage.put(id, entity);
        saveToFile();
    }

    @Override
    public List<T> find(Params<T> params) {
        if (params == null) {
            return new ArrayList<>(storage.values());
        }

        return storage.values().stream()
                .filter(entity -> matchesParams(entity, params))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<T> findFirst(Params<T> params) {
        if (params == null) {
            return storage.values().stream().findFirst();
        }

        return storage.values().stream()
                .filter(entity -> matchesParams(entity, params))
                .findFirst();
    }

    @Override
    public boolean exists(Params<T> params) {
        if (params == null) {
            return !storage.isEmpty();
        }

        return storage.values().stream()
                .anyMatch(entity -> matchesParams(entity, params));
    }

    @Override
    public int delete(Params<T> params) {
        if (params == null) {
            int count = storage.size();
            storage.clear();
            if (count > 0) {
                saveToFile();
            }
            return count;
        }

        List<UUID> idsToRemove = storage.entrySet().stream()
                .filter(entry -> matchesParams(entry.getValue(), params))
                .map(Map.Entry::getKey)
                .toList();

        idsToRemove.forEach(storage::remove);

        if (!idsToRemove.isEmpty()) {
            saveToFile();
        }

        return idsToRemove.size();
    }

    /**
     * Проверяет, соответствует ли сущность всем параметрам
     */
    private boolean matchesParams(T entity, Params<T> params) {
        Map<String, Object> filterParams = params.getParameters();

        for (Map.Entry<String, Object> param : filterParams.entrySet()) {
            String fieldName = param.getKey();
            Object expectedValue = param.getValue();

            if (!matchesField(entity, fieldName, expectedValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет соответствие значения одного поля
     */
    private boolean matchesField(T entity, String fieldName, Object expectedValue) {
        try {
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Object actualValue = entity.getClass().getMethod(getterName).invoke(entity);

            return Objects.equals(expectedValue, actualValue);

        } catch (Exception e) {
            // Пробуем для boolean полей с префиксом "is"
            try {
                String isGetterName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Object actualValue = entity.getClass().getMethod(isGetterName).invoke(entity);
                return Objects.equals(expectedValue, actualValue);
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(storage, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data to " + file.getName(), e);
        }
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type mapType = TypeToken.getParameterized(Map.class, UUID.class, entityClass).getType();
            Map<UUID, T> loaded = gson.fromJson(reader, mapType);
            if (loaded != null) {
                storage.putAll(loaded);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from " + file.getName(), e);
        }
    }

    private UUID extractUUID(T entity) {
        try {
            Object id = entity.getClass().getMethod("getId").invoke(entity);

            if (id instanceof UUID) {
                return (UUID) id;
            } else {
                throw new IllegalArgumentException("Entity getId() method must return UUID, but returned: " +
                        (id != null ? id.getClass().getSimpleName() : "null"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Entity must have a getId() method that returns UUID", e);
        }
    }
}