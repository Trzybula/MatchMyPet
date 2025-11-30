plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
    // ← UŻYWAMY TYLKO TEGO!
    alias(libs.plugins.sqlDelight)

}

group = "org.example.server"
version = "1.0"

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.example.project.ApplicationKt")
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("org.example.project.db")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
        }
    }
}


dependencies {
    implementation(project(":shared"))

    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serializationKotlinxJson)
    implementation(libs.logback)
    implementation("io.ktor:ktor-server-cors:3.3.1")
    implementation(libs.runtime)
    implementation(libs.sqlite.driver)
    implementation(libs.sqldelight.coroutines)
}

