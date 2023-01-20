import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "be.vamaralds"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("org.slf4j:slf4j-simple:2.0.6")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("org.hyperledger.fabric:fabric-gateway-java:2.2.0")
    implementation("org.json:json:20220924")
    implementation("com.github.ajalt.clikt:clikt:3.5.1")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta9")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}