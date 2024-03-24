plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
}

group = "org.vad1mchk.shenanigans"
version = "1.0-SNAPSHOT"

val exposedVersion: String by project

repositories {
    mavenCentral()
    maven(url="https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.charleskorn.kaml:kaml:0.57.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.1.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.vad1mchk.varargparse.mk2.MainKt")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to "org.vad1mchk.varargparse.mk2.MainKt"
        )
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }
}