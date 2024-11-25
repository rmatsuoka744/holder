plugins {
    kotlin("jvm") version "1.8.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.auth0:java-jwt:4.2.1") // Java JWTライブラリ
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // JSON処理に必要
}

application {
    mainClass.set("Holder.AppKt")
    applicationDefaultJvmArgs = listOf("-Djansi.force=true") // 標準入力のサポート
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in` // 標準入力を有効にする
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // 例: 17に設定
    }
}
