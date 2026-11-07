# Build from source and contribute

Compile the app yourself, find your way around the code, and contribute.

[← Back to the README](../README.md)

---

## Build from source

You need **Android Studio** (a recent version; it bundles the JDK 17 that the build uses) and the
**Android SDK Platform 36**.

1. Clone or download this repository and open the folder in Android Studio. Trust the project and
   let Gradle sync (the first sync downloads a lot). If it offers to upgrade the Android Gradle
   Plugin, accepting is fine.
2. **Build > Generate App Bundles or APKs > Generate APKs**. The debug APK is at
   `app/build/outputs/apk/debug/app-debug.apk`.
3. Install it with `adb install -r app-debug.apk` (or copy it to the phone).

The app has no launcher activity, so Android Studio's green **Run** button reports a missing default
activity. Install with adb, or set **Run > Edit Configurations > app > Launch Options > Nothing**.

To make a signed release build, see [docs/RELEASING.md](RELEASING.md).

Changing the app's name or package? See "Renaming" in [docs/RELEASING.md](RELEASING.md).


## Project layout

| Path | What it is |
|---|---|
| [`LockService.kt`](../app/src/main/java/dev/shamik/screenoff/LockService.kt) | The accessibility service: both double-tap detectors and the lock action. |
| [`SettingsActivity.kt`](../app/src/main/java/dev/shamik/screenoff/SettingsActivity.kt) | The single screen with all switches and sliders (built in code, no layout files). |
| [`LockTileService.kt`](../app/src/main/java/dev/shamik/screenoff/LockTileService.kt) | The Quick Settings tile. |
| [`Haptics.kt`](../app/src/main/java/dev/shamik/screenoff/Haptics.kt) | The optional vibration effect. |
| [`Prefs.kt`](../app/src/main/java/dev/shamik/screenoff/Prefs.kt) | Setting keys, ranges and defaults. |
| [`AndroidManifest.xml`](../app/src/main/AndroidManifest.xml), [`accessibility_service_config.xml`](../app/src/main/res/xml/accessibility_service_config.xml) | The permissions and what the service listens for. |
| [`docs/HOW_IT_WORKS.md`](HOW_IT_WORKS.md) | A technical explanation of the detectors. |
| [`docs/RELEASING.md`](RELEASING.md) | How to make and publish a release. |


## Contributing

Bug reports and pull requests are welcome. Please keep the project's principles:

- **No `INTERNET` permission and no networking code.**
- **No third-party dependencies**, and no analytics or tracking of any kind.
- Keep the service's access as small as possible (`canRetrieveWindowContent` stays `false`).

For a bug report, use the issue template and include your phone model, Android version and
launcher. For a change, open an issue first if it is large.
