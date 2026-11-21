plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.ioscc"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.ioscc"
        minSdk = 31
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
