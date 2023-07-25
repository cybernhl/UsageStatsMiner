pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "UsageStatsMiner"
//share
include(":shared")
//server
include(":server")
project(":server").projectDir = File("./server/server")
//client
include(":androidApp")