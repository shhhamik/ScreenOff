package dev.shamik.screenoff

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

/**
 * Turns the screen off. Uses the accessibility "lock screen" global action, which is the
 * only thing this service does with its permission.
 *
 * Two triggers:
 *
 * 1. Status bar strip: a thin, invisible strip over the status bar. Two quick taps lock the
 *    screen. A downward swipe on it is forwarded by opening the notification shade
 *    (left/middle) or Quick Settings (right half), because the strip would otherwise
 *    swallow the pull-down gesture.
 *
 * 2. Empty home screen space: while the home screen is showing, a 1x1 invisible window with
 *    FLAG_WATCH_OUTSIDE_TOUCH is told about every touch elsewhere on the screen (it never
 *    receives the touch itself, so the launcher works normally). Android hides WHERE those
 *    touches happened, so the position is not used. Instead: two touches within the
 *    double-tap time with NO click, long-click, scroll or window change in between are
 *    treated as a double tap on empty space. A tap on an icon, widget or folder always
 *    produces one of those events, which cancels the lock.
 */
class LockService : AccessibilityService() {

    companion object {
        /** The running service, or null if the user has not enabled it. */
        @Volatile
        var instance: LockService? = null
            private set

        private const val TAG = "ScreenOff"

        /** After the 2nd tap, wait this long for a late "something was clicked" event. */
        private const val SETTLE_MS = 120L

        /** Ignore two touches closer together than this (same as the platform's double-tap minimum). */
        private const val MIN_TAP_GAP_MS = 40L

        /** How long the launcher / keyboard package lists are reused. */
        private const val PACKAGE_CACHE_MS = 2 * 60 * 1000L

        private const val EVENTS_BASE = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        private const val EVENTS_HOME = EVENTS_BASE or
            AccessibilityEvent.TYPE_VIEW_CLICKED or
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED or
            AccessibilityEvent.TYPE_VIEW_SCROLLED
    }

    private var windowManager: WindowManager? = null
    private var prefs: SharedPreferences? = null
    private val handler = Handler(Looper.getMainLooper())
    private var debug = false

    private var statusStrip: View? = null
    private var tapWatcher: View? = null
    private var onHomeScreen = false

    // Cached lists of launcher and keyboard packages (see ensurePackageSets).
    private var launcherSet: Set<String> = emptySet()
    private var imeSet: Set<String> = emptySet()
    private var setsLoadedAt = 0L

    // Empty-space double-tap state.
    private var interactionCount = 0
    private var interactionsAtFirstTap = 0
    private var lastTapTime = 0L

