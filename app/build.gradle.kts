plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.loginpage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.loginpage"
        minSdk = 26  // Updated to support Java 9+ features
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17  // Set Java version to 17
        targetCompatibility = JavaVersion.VERSION_17
    }


}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.core:core:1.12.0")
    implementation ("com.google.firebase:firebase-messaging:23.1.0")
    implementation ("com.google.firebase:firebase-database:20.1.0")
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

