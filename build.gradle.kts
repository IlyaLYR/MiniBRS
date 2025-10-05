plugins {
    id("java")
    id("application")
}

group = "ru.vsu.cs.odinaev"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jline:jline:3.23.0")
    implementation("org.jline:jline-terminal-jansi:3.23.0")
    implementation("com.google.code.gson:gson:2.10.1")
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

    // Фиксируем имя JAR файла
    archiveFileName.set("minibrs.jar")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}