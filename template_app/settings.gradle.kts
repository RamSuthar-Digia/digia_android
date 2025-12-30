pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // If you publish digia-ui to mavenLocal, enable this:
        // mavenLocal()
    }
}

rootProject.name = "digia_ui_template_app"

// Option 1 (recommended for dev): reference the library module directly by path
include(":digia-ui")
project(":digia-ui").projectDir = file("../digia-ui")

include(":app")

