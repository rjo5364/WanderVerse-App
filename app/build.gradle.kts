import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "edu.psu.sweng888.wanderverseapp"
    compileSdk = 34

    buildFeatures {
        buildConfig = true // Enable BuildConfig generation
    }


    defaultConfig {
        applicationId = "edu.psu.sweng888.wanderverseapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        val localProperties = Properties()
        file(rootProject.file("local.properties")).inputStream().use {
            localProperties.load(it)
        }

        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY")
        val placesApiKey: String? = localProperties.getProperty("PLACES_API_KEY")

        buildConfigField("String", "PLACES_API_KEY", "\"${placesApiKey ?: ""}\"")

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation ("com.google.android.gms:play-services-maps:18.0.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.libraries.places:places:3.1.0")

    // Firebase and other dependencies
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)

    // Image loading
    implementation(libs.bumptech.glide)
    implementation(libs.play.services.maps)
    annotationProcessor(libs.bumptech.glide.compiler)


    // Core libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.junit.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
