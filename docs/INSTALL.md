# Install and set up

Everything about installing Screen Off, switching it on and removing it.

[← Back to the README](../README.md)

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
   asks to be an accessibility service. See [why Android warns](PRIVACY_AND_SAFETY.md#why-does-android-show-scary-warnings).

> Only install APKs from this repository's Releases page, or build the app yourself. Anyone can
> publish a modified copy of an open-source app, and an accessibility service is a powerful thing to
> grant. See [Safety notes](PRIVACY_AND_SAFETY.md#safety-notes-and-known-limitations).

### Option B: install with a USB cable (adb)

Turn on **Developer options > USB debugging** on the phone, connect it, then:

```
adb install -r ScreenOff-1.0.0.apk
```

### Option C: build it yourself

See [Build from source](BUILD.md). This is the most trustworthy option, because you
compile exactly the code you can read here.


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


## Uninstall

*Settings > Apps > See all apps > Screen Off > **Uninstall**.* You can switch the service off first in
*Settings > Accessibility*. Nothing else is left behind.
