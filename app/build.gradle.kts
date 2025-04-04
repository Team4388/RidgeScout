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
        versionCode = 11    // **IMPORTANT** Increment this before releasing on github
        versionName = "1.4"// **IMPORTANT** Change this before releasing on github (<Year num since 2024>.<Update Version>)

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
    implementation(libs.material3)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation("androidx.navigation:navigation-fragment:2.8.9")
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

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


//    implementation("com.google.firebase:firebase-ml-modeldownloader:24.1.2")
//    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    implementation("org.tensorflow:tensorflow-lite-task-text:0.3.0")

    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("commons-net:commons-net:3.10.0")



//    implementation("com.github.DeveloperPaul123:SimpleBluetoothLibrary:1.5.1")

}

