plugins {
    alias(libs.plugins.android.library)
    kotlin("multiplatform")
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(17)

    androidTarget()

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    applyDefaultHierarchyTemplate()



    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.pixelpass)
                implementation(libs.squareup.okhttp)
                implementation(libs.google.zxing.javase)
                implementation(libs.org.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockito.core)
                implementation(libs.mockito.inline)
                implementation(libs.mockito.kotlin)
                implementation(libs.junit.jupiter)
                implementation(libs.robolectric)
            }
        }
        val jvmMain by getting
        val androidMain by getting

    }
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
}

tasks {
    register<Wrapper>("wrapper") {
        gradleVersion = "8.7"
        validateDistributionUrl = true
    }
}

apply(from = "publish-artifact.gradle")

tasks.register("generateJar") {
    dependsOn("jvmJar")
}
tasks.register("generatePom") {
    dependsOn("generatePomFileForAarPublication", "generatePomFileForJarReleasePublication")
}

