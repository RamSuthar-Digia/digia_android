# Digia UI template app

This is a minimal Android app that consumes the `:digia-ui` library from this repo.

## How it works
- `settings.gradle.kts` includes `:digia-ui` by path (`../digia-ui`).
- `MainActivity` creates a `NetworkClient` + `ConfigResolver` and calls `getAppConfigFromNetwork`.

## Configure
Edit `template_app/app/src/main/java/com/digia/template/MainActivity.kt`:
- `accessKey`
- `baseUrl`
- endpoint path inside `getAppConfigFromNetwork(...)`

## Run
```bash
cd template_app
./gradlew :app:installDebug
```

Open the app and check the on-screen status text.

