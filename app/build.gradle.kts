plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.wzchou.jmgopresetswitcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.wzchou.jmgopresetswitcher"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
}
