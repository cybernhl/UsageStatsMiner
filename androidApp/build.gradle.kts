plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.ysy.usagestatsminer.android"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.ysy.usagestatsminer.android"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = false
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
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
    implementation(project(":shared"))
//    implementation("androidx.compose.ui:ui:1.4.3")
//    implementation("androidx.compose.ui:ui-tooling:1.4.3")
//    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
//    implementation("androidx.compose.foundation:foundation:1.4.3")
//    implementation("androidx.compose.material:material:1.4.3")
//    implementation("androidx.activity:activity-compose:1.7.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.2")

    implementation("com.blankj:utilcodex:1.31.0")
}
