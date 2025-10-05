package ru.vsu.cs.odinaev.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Реализация репозитория, хранящая данные в локальном JSON-файле.
 * Обеспечивает сохранение данных между запусками приложения.
 *
 * @param <T> тип сущности, должен иметь метод getId(), возвращающий UUID
 */
public class LocalRepository<T> implements Repository<T> {

    /**
     * In-memory хранилище сущностей в виде Map<UUID, T>
     */
    private final Map<UUID, T> storage = new ConcurrentHashMap<>();

    /**
     * Файл для хранения данных в формате JSON
     */
    private final File file;

    /**
     * Класс сущности для корректной десериализации
     */
    private final Class<T> entityClass;

    /**
     * Экземпляр Gson для сериализации/десериализации JSON
     */
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Создает новый репозиторий с указанным файлом для хранения данных.
     * При создании автоматически загружает данные из файла, если он существует.
     *
     * @param fileName    имя файла для хранения данных (например, "groups.json")
     * @param entityClass класс сущности для корректной десериализации
     * @throws IllegalArgumentException если entityClass равен null
     */
    public LocalRepository(String fileName, Class<T> entityClass) {
        this.file = new File(fileName);
        this.entityClass = entityClass;
        load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * {@inheritDoc}
     * Сохраняет сущность в памяти и немедленно записывает изменения в файл.
     *
     * @throws IllegalArgumentException если entity равен null или не имеет метода getId()
     * @throws RuntimeException         если произошла ошибка при записи в файл
     */
    @Override
    public void save(T entity) {
        UUID id = extractUUID(entity);
        storage.put(id, entity);
        saveToFile();
    }

    /**
     * {@inheritDoc}
     * Выполняет поиск сущностей по параметрам фильтрации.
     * Если параметры не указаны, возвращает все сущности.
     *
     * @param params параметры фильтрации, может быть null
     * @return список сущностей, удовлетворяющих критериям поиска
     */
    @Override
    public List<T> find(Params<T> params) {
        if (params == null) {
            return new ArrayList<>(storage.values());
        }

        return storage.values().stream().filter(entity -> matchesParams(entity, params)).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Находит первую сущность, удовлетворяющую параметрам фильтрации.
     *
     * @param params параметры фильтрации, не может быть null
     * @return Optional с найденной сущностью или empty, если ничего не найдено
     * @throws IllegalArgumentException если params равен null
     */
    @Override
    public Optional<T> findFirst(Params<T> params) {
        if (params == null) {
            return storage.values().stream().findFirst();
        }

        return storage.values().stream().filter(entity -> matchesParams(entity, params)).findFirst();
    }

    /**
     * {@inheritDoc}
     * Проверяет существование сущностей, удовлетворяющих параметрам фильтрации.
     *
     * @param params параметры фильтрации, не может быть null
     * @return true если найдена хотя бы одна сущность, удовлетворяющая параметрам
     * @throws IllegalArgumentException если params равен null
     */
    @Override
    public boolean exists(Params<T> params) {
        if (params == null) {
            return !storage.isEmpty();
        }

        return storage.values().stream().anyMatch(entity -> matchesParams(entity, params));
    }

    /**
     * {@inheritDoc}
     * Удаляет сущности, удовлетворяющие параметрам фильтрации.
     * Если параметры не указаны, удаляет все сущности.
     * Изменения автоматически сохраняются в файл.
     *
     * @param params параметры фильтрации для определения удаляемых сущностей
     * @return количество удаленных сущностей
     */
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

        List<UUID> idsToRemove = storage.entrySet().stream().filter(entry -> matchesParams(entry.getValue(), params)).map(Map.Entry::getKey).toList();

        idsToRemove.forEach(storage::remove);

        if (!idsToRemove.isEmpty()) {
            saveToFile();
        }

        return idsToRemove.size();
    }

    /**
     * Проверяет, соответствует ли сущность всем заданным параметрам фильтрации.
     * Использует рефлексию для получения значений полей сущности.
     *
     * @param entity проверяемая сущность
     * @param params параметры фильтрации
     * @return true если сущность удовлетворяет всем параметрам
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
     * Проверяет соответствие значения конкретного поля сущности ожидаемому значению.
     * Использует рефлексию для вызова геттеров (getFieldName или isFieldName).
     *
     * @param entity        проверяемая сущность
     * @param fieldName     имя поля для проверки
     * @param expectedValue ожидаемое значение поля
     * @return true если значение поля соответствует ожидаемому
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

    /**
     * Сохраняет текущее состояние хранилища в JSON-файл.
     *
     * @throws RuntimeException если произошла ошибка ввода-вывода при записи файла
     */
    private void saveToFile() {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(storage, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data to " + file.getName(), e);
        }
    }

    /**
     * Загружает данные из JSON-файла в память.
     * Если файл не существует, метод завершается без ошибок.
     *
     * @throws RuntimeException если произошла ошибка ввода-вывода при чтении файла
     *                          или ошибка десериализации JSON
     */
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

    /**
     * Извлекает UUID из сущности с помощью рефлексии.
     * Вызывает метод getId() сущности и проверяет, что он возвращает UUID.
     *
     * @param entity сущность для извлечения ID
     * @return UUID сущности
     * @throws IllegalArgumentException если сущность не имеет метода getId()
     *                                  или метод возвращает не UUID
     */
    private UUID extractUUID(T entity) {
        try {
            Object id = entity.getClass().getMethod("getId").invoke(entity);

            if (id instanceof UUID) {
                return (UUID) id;
            } else {
                throw new IllegalArgumentException("Entity getId() method must return UUID, but returned: " + (id != null ? id.getClass().getSimpleName() : "null"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Entity must have a getId() method that returns UUID", e);
        }
    }
}