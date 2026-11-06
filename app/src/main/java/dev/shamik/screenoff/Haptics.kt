package dev.shamik.screenoff

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Short vibration feedback when the screen is turned off. Uses only the VIBRATE permission. */
object Haptics {

    /** Pause between the two buzzes of the "double" pattern. */
    private const val DOUBLE_GAP_MS = 60L

    fun hasVibrator(context: Context): Boolean = vibrator(context)?.hasVibrator() == true

    fun hasAmplitudeControl(context: Context): Boolean =
        vibrator(context)?.hasAmplitudeControl() == true

    /**
     * Plays the effect if the Vibrate switch is on.
     * Returns how long the effect lasts in milliseconds, or 0 if nothing was played.
     */
    fun play(context: Context): Long {
        val prefs = Prefs.get(context)
        if (!prefs.getBoolean(Prefs.KEY_VIBRATE, Prefs.DEFAULT_VIBRATE)) return 0L
        return vibrate(context, prefs)
    }

    /** Plays the effect with the current settings, regardless of the Vibrate switch. */
    fun preview(context: Context): Long = vibrate(context, Prefs.get(context))

    private fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= 31) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        }

    @Suppress("DEPRECATION")
    private fun vibrate(context: Context, prefs: SharedPreferences): Long {
        val v = vibrator(context) ?: return 0L
        if (!v.hasVibrator()) return 0L

        val ms = Prefs.vibrateMs(prefs).toLong()
        val double = prefs.getInt(Prefs.KEY_VIBRATE_PATTERN, Prefs.PATTERN_SINGLE) == Prefs.PATTERN_DOUBLE
        val strength = v.hasAmplitudeControl()
        val amp = if (strength) Prefs.vibrateAmplitude(prefs) else VibrationEffect.DEFAULT_AMPLITUDE

        val effect: VibrationEffect
        val total: Long
        if (double) {
            val timings = longArrayOf(0L, ms, DOUBLE_GAP_MS, ms)
            effect = if (strength) {
                VibrationEffect.createWaveform(timings, intArrayOf(0, amp, 0, amp), -1)
            } else {
                VibrationEffect.createWaveform(timings, -1)
            }
            total = ms * 2 + DOUBLE_GAP_MS
        } else {
            effect = VibrationEffect.createOneShot(ms, amp)
            total = ms
        }

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        return try {
            v.vibrate(effect, attrs)
            total
        } catch (e: RuntimeException) {
            0L
        }
    }
}
