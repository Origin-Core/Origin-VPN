pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // اگر AndroidLibXrayLite را به صورت AAR محلی اضافه می‌کنید:
        flatDir { dirs("app/libs") }
    }
}
rootProject.name = "OriginVPN"
include(":app")
