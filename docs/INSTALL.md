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
4. **Play Protect will block the install, and there is usually no "Install anyway" button.** This is
   expected. Pause Play Protect first, as described in [Pause Play Protect to install](#pause-play-protect-to-install),
   then install.

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


## Pause Play Protect to install

> **You need to pause Play Protect scanning while you install Screen Off.** Play Protect blocks the install
> and does not offer an "Install anyway" button. Pausing it for a minute is the normal way round this. Turn
> it back on straight afterwards.

### Why it has to be paused

- Screen Off is an **accessibility service**, and Android treats that as a sensitive permission.
- The app is **new, unsigned by Google and not on Google Play**, so Play Protect has no history for it and
  blocks it by default. This is how it treats almost every new open-source accessibility app. It is not a
  finding that this app does anything wrong.
- For this kind of block Android gives **no override button**, so scanning has to be paused for the install.
- Pausing is only needed **once, for the install**. Turn scanning back on as soon as the app is installed.
  Screen Off does not need Play Protect off to run.

If you would rather not pause it, you can [build the app yourself](BUILD.md) from the public source, or
install from a PC with adb (below). Nothing in the app is hidden: it has no internet permission and cannot
read your screen (see [Permissions, privacy and safety](PRIVACY_AND_SAFETY.md)).

### Steps

1. Open the **Play Store** app.
2. Tap your **profile picture** (top right), then **Play Protect**.
3. Tap the **gear icon** (top right).
4. Turn off **Scan apps with Play Protect** and confirm.
5. Open the downloaded `ScreenOff-x.y.z.apk` and tap **Install**. If Android asks, allow **Install unknown
   apps** for the app you opened the file from (Files or Chrome).
6. When the install finishes, go back to the same screen and **turn Scan apps with Play Protect back on**.
7. Continue with [Turn it on](#turn-it-on).

Menu names vary slightly between phones and Play Store versions. If you cannot find the setting, search for
"Play Protect" in your phone's Settings.

### Alternatives

- **Send the app for scanning.** If the prompt offers **More details > Send app for scanning**, use it. Once
  Google has scanned that exact APK, it is often let through. Most phones do not show this option.
- **Install from a PC with adb**, which skips the install-time block: `adb install -r ScreenOff-1.0.0.apk`
  (turn on USB debugging first, see Option B above).

Always use the signed APK from Releases and keep the same signature across updates. Play Protect's trust
attaches to the signature, so updates are usually blocked less often than the first install.

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
