plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cocobar.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.cocobar.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

//dependencies {
//    implementation(libs.androidx.activity.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.androidx.constraintlayout)
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(libs.androidx.junit)
//}

dependencies {
    // ... other dependencies ...

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson converter for Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Logging interceptor for debugging Retrofit (optional but highly recommended)
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}