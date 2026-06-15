// Generates the app's Baseline Profile: a list of hot classes/methods that
// ProfileInstaller pre-compiles (AOT) at install time, so the first frames of
// startup, the home grid, and the product screen don't pay JIT cost. Run it on
// an API 28+ device/emulator with `./gradlew :app:generateBaselineProfile`; the
// result lands in app/src/release/generated/baselineProfiles/.
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.cartharsis.baselineprofile"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        // Baseline Profile generation needs an API 28+ device; the profile it
        // produces still benefits the app's full minSdk 24 range.
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro)
}
