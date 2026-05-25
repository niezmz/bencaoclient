plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

import java.util.Properties

// -------- Versioning (single-machine, no Git) --------
// versionCode: minutes since Unix epoch + buildNumber (fits Int & Play max 2100000000; yyyyMMddHHmm as decimal overflows Int since 2026+)
// versionName: 1.0.<buildNumber>, buildNumber stored in root version.properties and auto-incremented on assemble/bundle.
val versionPropsFile = rootProject.file("version.properties")

fun readBuildNumber(): Int {
    if (!versionPropsFile.exists()) return 0
    val props = Properties()
    versionPropsFile.inputStream().use { props.load(it) }
    return props.getProperty("buildNumber")?.toIntOrNull() ?: 0
}

fun writeBuildNumber(buildNumber: Int) {
    val props = Properties()
    props["buildNumber"] = buildNumber.toString()
    versionPropsFile.outputStream().use { props.store(it, "Auto-generated. Do not edit while Gradle is running.") }
}

val shouldAutoBumpBuildNumber = gradle.startParameter.taskNames.any { name ->
    name.contains("assemble", ignoreCase = true) || name.contains("bundle", ignoreCase = true)
}

val buildNumber: Int = run {
    val current = readBuildNumber()
    if (shouldAutoBumpBuildNumber) {
        val next = current + 1
        writeBuildNumber(next)
        next
    } else {
        current
    }
}

val versionCodeTimestamp: Int =
    ((System.currentTimeMillis() / 60_000L) + buildNumber)
        .toInt()
        .coerceIn(1, 2_100_000_000)

fun readLocalProperty(key: String): String? {
    val file = rootProject.file("local.properties")
    if (!file.exists()) return null
    val props = Properties()
    file.inputStream().use { props.load(it) }
    return props.getProperty(key)?.takeIf { it.isNotBlank() }
}

fun readProjectProperty(key: String): String? {
    return (project.findProperty(key) as? String)?.takeIf { it.isNotBlank() }
}

android {
    namespace = "com.example.bencaoclient"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bencaoclient"
        minSdk = 29
        targetSdk = 36
        versionCode = versionCodeTimestamp
        versionName = "1.0.$buildNumber"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // A short per-build tag (changes every Gradle invocation) for UI display/debugging.
        // Example: v1.0.72-kx1p7jv3
        val buildTag = System.currentTimeMillis().toString(36)
        buildConfigField("String", "BUILD_TAG", "\"$buildTag\"")

        // Doubao (Volcengine Ark, OpenAI compatible)
        // Provide your key via local.properties: DOUBAO_API_KEY=xxxx
        // Or configure in-app via AI settings screen.
        val doubaoApiKey = readLocalProperty("DOUBAO_API_KEY") ?: ""
        buildConfigField("String", "DOUBAO_API_KEY", "\"$doubaoApiKey\"")
        buildConfigField("String", "DOUBAO_BASE_URL", "\"https://ark.cn-beijing.volces.com/api/v3\"")
        // NOTE: Ark model id usually contains version suffix; override via DOUBAO_MODEL if needed.
        // e.g. DOUBAO_MODEL=doubao-seed-1-6-vision-250815
        val doubaoModel = readProjectProperty("DOUBAO_MODEL")
            ?: readLocalProperty("DOUBAO_MODEL")
            ?: "doubao-seed-1-6-vision-250815"
        buildConfigField("String", "DOUBAO_MODEL", "\"$doubaoModel\"")
        // Planting method text model (cheaper, faster). Override via DOUBAO_PLANTING_MODEL
        // e.g. DOUBAO_PLANTING_MODEL=doubao-seed-2-0-mini-260428
        val doubaoPlantingModel = readProjectProperty("DOUBAO_PLANTING_MODEL")
            ?: readLocalProperty("DOUBAO_PLANTING_MODEL")
            ?: "doubao-seed-2-0-mini-260428"
        buildConfigField("String", "DOUBAO_PLANTING_MODEL", "\"$doubaoPlantingModel\"")


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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.lottie.compose)
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Markdown rendering
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.27.0-rc02")

    // HTTP client (DeepSeek / OpenAI compatible)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Exif (camera photo orientation)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Room（本地持久化）
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 升级 Fragment 库 (建议使用 1.3.6 或更高版本以解决你之前的报错)
    implementation("androidx.fragment:fragment-ktx:1.3.6")
    // 建议同时升级 Activity 库，因为 registerForActivityResult 依赖它
    implementation("androidx.activity:activity-ktx:1.3.1")
}