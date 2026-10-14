plugins {
 id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose"); id("com.google.devtools.ksp"); id("com.google.gms.google-services")
}
val releaseKeyFile=providers.environmentVariable("KPN_KEYSTORE_FILE").orNull
android {
 signingConfigs {
  if(releaseKeyFile!=null) create("kpnRelease") {
   storeFile=file(releaseKeyFile)
   storePassword=providers.environmentVariable("KPN_KEYSTORE_PASSWORD").orNull
   keyAlias=providers.environmentVariable("KPN_KEY_ALIAS").orElse("kpn-release").get()
   keyPassword=providers.environmentVariable("KPN_KEY_PASSWORD").orNull?.takeIf { it.isNotBlank() } ?: providers.environmentVariable("KPN_KEYSTORE_PASSWORD").orNull
  }
 }

 namespace="com.kpn.mobile"; compileSdk=35
 defaultConfig { applicationId="com.kpn.mobile"; minSdk=26; targetSdk=35; versionCode=7; versionName="1.2.4"; testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"; vectorDrawables.useSupportLibrary=true; buildConfigField("String","API_BASE_URL","\"https://www.kpn.com.ng/api/v1/\"") }
 buildTypes { debug { versionNameSuffix="-debug"; providers.gradleProperty("debugApiBaseUrl").orNull?.let { url -> require(url.startsWith("https://") || url.startsWith("http://10.0.2.2:")) { "Debug API must use HTTPS or the Android emulator host bridge." }; buildConfigField("String","API_BASE_URL","\"${url}\"") } }; release { isDebuggable=false; if(releaseKeyFile!=null)signingConfig=signingConfigs.getByName("kpnRelease"); isMinifyEnabled=providers.gradleProperty("enableMinify").orNull=="true"; isShrinkResources=isMinifyEnabled; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro") } }
 buildFeatures { compose=true; buildConfig=true }
 packaging { resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}") }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget="17" }
}
dependencies {
 implementation("androidx.exifinterface:exifinterface:1.3.7"); implementation(platform("androidx.compose:compose-bom:2025.01.00")); implementation("androidx.core:core-ktx:1.15.0"); implementation("androidx.core:core:1.15.0"); implementation("androidx.activity:activity-compose:1.10.0"); implementation("androidx.fragment:fragment-ktx:1.8.5"); implementation("androidx.compose.material3:material3"); implementation("androidx.compose.material:material-icons-extended"); implementation("androidx.compose.ui:ui"); implementation("androidx.compose.ui:ui-tooling-preview"); implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7"); implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7"); implementation("androidx.navigation:navigation-compose:2.8.5"); implementation("androidx.datastore:datastore-preferences:1.1.1"); implementation("androidx.security:security-crypto:1.1.0-alpha06"); implementation("androidx.browser:browser:1.8.0"); implementation("com.squareup.retrofit2:retrofit:2.11.0"); implementation("com.squareup.retrofit2:converter-moshi:2.11.0"); implementation("com.squareup.moshi:moshi-kotlin:1.15.1"); ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1"); implementation("com.squareup.okhttp3:logging-interceptor:4.12.0"); implementation("io.coil-kt:coil-compose:2.7.0"); implementation(platform("com.google.firebase:firebase-bom:33.7.0")); implementation("com.google.firebase:firebase-messaging-ktx"); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0"); testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0"); testImplementation("junit:junit:4.13.2"); testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0"); androidTestImplementation(platform("androidx.compose:compose-bom:2025.01.00")); androidTestImplementation("androidx.compose.ui:ui-test-junit4"); androidTestImplementation("androidx.test.ext:junit:1.2.1"); debugImplementation("androidx.compose.ui:ui-tooling"); debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Do not silently produce an unsigned artifact when a signed production build is requested.
tasks.matching{it.name=="packageRelease"}.configureEach {
 doFirst{require(releaseKeyFile!=null){"Set KPN_KEYSTORE_FILE and private signing environment variables before packaging release."}}
}
