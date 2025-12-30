# Quick Reference: Syncing Latest Code

## For Package Consumers (Other Projects)

### 🚀 Quick Sync (3 Steps)

#### Step 1: Update Version
Edit your `gradle/libs.versions.toml`:
```toml
[versions]
digiaui = "1.0.1"  # ← Change this number
```

#### Step 2: Sync Gradle
```bash
./gradlew --refresh-dependencies
```

#### Step 3: Done! ✅
Your project now uses the latest Digia UI code.

---

## For Package Publishers (This Repo)

### 📦 Publishing New Version (4 Steps)

#### Step 1: Update Version Numbers
```bash
# Update these 3 files:
# 1. digia-ui/build.gradle.kts
version = "1.0.1"

# 2. app/src/main/java/com/digia/digiaui/version/DigiaUIVersion.kt
const val SDK_VERSION = "1.0.1"

# 3. gradle/libs.versions.toml
digiaui = "1.0.1"
```

#### Step 2: Publish
```bash
# For local testing:
./publish-local.sh

# For GitHub Packages (team sharing):
./gradlew :digia-ui:publish

# For Maven Central (public):
./gradlew :digia-ui:publishToMavenCentral
```

#### Step 3: Tag Release
```bash
git add .
git commit -m "Release v1.0.1"
git tag v1.0.1
git push origin main --tags
```

#### Step 4: GitHub Actions Auto-Publishes ✅
The CI/CD pipeline automatically:
- Builds the AAR
- Publishes to GitHub Packages
- Creates GitHub Release with artifacts

---

## Auto-Sync Options

### Option A: Composite Build (Best for Active Development)
Always gets latest code automatically!

In your app's `settings.gradle.kts`:
```kotlin
includeBuild("/path/to/digia_ui_compose")
```

✅ **No version management needed**  
✅ **Live code updates**  
✅ **Perfect for development**

### Option B: Version Catalog (Best for Teams)
Centralized version management.

```toml
# gradle/libs.versions.toml
[versions]
digiaui = "1.0.0"

[libraries]
digiaui = { module = "com.digia:digia-ui", version.ref = "digiaui" }
```

```kotlin
// build.gradle.kts
dependencies {
    implementation(libs.digiaui)
}
```

### Option C: Dependabot (Automated PRs)
Auto-creates PRs when new versions available.

```yaml
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "daily"
```

---

## Troubleshooting

### Not Getting Latest Code?

```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Force refresh dependencies
./gradlew --refresh-dependencies
```

### Build Errors After Update?

```bash
# Invalidate caches in Android Studio
File → Invalidate Caches → Invalidate and Restart

# Or command line:
./gradlew clean build --no-build-cache
```

---

## Version History

| Version | Date       | Changes                      |
|---------|------------|------------------------------|
| 1.0.0   | 2024-12-30 | Initial release              |
| 1.0.1   | TBD        | Bug fixes                    |
| 1.1.0   | TBD        | New widgets                  |

See [CHANGELOG.md](CHANGELOG.md) for detailed release notes.

---

## Need Help?

- 📖 **Full Guide**: [PUBLISHING.md](PUBLISHING.md)
- 📦 **Package Setup**: [USING_AS_PACKAGE.md](USING_AS_PACKAGE.md)
- 🐛 **Issues**: [GitHub Issues](https://github.com/digia/digia-ui-compose/issues)
- 💬 **Support**: support@digia.tech

