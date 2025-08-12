### Firebase setup (Google Services plugin via version catalog)

This project uses the Gradle Version Catalog and plugin aliases. The Google Services plugin is managed via an alias to keep versions centralized.

- Plugin alias is defined in `gradle/libs.versions.toml` under `[plugins]` as `googleServices`.
- Version is defined in `[versions]` as `googleServices = "4.4.3"`.

#### Where the plugin is applied
- Root `build.gradle.kts` declares the plugin with `apply false` using the alias:
  - `alias(libs.plugins.googleServices) apply false`
- Android module `composeApp/build.gradle.kts` applies the plugin:
  - `plugins { alias(libs.plugins.googleServices) }`

#### google-services.json placement
Place your Firebase config files in module-specific source sets:
- `composeApp/src/debug/google-services.json`
- `composeApp/src/release/google-services.json`

This supports different Firebase projects per build type. You may also use product flavors similarly.

#### Adding Firebase dependencies
Use the Firebase BOM to align versions. This project already wires it into `androidMain` of `composeApp` along with Firebase AI:

```kotlin
// in composeApp/build.gradle.kts (androidMain.dependencies)
implementation(platform(libs.firebase.bom))
// Firebase AI Logic
implementation(libs.firebase.ai)
// Example: Analytics (optional)
implementation(libs.firebase.analytics.ktx)
```

Aliases are defined in `gradle/libs.versions.toml`:
- `[versions] firebase-bom = "34.1.0"`
- `[libraries] firebase-bom`, `firebase-ai`, and `firebase-analytics-ktx`

#### Sync and build
After changes, sync Gradle and rebuild the project.
