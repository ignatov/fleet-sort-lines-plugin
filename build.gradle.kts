repositories {
    mavenCentral()
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
}

plugins {
    kotlin("jvm") version "1.8.0"
    `kotlin-dsl`
    id("org.jetbrains.fleet-plugin") version "0.1.31"
}

version = "0.1.0"

fleet {
    fleetVersion.set("1.17.23")
    useNightlyBuilds.set(true)

    // presentation
    vendor.set("Sergey Ignatov")
    readableName.set("Sort Lines")
    descriptor.set("A simple plugin that brings Sort Lines action")

    workspace {
        // workspace dependencies
    }

    frontend {
        // frontend dependencies
    }

    common {
        // common dependencies
    }
}
