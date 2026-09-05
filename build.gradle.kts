plugins {
    java
}

group = "me.themoo"
version = "1.0.1"

val paperApiVersion = providers.gradleProperty("paperApiVersion")
    .orElse("1.21-R0.1-SNAPSHOT")

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
    // Compile against the compatibility floor. Newer APIs are accessed reflectively.
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion.get()}")
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
}
