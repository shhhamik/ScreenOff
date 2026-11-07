# Using Screen Off

How to use the gestures and tile, what every setting does, and what to try when something is wrong.

[← Back to the README](../README.md)

---

## How to use it

- **Home screen:** double-tap an empty area of the home screen. It must be a quick double tap
  (see [Settings](USAGE.md#settings) to tune the timing).
- **Status bar:** double-tap the very top strip of the screen.
- **Tile:** tap **Screen off** in Quick Settings. If the service is not on yet, the tile opens the
  app's screen instead.
- **Settings screen:** *Settings > Accessibility > Screen Off > **Settings***. (That row is added by
  Android and opens the app's single screen.) The tile also opens it when the service is off.

While the status bar strip is active, a downward swipe on it still works: a swipe on the left or
middle opens the notification shade, a swipe on the right half opens Quick Settings.


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


## Troubleshooting

| Problem | What to try |
|---|---|
| The Accessibility switch is greyed out or says "Restricted setting" | *Settings > Apps > Screen Off > three-dot menu > **Allow restricted settings***, then try again. |
| Play Protect blocks the install | **More details > Install anyway.** See [why](PRIVACY_AND_SAFETY.md#why-does-android-show-scary-warnings). |
| Double-tapping the home screen does nothing | Open any app and return home once. Check that **Home screen** is on. Make sure you are tapping empty space. Raise **Double tap time** a little. |
| It locks when I double-tap an icon | Please [report it](https://github.com/shhhamik/ScreenOff/issues) with your phone model, Android version and launcher. A debug-build log helps (below). |
| I cannot tap status bar icons | Expected while the status bar strip is active. Turn off **Status bar** (or keep **only on home screen** on). |
| No vibration | Check that **Vibrate** is on, that your phone's system vibration/haptics are not turned off, and that the phone has a vibration motor. Use **Test vibration**. |
| The service stopped by itself | Re-enable it in Accessibility settings and exclude the app from battery optimisation. |
| The tile is missing | Swipe down twice, tap the pencil icon and drag **Screen off** into the panel. |
| I cannot find the app | There is no launcher icon by design. Use *Settings > Accessibility > Screen Off > Settings*, or the tile. |

**Collecting a log (debug build only).** Build the debug variant and install it, connect the phone
with USB debugging, then run `adb logcat -s ScreenOff`. Double-tap empty space, then double-tap an
icon quickly, and copy the output into your issue. The log contains event types and package names
only. Release builds write no log.
