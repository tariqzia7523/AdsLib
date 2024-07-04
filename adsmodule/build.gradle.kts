
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.module.ads"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        multiDexEnabled = true
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
    buildFeatures {
        buildConfig = true
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.review)
    implementation(libs.app.update)
    implementation(libs.billing)
    implementation(libs.sdp.android)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    implementation(libs.facebook)
    implementation(libs.multidex)
    constraints {
        implementation("androidx.work:work-runtime:2.7.0")
    }
}

afterEvaluate {
    (extensions["publishing"] as org.gradle.api.publish.PublishingExtension).apply {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.tariqzia7523"
                artifactId = "AdsLib"
                version = "3.0.4"
            }
        }
    }
}

