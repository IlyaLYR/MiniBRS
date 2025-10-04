package ru.vsu.cs.odinaev.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRepository<T> implements Repository<T> {
    private final Map<UUID, T> storage = new ConcurrentHashMap<>();
    private final File file;
    private final Class<T> type;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LocalRepository(String fileName, Class<T> type) {
        this.file = new File(fileName);
        this.type = type;
        load();
    }

    @Override
    public void save(T entity) {
        UUID id = extractUUID(entity);
        storage.put(id, entity);
        saveToFile();
    }

    @Override
    public Optional<T> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean deleteById(UUID id) {
        boolean removed = storage.remove(id) != null;
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(UUID id) {
        return storage.containsKey(id);
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
            Type mapType = TypeToken.getParameterized(Map.class, UUID.class, type).getType();
            Map<UUID, T> loaded = gson.fromJson(reader, mapType);
            if (loaded != null) {
                storage.putAll(loaded);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from " + file.getName(), e);
        }
    }

    // Исправленный метод для извлечения UUID
    private UUID extractUUID(T entity) {
        try {
            // Пытаемся получить UUID через метод getId()
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