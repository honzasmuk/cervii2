import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar

plugins {
    kotlin("jvm") version "1.4.21"
}

group = "org.androworks.cervii2"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j", "slf4j-api", "1.7.30")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("ch.qos.logback", "logback-core", "1.2.3")
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = archiveVersion.get()
        attributes["Main-Class"] = "org.androworks.cervii2.client.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}
