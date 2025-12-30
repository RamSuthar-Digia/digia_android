@echo off
REM Quick publish script for local development (Windows)

echo Publishing Digia UI to Maven Local...

REM Clean and build
call gradlew.bat :digia-ui:clean

REM Publish to Maven Local
call gradlew.bat :digia-ui:publishToMavenLocal

echo.
echo Successfully published to Maven Local!
echo.
echo Location: %%USERPROFILE%%\.m2\repository\com\digia\digia-ui\1.0.0\
echo.
echo To use in your project:
echo   1. Add mavenLocal() to repositories in settings.gradle.kts
echo   2. Add: implementation("com.digia:digia-ui:1.0.0")
echo   3. Sync Gradle: gradlew --refresh-dependencies
echo.
pause

