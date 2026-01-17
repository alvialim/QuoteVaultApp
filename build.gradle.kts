// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// Configure all projects with common settings
subprojects {
    afterEvaluate {
        // Configure Kotlin options for all modules
        extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions>()?.apply {
            jvmTarget = "17"
        }
        
        // Configure Android modules with common compile options
        extensions.findByType<com.android.build.gradle.BaseExtension>()?.apply {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}

// Clean task to remove all build artifacts
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}