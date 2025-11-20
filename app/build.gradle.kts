plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "dualnback.millenniumCodex.dualnback"
    compileSdk = 36

    defaultConfig {
        applicationId = "dualnback.millenniumCodex.dualnback"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

   dependenciesInfo {
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false

    }

    signingConfigs {
        create("release") {
            // These lines read the details from your gradle.properties file
            storeFile = file(System.getenv("MYAPP_RELEASE_STORE_FILE") ?: project.properties["MYAPP_RELEASE_STORE_FILE"] as String)
            storePassword = System.getenv("MYAPP_RELEASE_STORE_PASSWORD") ?: project.properties["MYAPP_RELEASE_STORE_PASSWORD"] as String
            keyAlias = System.getenv("MYAPP_RELEASE_KEY_ALIAS") ?: project.properties["MYAPP_RELEASE_KEY_ALIAS"] as String
            keyPassword = System.getenv("MYAPP_RELEASE_KEY_PASSWORD") ?: project.properties["MYAPP_RELEASE_KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.8"
        }
    }
} // <-- The 'android' block now correctly closes here at the end

dependencies {
    // FIX #3: MERGED ALL DEPENDENCIES INTO ONE BLOCK
    
    // Standard Compose & Core dependencies from both blocks
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room for Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Lifecycle Components for observing state
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Vico for Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
