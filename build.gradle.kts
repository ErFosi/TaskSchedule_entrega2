

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    id ("com.google.dagger.hilt.android") version "2.48.1" apply false
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"

    id("io.ktor.plugin") version "2.3.8"
    id("com.google.gms.google-services") version "4.4.1" apply false
}
