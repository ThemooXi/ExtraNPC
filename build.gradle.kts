plugins {
    java
}

group = "me.themoo"
version = "1.0.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
}

dependencies {
    // Compile against the oldest supported API. Newer features are loaded
    // reflectively so this one jar also runs on 1.20.x, 1.21.x and 26.x.
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveBaseName.set("ExtraNPC")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}
