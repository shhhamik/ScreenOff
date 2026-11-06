# Screen Off

**Turn your Android screen off with a double tap, on the home screen or the status bar, without
reaching for the power button.**

Free, open source, no ads, no tracking, no internet access, no root, no third-party libraries.

![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)
![Android 11+](https://img.shields.io/badge/Android-11%2B-3DDC84.svg)
![No internet permission](https://img.shields.io/badge/internet-no%20permission-success.svg)

> **Version 1.0.0.** Works on Android 11 and newer; developed on a Pixel 11 Pro running Android 17.
> If something misbehaves on your phone, please [open an issue](../../issues).

---

## Contents

- [Features](#features)
- [Install](#install)
- [Turn it on](#turn-it-on)
- [How to use it](#how-to-use-it)
- [Settings](#settings)
- [Permissions and privacy](#permissions-and-privacy)
- [Safety notes and known limitations](#safety-notes-and-known-limitations)
- [Troubleshooting](#troubleshooting)
- [Uninstall](#uninstall)
- [Build from source](#build-from-source)
- [Project layout](#project-layout)
- [Contributing](#contributing) · [Security](#security) · [License](#license)

---

## Features

| | |
|---|---|
| **Double-tap the home screen** | Double-tap an empty spot on your home screen and the screen turns off (locks). Tapping icons and widgets never triggers it. |
| **Double-tap the status bar** | Double-tap the strip at the very top of the screen. By default this only works while the home screen is showing, so it never gets in the way inside other apps. |
| **Quick Settings tile** | A "Screen off" tile for your Quick Settings panel: one tap locks the screen. |
| **Adjustable double-tap time** | A slider from 100 to 600 ms (default 300 ms) sets how fast the two taps must be. |
| **Optional vibration** | A short buzz when the screen turns off. Choose strength (if your phone supports it), length, and a single or double buzz, and test it on the spot. |
| **One simple settings screen** | Every option is a switch or a slider on a single screen that follows your phone's light/dark and Material You colours. |
| **Private by design** | No internet permission, no analytics, no accounts, nothing stored except your own settings. [Details below](#permissions-and-privacy). |
| **Tiny and light** | A few small Kotlin files, no libraries, no background work. Negligible battery use, and it can save battery because the screen goes off instantly instead of waiting for the timeout. |

The app has **no launcher icon** on purpose: everything happens through the Accessibility settings
page and the Quick Settings tile.

---

## Install

Pick **one** of these.

### Option A: download the APK (easiest)

1. Open the **Releases** section of this GitHub page (right-hand side, or the "Releases" link
   under the repository name) and download the latest `ScreenOff-x.y.z.apk`.
2. *(Recommended)* Check that the file is genuine. Compare its SHA-256 checksum with the one in the
   release notes:
   - Windows: `certutil -hashfile ScreenOff-1.0.0.apk SHA256`
   - macOS / Linux: `shasum -a 256 ScreenOff-1.0.0.apk`
3. Open the APK on your phone. If Android asks, allow **Install unknown apps** for the app you
   opened it with (Files, Chrome, ...), then tap **Install**.
4. If **Play Protect** says *"App blocked"* or *"unrecognised app"*, tap **More details**, then
   **Install anyway**. This warning appears because the app is not distributed through Google Play and
   asks to be an accessibility service. See [why Android warns](#why-does-android-show-scary-warnings).

> Only install APKs from this repository's Releases page, or build the app yourself. Anyone can
> publish a modified copy of an open-source app, and an accessibility service is a powerful thing to
> grant. See [Safety notes](#safety-notes-and-known-limitations).

### Option B: install with a USB cable (adb)

Turn on **Developer options > USB debugging** on the phone, connect it, then:

```
adb install -r ScreenOff-1.0.0.apk
```

### Option C: build it yourself

See [Build from source](#build-from-source). This is the most trustworthy option, because you
compile exactly the code you can read here.

---

## Turn it on

1. Open **Settings > Accessibility**. Find **Screen Off** (it may be under *Installed apps*,
   *Downloaded apps* or *Installed services*) and switch it on. Confirm Android's standard
   warning.
2. **If the switch is greyed out or says "Restricted setting":** open **Settings > Apps > See all
   apps > Screen Off**, tap the **three-dot menu** in the top corner, choose **Allow restricted
   settings**, then go back to step 1. Android does this for every app installed from an APK file.
3. Open any app and return to the home screen once. The service learns that the home screen is
   showing from a "window changed" event, so after switching it on (or after a restart) it starts
   watching only after the screen has changed at least once.
4. *(Optional)* Add the Quick Settings tile: swipe down twice, tap the **pencil (edit)** icon, and
   drag **Screen off** into your panel.

---

## How to use it

- **Home screen:** double-tap an empty area of the home screen. It must be a quick double tap
  (see [Settings](#settings) to tune the timing).
- **Status bar:** double-tap the very top strip of the screen.
- **Tile:** tap **Screen off** in Quick Settings. If the service is not on yet, the tile opens the
  app's screen instead.
- **Settings screen:** *Settings > Accessibility > Screen Off > **Settings***. (That row is added by
  Android and opens the app's single screen.) The tile also opens it when the service is off.

While the status bar strip is active, a downward swipe on it still works: a swipe on the left or
middle opens the notification shade, a swipe on the right half opens Quick Settings.

---

## Settings

| Setting | What it does | Default |
|---|---|---|
| **Home screen** | Double-tap empty home screen space to turn the screen off. | On |
| **Status bar** | Double-tap the status bar to turn the screen off. | On |
| **Status bar only on home screen** | Ignore the status bar trigger while another app is open. | On |
| **Double tap time** | Maximum time between the two taps, 100 to 600 ms. Raise it if double taps are missed; lower it if you lock by accident. | 300 ms |
| **Vibrate** | Short vibration when the screen turns off. | Off |
| **Vibration strength** | 10 to 100 %. Only shown if your phone's vibration motor supports it. | 60 % |
| **Vibration length** | 10 to 200 ms. | 40 ms |
| **Vibration pattern** | Single buzz or double buzz. | Single |
| **Test vibration** | Plays the current vibration settings. | n/a |

When vibration is on, the screen locks right after the buzz finishes (a fraction of a second later),
so the buzz is not cut short by the screen going dark.

---

## Permissions and privacy

### What the app can access

| Access | Why it is needed | What it does **not** allow |
|---|---|---|
| **Accessibility service** | Android only lets an accessibility service (1) turn the screen off with the "lock screen" action, (2) place the two invisible touch strips used for the double taps, and (3) see which app is in front. | The service is configured with `canRetrieveWindowContent="false"`: it cannot inspect what is on your screen. It cannot read text you type, cannot see passwords, cannot click for you, and cannot perform gestures. |
| **Vibrate** (`VIBRATE`) | The optional vibration effect. | Nothing else. It is a normal permission, granted automatically at install. |

**Not requested:** internet, storage or files, contacts, microphone, camera, location, phone, SMS,
notification access, or any other permission.

### What the service actually receives

- It always listens for **window changes**, to know whether the home screen is the app on screen.
  The code reads only the **package name** of the app involved.
- **Only while the home screen is showing,** it also asks for **click, long-click and scroll
  events**, purely to count that "something was tapped", which is how an icon is told apart from
  empty space. The code reads only the **event type**.
- Android may attach more detail to accessibility events. This app's code ignores all of it, and you
  can confirm that in `LockService.onAccessibilityEvent` in
  [`LockService.kt`](app/src/main/java/dev/shamik/screenoff/LockService.kt).
- The two invisible touch windows are told *that* a touch happened elsewhere on the screen, **not
  where**. Position is never used.

### What is stored or sent

- **Stored:** only your own settings (the switches and sliders), in the app's private storage.
  Backups are switched off (`allowBackup="false"`), so nothing is copied to cloud backups.
- **Sent:** nothing. There is no internet permission, no networking code, no analytics, and no
  crash reporting.
- **Logging:** a normal (release) build writes nothing to the system log. Only a *debug* build logs
  event types and package names, to help find problems (see [Troubleshooting](#troubleshooting)).

### Check it yourself

- Open [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml): the only `<uses-permission>` is
  `VIBRATE`.
- On a built APK, with the Android SDK build-tools:
  `aapt2 dump permissions ScreenOff-1.0.0.apk` should list only `android.permission.VIBRATE`.
- [`app/build.gradle.kts`](app/build.gradle.kts) has no dependencies: the app uses only the Android
  framework.
- The whole code base is a handful of short Kotlin files under
  [`app/src/main/java/dev/shamik/screenoff/`](app/src/main/java/dev/shamik/screenoff).

### Why does Android show scary warnings?

Android shows the same strong warning ("this service can observe your actions and read what's on
your screen") for **every** accessibility service, because the permission is powerful in general.
That wording describes what an accessibility service *could* be allowed to do, not what this one is
configured to do. Play Protect adds its own warning because the APK does not come from Google Play.
That is why it matters to install only genuine builds.

---

## Safety notes and known limitations

**Trust and installation**

- An accessibility service is a powerful permission. Install only the APK from this repository's
  Releases (and verify the checksum), or build it yourself.
- Never install a copy of this app from a random link or chat message. A modified copy could do harm.

**Compatibility**

- Needs **Android 11 or newer**.
- Developed on a Pixel 11 Pro (Android 17, stock launcher). Other launchers may report taps
  differently, which can affect the home screen double tap. Report anything odd and it can be fixed.

**Behaviour you might notice**

- **The home screen double tap is a heuristic.** Android does not tell an app where a touch landed,
  so the app treats two quick touches with no click, scroll or window change in between as "empty
  space". Details are in [docs/HOW_IT_WORKS.md](docs/HOW_IT_WORKS.md). A launcher or widget that does
  not report taps to Android can cause an unintended lock when double-tapped.
- The launcher's own screens (app drawer, recents) count as "home screen" and may also lock on a
  double tap in empty space.
- While the status bar strip is active, **single taps on status bar icons do not pass through**
  (swipes down still open the shade and Quick Settings). Turn off the **Status bar** trigger, or leave
  **Status bar only on home screen** on, if this bothers you.
- Pressing the power button is still the dependable way to turn the screen off. If the service is
  switched off or stopped by Android, the gestures stop working until it is on again.
- Some phones with aggressive battery savers stop accessibility services in the background. If it
  stops working, exclude the app from battery optimisation.
- **Some apps (for example some banking or payment apps) check whether accessibility services are
  enabled and may warn you or refuse to run.** If an app
  complains, switch **Screen Off** off in Accessibility settings while you use it.
- If you installed an older or differently-signed copy of the app, uninstall it before installing a
  new one: Android will not install over an app signed with another key.

**Battery**

- The service does no background work, holds no wake locks and makes no network calls. It only runs
  when Android hands it an event, so its battery usage is negligible.
- It can actually **save** battery: the screen goes off the instant you double-tap, instead of staying
  lit until the screen timeout runs out. You can also set a shorter system timeout without it
  getting in your way.

---

## Troubleshooting

| Problem | What to try |
|---|---|
| The Accessibility switch is greyed out or says "Restricted setting" | *Settings > Apps > Screen Off > three-dot menu > **Allow restricted settings***, then try again. |
| Play Protect blocks the install | **More details > Install anyway.** See [why](#why-does-android-show-scary-warnings). |
| Double-tapping the home screen does nothing | Open any app and return home once. Check that **Home screen** is on. Make sure you are tapping empty space. Raise **Double tap time** a little. |
| It locks when I double-tap an icon | Please [report it](../../issues) with your phone model, Android version and launcher. A debug-build log helps (below). |
| I cannot tap status bar icons | Expected while the status bar strip is active. Turn off **Status bar** (or keep **only on home screen** on). |
| No vibration | Check that **Vibrate** is on, that your phone's system vibration/haptics are not turned off, and that the phone has a vibration motor. Use **Test vibration**. |
| The service stopped by itself | Re-enable it in Accessibility settings and exclude the app from battery optimisation. |
| The tile is missing | Swipe down twice, tap the pencil icon and drag **Screen off** into the panel. |
| I cannot find the app | There is no launcher icon by design. Use *Settings > Accessibility > Screen Off > Settings*, or the tile. |

**Collecting a log (debug build only).** Build the debug variant and install it, connect the phone
with USB debugging, then run `adb logcat -s ScreenOff`. Double-tap empty space, then double-tap an
icon quickly, and copy the output into your issue. The log contains event types and package names
only. Release builds write no log.

---

## Uninstall

*Settings > Apps > See all apps > Screen Off > **Uninstall**.* You can switch the service off first in
*Settings > Accessibility*. Nothing else is left behind.

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

To make a signed release build, see [docs/RELEASING.md](docs/RELEASING.md).

Changing the app's name or package? See "Renaming" in [docs/RELEASING.md](docs/RELEASING.md).

---

## Project layout

| Path | What it is |
|---|---|
| [`LockService.kt`](app/src/main/java/dev/shamik/screenoff/LockService.kt) | The accessibility service: both double-tap detectors and the lock action. |
| [`SettingsActivity.kt`](app/src/main/java/dev/shamik/screenoff/SettingsActivity.kt) | The single screen with all switches and sliders (built in code, no layout files). |
| [`LockTileService.kt`](app/src/main/java/dev/shamik/screenoff/LockTileService.kt) | The Quick Settings tile. |
| [`Haptics.kt`](app/src/main/java/dev/shamik/screenoff/Haptics.kt) | The optional vibration effect. |
| [`Prefs.kt`](app/src/main/java/dev/shamik/screenoff/Prefs.kt) | Setting keys, ranges and defaults. |
| [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml), [`accessibility_service_config.xml`](app/src/main/res/xml/accessibility_service_config.xml) | The permissions and what the service listens for. |
| [`docs/HOW_IT_WORKS.md`](docs/HOW_IT_WORKS.md) | A technical explanation of the detectors. |
| [`docs/RELEASING.md`](docs/RELEASING.md) | How to make and publish a release. |

---

## Contributing

Bug reports and pull requests are welcome. Please keep the project's principles:

- **No `INTERNET` permission and no networking code.**
- **No third-party dependencies**, and no analytics or tracking of any kind.
- Keep the service's access as small as possible (`canRetrieveWindowContent` stays `false`).

For a bug report, use the issue template and include your phone model, Android version and
launcher. For a change, open an issue first if it is large.

## Security

See [SECURITY.md](SECURITY.md) for how to report a vulnerability.

## License

Released under the [MIT License](LICENSE). Copyright (c) 2026 Shamik.

This is an independent project and is not affiliated with or endorsed by Google. "Pixel" and
"Android" are trademarks of Google LLC. The software is provided "as is", without warranty of any
kind; use it at your own risk.
