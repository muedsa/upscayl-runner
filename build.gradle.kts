import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
}

group = "com.muedsa.upscayl"
version = "0.0.1"

application {
    mainClass.set("com.muedsa.upscayl.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.json)
    implementation(libs.ktor.server.metrics.micrometer)
    testImplementation(libs.ktor.server.test)

    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logback)
    implementation(libs.lettuce)

//    implementation(libs.koin.bom)
//    implementation(libs.koin.core)
//    implementation(libs.koin.ktor)
//    implementation(libs.koin.logger.slf4j)
//    testImplementation(libs.koin.test)

    val koinBomVersion = "3.5.3"
    implementation(platform("io.insert-koin:koin-bom:$koinBomVersion"))
    implementation("io.insert-koin:koin-ktor")
    implementation("io.insert-koin:koin-logger-slf4j")

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_11)
        localImageName.set("upscayl-runner")
        imageTag.set("0.0.0")
        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                8091,
                8091,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))
    }
}