# Permissions, privacy and safety

What the app can access, what it stores, how to check it yourself, and the limitations to know about.

[← Back to the README](../README.md)

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
  [`LockService.kt`](../app/src/main/java/dev/shamik/screenoff/LockService.kt).
- The two invisible touch windows are told *that* a touch happened elsewhere on the screen, **not
  where**. Position is never used.

### What is stored or sent

- **Stored:** only your own settings (the switches and sliders), in the app's private storage.
  Backups are switched off (`allowBackup="false"`), so nothing is copied to cloud backups.
- **Sent:** nothing. There is no internet permission, no networking code, no analytics, and no
  crash reporting.
- **Logging:** a normal (release) build writes nothing to the system log. Only a *debug* build logs
  event types and package names, to help find problems (see [Troubleshooting](USAGE.md#troubleshooting)).

### Check it yourself

- Open [`AndroidManifest.xml`](../app/src/main/AndroidManifest.xml): the only `<uses-permission>` is
  `VIBRATE`.
- On a built APK, with the Android SDK build-tools:
  `aapt2 dump permissions ScreenOff-1.0.0.apk` should list only `android.permission.VIBRATE`.
- [`app/build.gradle.kts`](../app/build.gradle.kts) has no dependencies: the app uses only the Android
  framework.
- The whole code base is a handful of short Kotlin files under
  [`app/src/main/java/dev/shamik/screenoff/`](../app/src/main/java/dev/shamik/screenoff).

### Why does Android show scary warnings?

Android shows the same strong warning ("this service can observe your actions and read what's on
your screen") for **every** accessibility service, because the permission is powerful in general.
That wording describes what an accessibility service *could* be allowed to do, not what this one is
configured to do. Play Protect adds its own warning because the APK does not come from Google Play.
That is why it matters to install only genuine builds.


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
  space". Details are in [docs/HOW_IT_WORKS.md](HOW_IT_WORKS.md). A launcher or widget that does
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
