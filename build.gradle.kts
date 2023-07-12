import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

// Versions
val kotlinVersion: String = "1.8.20"
val kvisionVersion: String = "6.6.0"

plugins {
    val kotlinVersion_p: String = "1.8.20"
    val kvisionVersion_p: String = "6.6.0"
    kotlin("plugin.serialization") version kotlinVersion_p
    kotlin("js") version kotlinVersion_p
    id("io.kvision") version kvisionVersion_p
}

version = "1.0.0-SNAPSHOT"
group = "io.github.muqhc"

repositories {
    mavenCentral()
    mavenLocal()
}


val webDir = file("src/main/web")

kotlin {
    js(IR) {
        browser {
            runTask {
                outputFileName = "main.bundle.js"
                sourceMaps = false
                devServer = KotlinWebpackConfig.DevServer(
                    open = false,
                    port = 3000,
                    proxy = mutableMapOf(
                        "/kv/*" to "http://localhost:8080",
                        "/kvws/*" to mapOf("target" to "ws://localhost:8080", "ws" to true)
                    ),
                    static = mutableListOf("$buildDir/processedResources/js/main")
                )
            }
            webpackTask {
                outputFileName = "main.bundle.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }
    sourceSets["main"].dependencies {
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
        implementation("io.kvision:kvision-i18n:$kvisionVersion")
    }
    sourceSets["test"].dependencies {
        implementation(kotlin("test-js"))
        implementation("io.kvision:kvision-testutils:$kvisionVersion")
    }
    sourceSets["main"].resources.srcDir(webDir)
}
