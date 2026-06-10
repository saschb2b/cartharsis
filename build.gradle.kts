// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless)
}

// The "prettier" of this repo: ktlint via Spotless. `spotlessApply` formats,
// `spotlessCheck` gates in CI. Settings are passed explicitly because the
// ktlint step does not reliably discover .editorconfig on its own; keep the
// two in sync (the .editorconfig copy is for IDEs).
val ktlintSettings = mapOf(
    "ktlint_code_style" to "android_studio",
    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
    "ij_kotlin_allow_trailing_comma" to "true",
    "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
    "max_line_length" to "120",
    // One-arg-per-line explosions ruin compact data tables like FakeCatalog.
    "ktlint_standard_argument-list-wrapping" to "disabled",
)

spotless {
    kotlin {
        target("**/src/**/*.kt")
        targetExclude("**/build/**")
        ktlint().editorConfigOverride(ktlintSettings)
    }
    kotlinGradle {
        target("*.gradle.kts", "app/*.gradle.kts")
        ktlint().editorConfigOverride(ktlintSettings)
    }
}
