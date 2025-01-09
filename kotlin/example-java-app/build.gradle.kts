plugins {
    id("java")
}

group = "io.mosip"
version = "unspecified"


dependencies {
    implementation(project(":injivcrenderer"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}