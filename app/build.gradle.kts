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
}

application {
    mainClass.set("Holder.AppKt")
    applicationDefaultJvmArgs = listOf("-Djansi.force=true") // 標準入力のサポート
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in` // 標準入力を有効にする
}