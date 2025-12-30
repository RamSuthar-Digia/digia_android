#!/bin/bash
# Quick publish script for local development

set -e

echo "🚀 Publishing Digia UI to Maven Local..."

# Clean and build
./gradlew :digia-ui:clean

# Publish to Maven Local
./gradlew :digia-ui:publishToMavenLocal

echo ""
echo "✅ Successfully published to Maven Local!"
echo ""
echo "📦 Location: ~/.m2/repository/com/digia/digia-ui/1.0.0/"
echo ""
echo "💡 To use in your project:"
echo "   1. Add mavenLocal() to repositories in settings.gradle.kts"
echo "   2. Add: implementation(\"com.digia:digia-ui:1.0.0\")"
echo "   3. Sync Gradle: ./gradlew --refresh-dependencies"
echo ""

