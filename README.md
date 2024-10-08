# Translations validator plugin
Gradle plugin which validates placeholders from translated strings.xml files by comparing them with main strings.xml file.

## Configuration

### 1. Add plugin's classpath to your app-level `build.gradle`.

```groovy
buildscript {
    repositories {
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        final validatorVer = '0.0.1'

        classpath "world.betterme.translationsvalidator:translations-validator-plugin-android:$validatorVer"
    }
}
```

### 2. Apply plugin in your module-level `build.gradle`.

```groovy
apply plugin: 'world.betterme.translationsvalidator'
```