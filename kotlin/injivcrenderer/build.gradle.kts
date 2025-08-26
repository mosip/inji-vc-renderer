plugins {
    alias(libs.plugins.android.library)
    kotlin("multiplatform")
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
    alias(libs.plugins.sonarqube)
    jacoco
}
jacoco {
    toolVersion = "0.8.11"
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

kotlin {
    jvmToolchain(17)

    androidTarget()

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.squareup.okhttp)
                implementation(libs.google.zxing.javase)
                implementation(libs.org.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit)
                implementation(libs.mockito.core)
                implementation(libs.mockito.inline)
                implementation(libs.mockito.kotlin)
                implementation(libs.junit.jupiter)
                implementation(libs.robolectric)

            }
        }
        val jvmMain by getting{
            dependencies {
                implementation(libs.pixelpass.jar)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.pixelpass.aar)
            }
        }

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


tasks.withType<AbstractPublishToMaven>().configureEach {
    onlyIf {
        publication.name in listOf("aar", "jarRelease")

    }
}

tasks.register("jacocoTestReportJvm", JacocoReport::class) {
    dependsOn("jvmTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(fileTree(layout.buildDirectory.file("classes/kotlin/jvm/main")))
    sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/jvmMain/kotlin"))
    executionData.setFrom(files("${layout.buildDirectory.get()}/jacoco/jvmTest.exec"))
}

tasks.register("jacocoTestReportAndroid", JacocoReport::class) {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(fileTree(layout.buildDirectory.file("tmp/kotlin-classes/debug")))
    sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/androidMain/kotlin"))
    executionData.setFrom(files("${layout.buildDirectory.get()}/jacoco/testDebugUnitTest.exec"))
}

tasks.withType<Test> {
    jacoco {
        isEnabled = true
    }
    if (name.contains("jvm", ignoreCase = true)) {
        finalizedBy(tasks.named("jacocoTestReportJvm"))
    } else if (name.contains("android", ignoreCase = true)) {
        finalizedBy(tasks.named("jacocoTestReportAndroid"))
    }
}

tasks {
    register<Wrapper>("wrapper") {
        gradleVersion = "8.7"
        validateDistributionUrl = true
    }
}
tasks.register("prepareKotlinBuildScriptModel"){}
tasks.register<Jar>("jarRelease") {
    dependsOn("dokkaJavadoc")
    dependsOn("assembleRelease")
    dependsOn("jvmJar")
}

tasks.register<Jar>("javadocJar") {
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaHtml").get().outputs.files)
}
tasks.register("generatePom") {
    dependsOn("generatePomFileForAarPublication", "generatePomFileForJarReleasePublication")
}
apply(from = "publish-artifact.gradle")
var buildDir = project.layout.buildDirectory.get()
sonarqube {
    properties {
        property("sonar.java.binaries", "$buildDir/classes/kotlin/jvm/main, $buildDir/tmp/kotlin-classes/debug")
        property("sonar.language", "kotlin")
        property("sonar.exclusions", "**/build/**, **/*.kt.generated, **/R.java, **/BuildConfig.java")
        property("sonar.scm.disabled", "true")
        property("sonar.coverage.jacoco.xmlReportPaths",
            "$buildDir/reports/jacoco/jacocoTestReportJvm/jacocoTestReportJvm.xml," +
                    "$buildDir/reports/jacoco/jacocoTestReportAndroid/jacocoTestReportAndroid.xml"
        )
    }
}