    private val prefListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refreshOverlays() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        setsLoadedAt = 0L // reload the launcher / keyboard lists
        debug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefs = Prefs.get(this).also { it.registerOnSharedPreferenceChangeListener(prefListener) }
        refreshOverlays()
    }

    /**
     * Locks the screen. If vibration is on, the buzz plays first and the lock follows once it
     * has finished, so the screen going dark does not cut it short. Returns false if the
     * system refused an immediate lock.
     */
    fun lockScreen(): Boolean {
        val buzzMs = Haptics.play(this)
        if (buzzMs <= 0L) return performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) }, buzzMs)
        return true
    }

    /** The double-tap time chosen with the slider. */
    private fun doubleTapMs(): Long =
        (prefs?.let { Prefs.doubleTapMs(it) } ?: Prefs.TAP_MS_DEFAULT).toLong()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (debug) {
            Log.d(TAG, "event ${AccessibilityEvent.eventTypeToString(event.eventType)} from ${event.packageName}")
        }
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> interactionCount++

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                interactionCount++
                trackForeground(event)
            }
        }
    }

    override fun onInterrupt() {}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Rotation changes the status bar height; rebuild the windows.
        hideStatusStrip()
        hideTapWatcher()
        refreshOverlays()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        handler.removeCallbacksAndMessages(null)
        hideStatusStrip()
        hideTapWatcher()
        prefs?.unregisterOnSharedPreferenceChangeListener(prefListener)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /** Works out whether the home screen is the app on screen. */
    private fun trackForeground(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        // System UI (shade, volume panel...) and the keyboard are overlays, not "the app on screen".
        ensurePackageSets()
        if (pkg == packageName || pkg == "com.android.systemui" || pkg in imeSet) return

        val home = pkg in launcherSet
        if (home != onHomeScreen) {
            onHomeScreen = home
            refreshOverlays()
        }
    }

    private fun refreshOverlays() {
        val p = prefs ?: return
        val statusBarOn = p.getBoolean(Prefs.KEY_STATUS_BAR, true)
        val homeOnly = p.getBoolean(Prefs.KEY_ONLY_HOME, true)
        val homeTapOn = p.getBoolean(Prefs.KEY_HOME_DOUBLE_TAP, true)

        if (statusBarOn && (!homeOnly || onHomeScreen)) showStatusStrip() else hideStatusStrip()

        val watch = homeTapOn && onHomeScreen
        if (watch) {
            showTapWatcher()
        } else {
            hideTapWatcher()
            lastTapTime = 0L
        }
        // Click/scroll events are only needed (and only requested) while the home screen is watched.
        setEventTypes(if (watch) EVENTS_HOME else EVENTS_BASE)
    }

    private fun setEventTypes(types: Int) {
        val info = serviceInfo ?: return
        if (info.eventTypes != types) {
            info.eventTypes = types
            serviceInfo = info
        }
    }

    // ---- Empty home screen space ------------------------------------------------------

    @SuppressLint("ClickableViewAccessibility")
    private fun showTapWatcher() {
        if (tapWatcher != null) return
        val wm = windowManager ?: return

        val view = View(this)
        view.setOnTouchListener { _, ev ->
            if (ev.actionMasked == MotionEvent.ACTION_OUTSIDE) {
                onOutsideTouch(ev.eventTime)
            }
            false
        }

        val params = WindowManager.LayoutParams(
            1,
            1,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            wm.addView(view, params)
            tapWatcher = view
        } catch (e: RuntimeException) {
            tapWatcher = null
        }
    }

    private fun hideTapWatcher() {
        val view = tapWatcher ?: return
        tapWatcher = null
        try {
            windowManager?.removeView(view)
        } catch (e: IllegalArgumentException) {
            // already gone
        }
    }

    /** Called for every touch-down anywhere on screen while the home screen is showing. */
    private fun onOutsideTouch(time: Long) {
        val gap = time - lastTapTime
        val isSecondTap = lastTapTime != 0L &&
            gap >= MIN_TAP_GAP_MS &&
            gap <= doubleTapMs()

        if (debug) {
            Log.d(TAG, "outside touch gap=$gap second=$isSecondTap interactions=${interactionCount - interactionsAtFirstTap}")
        }

        if (isSecondTap && interactionCount == interactionsAtFirstTap) {
            val expected = interactionsAtFirstTap
            lastTapTime = 0L // a third tap starts a fresh pair
            handler.postDelayed({
                // Still nothing clicked or scrolled, and still on the home screen.
                if (interactionCount == expected && onHomeScreen) {
                    if (debug) Log.d(TAG, "double tap on empty space -> lock")
                    lockScreen()
                }
            }, SETTLE_MS)
        } else {
            lastTapTime = time
            interactionsAtFirstTap = interactionCount
        }
    }

    // ---- Status bar strip -----------------------------------------------------------------

    @SuppressLint("ClickableViewAccessibility")
    private fun showStatusStrip() {
        if (statusStrip != null) return
        val wm = windowManager ?: return

        val view = View(this) // fully transparent
        val config = ViewConfiguration.get(this)
        val slop = config.scaledTouchSlop
        val doubleTapSlop = config.scaledDoubleTapSlop
        val longPressMs = ViewConfiguration.getLongPressTimeout().toLong()

        // Double tap detection is done by hand (instead of GestureDetector) so the time
        // between the taps can follow the slider setting.
        var downX = 0f
        var downY = 0f
        var downTime = 0L
        var moved = false
        var swipeHandled = false
        var firstTapTime = 0L // down time of a finished clean tap; 0 = none waiting
        var firstTapX = 0f
        var firstTapY = 0f

        view.setOnTouchListener { v, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = ev.rawX
                    downY = ev.rawY
                    downTime = ev.eventTime
                    moved = false
                    swipeHandled = false

                    val gap = ev.eventTime - firstTapTime
                    val near = Math.hypot((downX - firstTapX).toDouble(), (downY - firstTapY).toDouble()) <= doubleTapSlop
                    if (firstTapTime != 0L && gap >= MIN_TAP_GAP_MS && gap <= doubleTapMs() && near) {
                        firstTapTime = 0L
                        swipeHandled = true // the rest of this touch does nothing
                        lockScreen()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (Math.hypot((ev.rawX - downX).toDouble(), (ev.rawY - downY).toDouble()) > slop) {
                        moved = true
                    }
                    if (!swipeHandled && ev.rawY - downY > slop * 2) {
                        swipeHandled = true
                        val rightSide = downX > v.resources.displayMetrics.widthPixels / 2f
                        performGlobalAction(
                            if (rightSide) GLOBAL_ACTION_QUICK_SETTINGS else GLOBAL_ACTION_NOTIFICATIONS
                        )
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved && !swipeHandled && ev.eventTime - downTime < longPressMs) {
                        firstTapTime = downTime
                        firstTapX = downX
                        firstTapY = downY
                    } else if (!swipeHandled) {
                        firstTapTime = 0L
                    }
                }
                MotionEvent.ACTION_CANCEL -> firstTapTime = 0L
            }
            true
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            statusBarHeight(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }

        try {
            wm.addView(view, params)
            statusStrip = view
        } catch (e: RuntimeException) {
            statusStrip = null // the system refused the window; nothing else to do
        }
    }

    private fun hideStatusStrip() {
        val view = statusStrip ?: return
        statusStrip = null
        try {
            windowManager?.removeView(view)
        } catch (e: IllegalArgumentException) {
            // already gone
        }
    }

    private fun statusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) {
            resources.getDimensionPixelSize(id)
        } else {
            (24 * resources.displayMetrics.density).toInt()
        }
    }

    // ---- Helpers --------------------------------------------------------------------------

    /**
     * The launcher and keyboard package lists almost never change, but they used to be looked up
     * from the system on every window change. Keep them for a couple of minutes instead.
     * (After installing a new launcher or enabling a new keyboard it can take up to this long
     * to be recognised.)
     */
    private fun ensurePackageSets() {
        val now = SystemClock.elapsedRealtime()
        if (setsLoadedAt != 0L && now - setsLoadedAt < PACKAGE_CACHE_MS) return
        launcherSet = launcherPackages()
        imeSet = imePackages()
        setsLoadedAt = now
    }

    @Suppress("DEPRECATION")
    private fun launcherPackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }
            .toSet()
    }

    private fun imePackages(): Set<String> {
        val imm = getSystemService(InputMethodManager::class.java) ?: return emptySet()
        return imm.enabledInputMethodList.map { it.packageName }.toSet()
    }
}
