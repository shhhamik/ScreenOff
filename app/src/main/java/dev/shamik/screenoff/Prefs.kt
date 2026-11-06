package dev.shamik.screenoff

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    // Triggers
    const val KEY_HOME_DOUBLE_TAP = "home_double_tap"
    const val KEY_STATUS_BAR = "status_bar_double_tap"
    const val KEY_ONLY_HOME = "only_on_home"

    // Double-tap timing (milliseconds between the two taps)
    const val KEY_DOUBLE_TAP_MS = "double_tap_ms"
    const val TAP_MS_MIN = 100
    const val TAP_MS_MAX = 600
    const val TAP_MS_STEP = 10
    const val TAP_MS_DEFAULT = 300

    // Vibration
    const val KEY_VIBRATE = "vibrate"
    const val KEY_VIBRATE_STRENGTH = "vibrate_strength" // percent
    const val KEY_VIBRATE_MS = "vibrate_ms"
    const val KEY_VIBRATE_PATTERN = "vibrate_pattern"

    const val DEFAULT_VIBRATE = false

    const val STRENGTH_MIN = 10
    const val STRENGTH_MAX = 100
    const val STRENGTH_STEP = 5
    const val STRENGTH_DEFAULT = 60

    const val VIBRATE_MS_MIN = 10
    const val VIBRATE_MS_MAX = 200
    const val VIBRATE_MS_STEP = 5
    const val VIBRATE_MS_DEFAULT = 40

    const val PATTERN_SINGLE = 0
    const val PATTERN_DOUBLE = 1

    fun get(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun doubleTapMs(p: SharedPreferences): Int =
        p.getInt(KEY_DOUBLE_TAP_MS, TAP_MS_DEFAULT).coerceIn(TAP_MS_MIN, TAP_MS_MAX)

    fun vibrateMs(p: SharedPreferences): Int =
        p.getInt(KEY_VIBRATE_MS, VIBRATE_MS_DEFAULT).coerceIn(VIBRATE_MS_MIN, VIBRATE_MS_MAX)

    /** Vibration strength as a 1..255 amplitude. */
    fun vibrateAmplitude(p: SharedPreferences): Int {
        val percent = p.getInt(KEY_VIBRATE_STRENGTH, STRENGTH_DEFAULT).coerceIn(STRENGTH_MIN, STRENGTH_MAX)
        return (percent * 255 / 100).coerceIn(1, 255)
    }
}
