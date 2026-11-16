/*
 * Copyright 2026 originalFactor
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy at https://www.apache.org/licenses/LICENSE-2.0
 */
@file:Suppress("DEPRECATION", "OPT_IN_USAGE", "UnstableApiUsage")

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    androidLibrary {
        namespace = "cc.ptoe.llmtypewriter"
        compileSdk { version = release(37) }
        buildToolsVersion = rootProject.ext["buildToolsVersion"] as String
        minSdk = 30
        androidResources { enable = true }
    }
    jvm("desktop")
    sourceSets {
        commonMain {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.animation)
                api(compose.ui)
                api(libs.jetbrains.compose.material3)
                api(libs.kotlinx.coroutines.core)
                implementation(libs.coil3.compose)
                implementation(libs.typewriter.highlights)
                implementation(libs.latex.renderer)
                implementation(libs.latex.fonts.katex)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidMain") {
            dependencies { implementation(libs.coil3.network.okhttp) }
        }
        getByName("desktopMain") {
            kotlin.srcDir("src/jvmMain/kotlin")
            dependencies {
                implementation(libs.coil3.network.okhttp)

            }
        }
    }
}
