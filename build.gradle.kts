plugins {
    id("java")
    id("application")
    id("war") // Оставляем для создания WAR файлов
    id ("org.gretty") version ("4.1.0")
}

group = "ru.vsu.cs.odinaev"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // CMD зависимости (пометить для удаления) ⬇️
    implementation("org.jline:jline:3.25.0") // CMD - можно удалить
    implementation("org.jline:jline-terminal-jansi:3.23.0") // CMD - можно удалить
    implementation("info.picocli:picocli:4.7.5") // CMD - можно удалить
    annotationProcessor("info.picocli:picocli-codegen:4.7.5") // CMD - можно удалить

    // Общие зависимости (оставить) ⬇️
    implementation("com.google.code.gson:gson:2.10.1") // Используем для JSON
    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.typesafe:config:1.4.3")

    // Сервлет зависимости (минимум) ⬇️
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0") // Только API, без реализации

    // ИЛИ если используете Tomcat Embedded:
    // implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.15")
    // implementation("org.apache.tomcat.embed:tomcat-embed-jasper:10.1.15")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("ru.vsu.cs.odinaev.Main")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "ru.vsu.cs.odinaev.Main"
        )
    }

    archiveFileName.set("minibrs.jar")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.war {
    archiveFileName.set("minibrs.war")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

gretty {
    httpPort = 8080
    contextPath = "/"
}