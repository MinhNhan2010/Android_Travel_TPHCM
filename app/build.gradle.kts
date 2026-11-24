import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Gemini API key: thêm dòng GEMINI_API_KEY=... vào local.properties (file này không được commit)
val geminiApiKey: String = run {
    val props = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { stream -> props.load(stream) }
    (props.getProperty("GEMINI_API_KEY") ?: System.getenv("GEMINI_API_KEY") ?: "").trim()
}

android {
    namespace = "com.example.nhom_15_dulichtphcm"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.nhom_15_dulichtphcm"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.annotation)

    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.10")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.2.10")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.10")
        implementation("androidx.lifecycle:lifecycle-common-jvm:2.11.0")
        implementation("androidx.lifecycle:lifecycle-runtime-android:2.11.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel:2.11.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-android:2.11.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate-android:2.11.0")
        implementation("androidx.lifecycle:lifecycle-livedata-core:2.11.0")
        implementation("androidx.arch.core:core-runtime:2.2.0")
        implementation("androidx.arch.core:core-common:2.2.0")
        implementation("androidx.slidingpanelayout:slidingpanelayout:1.2.0")
        implementation("androidx.window:window:1.2.0")
    }

    testImplementation(libs.junit)
    testImplementation(libs.org.json)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
