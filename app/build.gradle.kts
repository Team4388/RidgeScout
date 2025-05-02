import com.android.build.api.dsl.AaptOptions
import com.android.build.api.dsl.AndroidResources

plugins {
    alias(libs.plugins.androidApplication)
//    id("com.google.gms.google-services")
}

//allprojects {
//    repositories {
//        maven ( url = "https://jitpack.io" )
//    }
//}

android {
    namespace = "com.ridgebotics.ridgescout"
    compileSdk = 34

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }


    defaultConfig {
        applicationId = "com.ridgebotics.ridgescout"
        minSdk = 24
        targetSdk = 34
        versionCode = 6
        versionName = "0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
    aaptOptions {
        noCompress("tflite");
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.preference)
//    implementation(libs.support.annotations)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    var camerax_version = "1.3.2"
    implementation("androidx.camera:camera-core:1.3.2")
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation("com.github.skydoves:powerspinner:1.2.7")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


//    implementation("com.google.firebase:firebase-ml-modeldownloader:24.1.2")
//    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    implementation("org.tensorflow:tensorflow-lite-task-text:0.3.0")

    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("commons-net:commons-net:3.10.0")



//    implementation("com.github.DeveloperPaul123:SimpleBluetoothLibrary:1.5.1")

}

