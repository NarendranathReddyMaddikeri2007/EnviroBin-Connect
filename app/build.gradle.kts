plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.capstone.envibinconnect"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.capstone.envibinconnect"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("de.hdodenhof:circleimageview:3.1.0")


    //GOOGLE Services
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.libraries.places:places:3.2.0")

    //Workmanager & Coroutines
    implementation ("androidx.work:work-runtime-ktx:2.8.1")

//    //okHTTP Client
//    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
//    //GSON
//    implementation ("com.google.code.gson:gson:2.9.0")

    //Firebase
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation ("com.google.firebase:firebase-messaging-ktx:23.3.1")
    implementation("com.google.firebase:firebase-messaging:23.3.1")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation ("com.google.firebase:firebase-analytics-ktx:21.5.0")
    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

}