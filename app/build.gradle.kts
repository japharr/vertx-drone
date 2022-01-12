
dependencies {

}

application {
    mainClass.set("com.jeliiadesina.drone.App")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
