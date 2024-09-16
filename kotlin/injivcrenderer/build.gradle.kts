plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
    `signing`
}

android {
    namespace = "io.mosip.injivcrenderer"
    compileSdk = 34

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.pixelpass)
    implementation(libs.squareup.okhttp)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.junit.jupiter)

}


tasks {
    register<Wrapper>("wrapper") {
        gradleVersion = "8.5"
        validateDistributionUrl = true
    }
}

tasks.register<Jar>("jarRelease") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn("assembleRelease")
    from("build/intermediates/javac/release/classes") {
        include("**/*.class")
    }
    from("build/tmp/kotlin-classes/release") {
        include("**/*.class")
    }
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = "0.1.0-SNAPSHOT"
    }
    archiveBaseName.set("${project.name}-release")
    archiveVersion.set("0.1.0-SNAPSHOT")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
apply(from = "publish-artifact.gradle")
tasks.register("generatePom") {
    dependsOn("generatePomFileForAarPublication", "generatePomFileForJarReleasePublication")
}

