plugins {
    java
    application
}

allprojects {
    extra["vertxVersion"] = "4.2.3"
    extra["flywayVersion"] = "8.3.0"
    extra["jupiterVersion"] = "5.8.2"
    extra["logbackClassicVersion"] = "1.2.10"
    extra["postgresTestContainerVersion"] = "1.16.2"
}

subprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }

    apply(plugin = "java")
    apply(plugin = "application")
}