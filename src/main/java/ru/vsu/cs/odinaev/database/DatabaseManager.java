package ru.vsu.cs.odinaev.database;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private static final DatabaseManager INSTANCE = new DatabaseManager();

    private DatabaseManager() {
        Config config = ConfigFactory.parseResources("database.conf").getConfig("database");
        this.dataSource = createDataSource(config);
        startH2Console();
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    private HikariDataSource createDataSource(Config config) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(config.getString("url"));
        hikariConfig.setUsername(config.getString("username"));
        hikariConfig.setPassword(config.getString("password"));
        hikariConfig.setDriverClassName(config.getString("driver"));

        Config poolConfig = config.getConfig("pool");
        hikariConfig.setMaximumPoolSize(poolConfig.getInt("size"));
        hikariConfig.setConnectionTimeout(poolConfig.getLong("connection-timeout"));
        hikariConfig.setIdleTimeout(poolConfig.getLong("idle-timeout"));
        hikariConfig.setMaxLifetime(poolConfig.getLong("max-lifetime"));

        return new HikariDataSource(hikariConfig);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Создаем таблицы
            createTables(stmt);

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void createTables(Statement stmt) throws SQLException {
        // Таблица групп
        String createGroupsTable = """
        CREATE TABLE IF NOT EXISTS groups (
            id VARCHAR(36) PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            course_number INT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;
        stmt.execute(createGroupsTable);

        // Таблица студентов
        String createStudentsTable = """
        CREATE TABLE IF NOT EXISTS students (
            id VARCHAR(36) PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            group_id VARCHAR(36) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
        )
        """;
        stmt.execute(createStudentsTable);

        // Таблица задач
        String createTasksTable = """
        CREATE TABLE IF NOT EXISTS tasks (
            id VARCHAR(36) PRIMARY KEY,
            student_id VARCHAR(36) NOT NULL,
            number INT NOT NULL,
            status VARCHAR(20) NOT NULL CHECK (status IN ('SUBMITTED', 'NOT_SUBMITTED')),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
            UNIQUE (student_id, number)
        )
        """;
        stmt.execute(createTasksTable);
    }
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    //Для отладки
    private void startH2Console() {
        try {
            // Запускаем H2 Console на порту 8082
            org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 Console available at: http://localhost:8082");
        } catch (SQLException e) {
            System.err.println("Failed to start H2 Console: " + e.getMessage());
        }
    }
}