
dependencies {
    val vertxVersion = project.extra["vertxVersion"]
    val flywayVersion = project.extra["flywayVersion"]
    val jupiterVersion = project.extra["jupiterVersion"]
    val logbackClassicVersion = project.extra["logbackClassicVersion"]
    val postgresTestContainerVersion = project.extra["postgresTestContainerVersion"]

    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-config-yaml:$vertxVersion")
    implementation("io.vertx:vertx-pg-client:$vertxVersion")

    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("org.testcontainers:postgresql:$postgresTestContainerVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
}

application {
    mainClass.set("com.jeliiadesina.drone.App")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
