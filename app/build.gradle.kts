import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

apply(plugin = "com.github.johnrengelman.shadow")

dependencies {
    val vertxVersion = project.extra["vertxVersion"]
    val flywayVersion = project.extra["flywayVersion"]
    val jupiterVersion = project.extra["jupiterVersion"]
    val postgresVersion = project.extra["postgresVersion"]
    val logbackClassicVersion = project.extra["logbackClassicVersion"]
    val postgresTestContainerVersion = project.extra["postgresTestContainerVersion"]
    val restAssuredVersion = project.extra["restAssuredVersion"]
    val assertjVersion = project.extra["assertjVersion"]
    val testContainersVersion = project.extra["testContainersVersion"]
    val nettyResolverVersion = project.extra["nettyResolverVersion"]
    val yaviVersion = project.extra["yaviVersion"]

    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-config-yaml:$vertxVersion")
    implementation("io.vertx:vertx-pg-client:$vertxVersion")
    implementation("io.vertx:vertx-web-validation:$vertxVersion")
    implementation("io.vertx:vertx-service-proxy:$vertxVersion")

    implementation("io.vertx:vertx-codegen:$vertxVersion:processor")

    annotationProcessor("io.vertx:vertx-codegen:$vertxVersion:processor")
    annotationProcessor("io.vertx:vertx-service-proxy:$vertxVersion")

    implementation("am.ik.yavi:yavi:$yaviVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("org.testcontainers:postgresql:$postgresTestContainerVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    testImplementation("io.vertx:vertx-junit5:$vertxVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")

    runtimeOnly("io.netty:netty-resolver-dns-native-macos:$nettyResolverVersion:osx-x86_64")
}

application {
    mainClass.set("com.jeliiadesina.drone.MainVerticle")
}

tasks.test {
    useJUnitPlatform()
}
