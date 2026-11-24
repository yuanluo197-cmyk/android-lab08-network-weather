plugins { alias(libs.plugins.android.application) }
android {
    namespace = "com.luoyuan.lab08"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "com.luoyuan.lab08"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.13.2")
}
