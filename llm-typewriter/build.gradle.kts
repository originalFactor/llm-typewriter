/*
 * Copyright 2026 ECSDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.maven.publish)
}

// Release version is driven by the git tag on CI: tag `v0.2.0` publishes `0.2.0`.
// Override locally with `-Pversion=...`; otherwise builds use the literal below.
val libVersion: String =
    (System.getenv("RELEASE_VERSION") ?: findProperty("version") as String?)
        ?.removePrefix("v")
        ?.takeUnless { it.isBlank() || it == "unspecified" }
        ?: "0.1.0"

group = "cc.ptoe"
version = libVersion

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate()

    android {
        namespace = "cc.ptoe.llmtypewriter"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        androidResources { enable = true }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        withHostTest { }

        compilations.all {
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }
    }

    // Desktop JVM target (Compose Multiplatform). Math rendering uses RaTeX-CMP, whose JVM
    // native runtime is selected per-host in jvmMain below.
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // These types are part of the public API and must be compile-scope dependencies in
            // the generated Maven POM. Otherwise Maven consumers cannot compile calls that use
            // Flow, Modifier, TextStyle, Color, or Compose annotations.
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.animation)
            api(libs.compose.ui)
            api(libs.compose.material3)
            implementation(libs.coil.compose)
            api(libs.kotlinx.coroutines.core)
            implementation(libs.highlights)
            // RaTeX-CMP provides LaTeX math rendering (pure-Rust KaTeX-compatible core) for every
            // target from commonMain. JVM Desktop additionally pulls per-platform native runtimes
            // from jvmMain below.
            implementation(libs.ratex)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        // Android target — RaTeX-CMP (declared in commonMain) ships Android native libraries
        // (arm64-v8a / armeabi-v7a / x86_64 / x86) inside its AAR, so no per-target native
        // runtime selection is needed here.
        getByName("androidMain") {
            dependencies {
                implementation(libs.coil.network.okhttp)
            }
        }

        // Desktop JVM — Coil needs a network fetcher on JVM too. RaTeX-CMP's Rust core is
        // distributed as separate per-platform native artifacts; declare every desktop target so
        // the matching runtime resolves on each host. The aliases use `x8664` (not `x86-64`)
        // because a trailing `64` segment would start with a digit and can't map to a Kotlin
        // property accessor — the Maven coordinates still use `x86-64`.
        getByName("jvmMain") {
            dependencies {
                implementation(libs.coil.network.okhttp)
                runtimeOnly(libs.ratex.native.windows.x8664)
                runtimeOnly(libs.ratex.native.linux.x8664)
                runtimeOnly(libs.ratex.native.linux.aarch64)
                runtimeOnly(libs.ratex.native.darwin.x8664)
                runtimeOnly(libs.ratex.native.darwin.aarch64)
            }
        }

        // Compose UI tests — Android instrumented tests backed by `compose.uiTest`.
        // Pure-logic tests live in `commonTest` (run as `androidUnitTest`).
        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.compose.ui.test)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    if (
        project.hasProperty("signingInMemoryKey") ||
        project.hasProperty("signing.keyId")
    ) {
        signAllPublications()
    }

    coordinates("cc.ptoe", "llm-typewriter", libVersion)

    pom {
        name.set("LlmTypewriter")
        description.set(
            "Streaming-text typewriter composable for LLM apps on Compose — " +
                "renders a Flow<String> token stream with live progressive Markdown, " +
                "syntax-highlighted code blocks that build up as tokens arrive, a configurable " +
                "three speed curves (linear/easeOut/natural), tap-to-skip, " +
                "graceful stop-mid-stream, selectable text, and screen-reader-friendly a11y.",
        )
        inceptionYear.set("2026")
        url.set("https://github.com/ECSDevs/llm-typewriter")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            // Upstream author of llm-typewriter (this project is a fork — see
            // README "Credits" and LICENSE for the full attribution notice).
            developer {
                id.set("NadeemIqbal")
                name.set("NadeemIqbal")
                url.set("https://github.com/NadeemIqbal")
            }
            developer {
                id.set("originalFactor")
                name.set("originalFactor")
                email.set("2438926613@qq.com")
                url.set("https://github.com/originalFactor")
            }
        }
        scm {
            url.set("https://github.com/ECSDevs/llm-typewriter")
            connection.set("scm:git:git://github.com/ECSDevs/llm-typewriter.git")
            developerConnection.set("scm:git:ssh://git@github.com/ECSDevs/llm-typewriter.git")
        }
    }
}
