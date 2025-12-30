# Publishing Digia UI Compose

This guide explains how to publish and consume the Digia UI library.

## Publishing to Maven Local (for development/testing)

From the project root:

```bash
./gradlew :digia-ui:publishToMavenLocal
```

This publishes to `~/.m2/repository/com/digia/digia-ui/1.0.0/`

## Publishing to GitHub Packages (recommended for teams)

### 1. Set up GitHub token

Add to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

### 2. Update build.gradle.kts

Add GitHub Packages repository:

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/YOUR_ORG/digia_ui_compose")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
        }
    }
}
```

### 3. Publish

```bash
./gradlew :digia-ui:publish
```

## Publishing to Maven Central (for public distribution)

### 1. Sign up for Sonatype OSSRH

Follow: https://central.sonatype.org/publish/publish-guide/

### 2. Configure signing

Add to `~/.gradle/gradle.properties`:

```properties
signing.keyId=YOUR_KEY_ID
signing.password=YOUR_KEY_PASSWORD
signing.secretKeyRingFile=/path/to/secring.gpg

ossrhUsername=YOUR_SONATYPE_USERNAME
ossrhPassword=YOUR_SONATYPE_PASSWORD
```

### 3. Update build.gradle.kts

Add signing and Sonatype repository configuration (see full example below).

### 4. Publish

```bash
./gradlew :digia-ui:publishToMavenCentral
```

## Consuming in Other Projects

### Option A: From Maven Local (development)

In your app's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.digia:digia-ui:1.0.0")
}
```

### Option B: From GitHub Packages

In your app's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/YOUR_ORG/digia_ui_compose")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                password = providers.gradleProperty("gpr.token").orNull
            }
        }
        google()
        mavenCentral()
    }
}
```

### Option C: From Maven Central (public)

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.digia:digia-ui:1.0.0")
}
```

No special repository configuration needed - Maven Central is included by default.

### Option D: Composite Build (active development)

Best for when you're actively developing the library and consuming app together.

In your app's `settings.gradle.kts`:

```kotlin
includeBuild("/path/to/digia_ui_compose")
```

Gradle will automatically pick up changes from the library project.

## Versioning

Current version: **1.0.0**

To bump the version:

1. Update `digia-ui/build.gradle.kts` → `version = "x.y.z"`
2. Update `app/src/main/java/com/digia/digiaui/version/DigiaUIVersion.kt`
3. Commit and tag: `git tag v1.0.1 && git push --tags`
4. Publish: `./gradlew :digia-ui:publish`

## Automatic Sync

### Using Gradle Version Catalogs

Create `gradle/libs.versions.toml`:

```toml
[versions]
digiaui = "1.0.0"

[libraries]
digiaui = { module = "com.digia:digia-ui", version.ref = "digiaui" }
```

In your app:

```kotlin
dependencies {
    implementation(libs.digiaui)
}
```

Now updating versions is centralized!

### Using Dependabot (GitHub)

Add `.github/dependabot.yml`:

```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "daily"
```

Dependabot will auto-create PRs when new versions are published.

## CI/CD Publishing (GitHub Actions)

Create `.github/workflows/publish.yml`:

```yaml
name: Publish Package

on:
  push:
    tags:
      - 'v*'

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Publish to GitHub Packages
        run: ./gradlew :digia-ui:publish
        env:
          USERNAME: ${{ github.actor }}
          TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

Push a tag to trigger:

```bash
git tag v1.0.1
git push origin v1.0.1
```

