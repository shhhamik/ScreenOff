# How it works

Screen Off is a single `AccessibilityService` (`LockService`) plus a settings screen and a tile.
This page explains the two double-tap detectors and why they are built the way they are.

## Locking the screen

The lock itself is one call: `performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)` (Android 9+). It needs no
root and no device-admin rights. If vibration is on, the lock is delayed by the length of the buzz so the
vibration is not cut short.

## The constraint

An accessibility service never receives raw touches on other apps' windows, and Android does not report
*where* a touch happened. So the app has to infer double taps indirectly, using two invisible overlay
windows (`TYPE_ACCESSIBILITY_OVERLAY`, which an accessibility service may add without any extra
permission).

## Status bar double tap

A thin, fully transparent window covers the status bar area. It receives real touches, so the detector
is simple:

- A tap is a down/up pair that stays within the system touch slop.
- Two tap-downs closer than the **double tap time** setting (but at least `MIN_TAP_GAP_MS = 40` ms
  apart, to ignore contact bounce) count as a double tap, and the screen locks.
- Because the strip would otherwise swallow swipes, a downward swipe is forwarded: the left and middle
  of the strip open the notification shade, the right half opens Quick Settings. Single taps on status
  bar icons are consumed by the strip (a known trade-off).
- When **Status bar only on home screen** is on, the strip is shown only while the launcher is in front.
- The strip is rebuilt on rotation, because the status bar height changes.

## Home screen double tap on empty space

The launcher, not this app, receives the touches on the home screen. The service therefore uses a 1x1
invisible overlay with `FLAG_WATCH_OUTSIDE_TOUCH`. Such a window is told `ACTION_OUTSIDE` for every
touch-down elsewhere on the screen, **without coordinates**.

That gives the detector one fact: "a finger just went down somewhere". To tell empty space from an icon,
it counts accessibility events that a real interaction produces:

1. While the launcher is in front, the service subscribes to click, long-click and scroll events in
   addition to window-state changes. Tapping an icon, folder or widget emits a click event; dragging
   emits scroll events; opening an app changes the window.
2. `interactionCount` increments on each of those events.
3. When two touch-downs arrive within the double-tap time, and `interactionCount` did not change between
   them, the taps hit nothing interactive.
4. A short `SETTLE_MS = 120` ms re-check waits for any late events (click events can arrive slightly
   after the touch) before locking.

Which package is "home" is learned from window-state-changed events and a cached list of launcher
packages (resolved from the `HOME` intent, refreshed every two minutes). Input-method packages are
cached the same way so the keyboard is not mistaken for the launcher.

## Why it is a heuristic

Everything above is inference. Cases where it can be wrong:

- A launcher or widget that handles touches without emitting accessibility click events can look like
  empty space.
- The app drawer and recents screens belong to the launcher, so empty space there can count as "home".
- The first window-state event must be seen after the service starts; before that, the service does not
  know whether home is showing.

## Events and privacy

| Event | When subscribed | What the code reads |
|---|---|---|
| `TYPE_WINDOW_STATE_CHANGED` | Always | package name |
| click, long-click, scroll | Only while the launcher is in front | event type only |

`canRetrieveWindowContent="false"` is set in
[`accessibility_service_config.xml`](../app/src/main/res/xml/accessibility_service_config.xml), so the
service gets no view hierarchy. The app declares only the `VIBRATE` permission, has no networking code,
and sets `allowBackup="false"`. A debug build additionally logs event types and package names with the
tag `ScreenOff`; release builds log nothing.

## Performance

The service does work only when Android delivers an event or a touch. Launcher and keyboard package
lists are cached for two minutes instead of being re-queried per event, event-type subscriptions are
switched on only while home is showing, and no timers run in the background.

## Code map

| File | Role |
|---|---|
| `LockService.kt` | Both detectors, overlay windows, event handling, locking |
| `LockTileService.kt` | Quick Settings tile; opens settings if the service is off |
| `SettingsActivity.kt` | The single settings screen, built in code |
| `Haptics.kt` | Builds and plays the vibration effect |
| `Prefs.kt` | Setting keys, defaults, ranges |
