plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.ssti.alermapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ssti.alermapp"
        minSdk = 24
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding=true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-android-compiler:2.57.1")

    //navigation-fragment
    implementation("androidx.navigation:navigation-fragment:2.9.7")
    implementation("androidx.navigation:navigation-ui:2.9.7")
    //swiperefreshlayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    // Retrofit & Gson
    implementation ("com.squareup.retrofit2:retrofit:3.0.0")
    implementation ("com.squareup.retrofit2:converter-gson:3.0.0")
    // Okhttp
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // ViewModel support
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    // Core Room dependency
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")  // For coroutines/Flow
    ksp("androidx.room:room-compiler:2.8.4")
    testImplementation("androidx.room:room-testing:2.8.4")
    implementation("androidx.sqlite:sqlite-bundled:2.4.0")
    // Paging 3
    implementation("androidx.paging:paging-runtime-ktx:3.4.0")
    testImplementation("androidx.paging:paging-common:3.4.0")
    implementation("androidx.room:room-paging:2.6.1")
// Glide
    implementation("com.github.bumptech.glide:glide:4.13.0")
}