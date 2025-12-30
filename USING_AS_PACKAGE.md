# Using `digia_ui_compose` as a package (Android/Kotlin)

This repo currently contains:
- `:app` — a sample/host application
- `:digia-ui` — **library module** you can consume from another Android project

> Note: for now, `:digia-ui` points its `sourceSets` at `:app/src/main` (so we don’t have to move files yet). Later we can migrate the code into `digia-ui/src/main/...` cleanly.

---

## Option A (recommended during development): include as a Gradle composite build

This is the easiest way to use the library in another app while you’re actively developing it.

### 1) In your app repo, add the Digia UI repo as a folder
Example:

```
my-app/
  settings.gradle.kts
  app/
  external/
    digia_ui_compose/
```

### 2) In **your app** `settings.gradle.kts`
Add:

```kotlin
includeBuild("external/digia_ui_compose")
```

### 3) In **your app module** `app/build.gradle.kts`
Add:

```kotlin
dependencies {
    implementation("com.digia:digia-ui")
}
```

### 4) In Digia UI repo: set a group + version
In `digia_ui_compose/digia-ui/build.gradle.kts` add (top-level):

```kotlin
group = "com.digia"
version = "0.0.1"
```

Now Gradle will resolve `com.digia:digia-ui` from the included build.

---

## Option B: include as a source module (`include(":digia-ui")`)

If you don’t want composite builds, you can include the module directly by path.

In your app `settings.gradle.kts`:

```kotlin
include(":digia-ui")
project(":digia-ui").projectDir = file("external/digia_ui_compose/digia-ui")
```

And then in `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":digia-ui"))
}
```

---

## Option C: publish an AAR to local Maven (for CI / sharing)

Inside `digia_ui_compose/digia-ui/build.gradle.kts`, apply `maven-publish` and publish.

### 1) Add:

```kotlin
plugins {
    id("maven-publish")
}

group = "com.digia"
version = "0.0.1"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "digia-ui"
            }
        }
    }
}
```

### 2) Publish
Run in this repo:

```bash
./gradlew :digia-ui:publishToMavenLocal
```

### 3) Consume in your other app
In your other app’s `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
  repositories {
    mavenLocal()
    google()
    mavenCentral()
  }
}
```

Then:

```kotlin
dependencies {
    implementation("com.digia:digia-ui:0.0.1")
}
```

---

## Quick usage snippet

Once added as a dependency, you can import the public APIs, for example:

```kotlin
import com.digia.digiaui.init.DigiaUI
import com.digia.digiaui.init.DigiaUIOptions

// ...
```

If you tell me your target app’s package name + how you want to initialize Digia UI (asset config vs network config), I can wire a minimal working init example for that app.

