plugins {
    kotlin("jvm") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("blue.starry:penicillin:6.2.3")
    implementation("io.ktor:ktor-client-cio:1.6.8")

    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_17.toString()
                apiVersion = "1.6"
                languageVersion = "1.6"
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings {
            progressiveMode = true
            optIn("kotlin.RequiresOptIn")
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.setlist.MainKt")
    }
}
