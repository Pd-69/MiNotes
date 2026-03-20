plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "net.micode.notes"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.micode.notes"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // 使用 Apache HTTP 兼容库
        useLibrary("org.apache.http.legacy")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // 使用 Apache HTTP 兼容库
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}