plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id ("kotlin-kapt")
    id ("com.google.dagger.hilt.android")

    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"


}

android {
    namespace = "com.example.taskschedule"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taskschedule"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.wear.compose:compose-material:1.3.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.compose.material:material-icons-extended:1.6.2")

    // Hilt
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("com.google.dagger:hilt-android:2.48.1")
    kapt ("com.google.dagger:hilt-compiler:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    //Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:${room_version}")

    //ksp("androidx.room:room-compiler:${room_version}")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:${room_version}")

    //Carousel card slider no se si lo usaré
    implementation("com.google.accompanist:accompanist-pager:0.35.0-alpha")
    implementation("androidx.compose.ui:ui-util:1.6.3")
    implementation("io.coil-kt:coil-compose:2.2.2")

    //Librerias de gráficos
    implementation("com.github.tehras:charts:0.2.4-alpha")

    //gson para los json
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("com.google.android.material:material:1.4.0")

    //ktor para cliente http
    val ktor_version="2.3.8"


    implementation ("io.ktor:ktor-client-android:$ktor_version")
    implementation ("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation ("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation ("io.ktor:ktor-client-auth:$ktor_version")
    implementation ("io.ktor:ktor-client-cio:$ktor_version")
    //implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation ("io.ktor:ktor-client-serialization-jvm:$ktor_version")
    implementation ("androidx.activity:activity-compose:1.8.2")
}