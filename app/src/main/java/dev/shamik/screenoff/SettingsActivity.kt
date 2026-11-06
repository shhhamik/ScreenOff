package dev.shamik.screenoff

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

/**
 * The one and only screen of the app: every option lives here as a switch or slider.
 * It has no launcher entry; it is reached from Settings > Accessibility > Screen Off
 * (the "Settings" entry there is added by Android and opens this screen), from the Quick
 * Settings tile when the service is off, or with `adb shell am start`.
 */
class SettingsActivity : Activity() {

    private class Palette(
        val bg: Int,
        val card: Int,
        val pill: Int,
        val pillText: Int,
        val text: Int,
        val subText: Int,
        val accent: Int
    )

    private class SwitchRow(val view: View, val toggle: Switch)

    private lateinit var prefs: SharedPreferences
    private lateinit var palette: Palette
    private lateinit var serviceSwitch: Switch
    private lateinit var serviceSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)
        prefs = Prefs.get(this)
        palette = buildPalette()

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(32))
        }

        column.addView(TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 34f
            setTextColor(palette.text)
            setPadding(dp(12), dp(16), dp(12), dp(24))
        })

        column.addView(buildServiceCard())

        // ---- Triggers ----
        val homeRow = switchRow(R.string.home_title, R.string.home_summary, Prefs.KEY_HOME_DOUBLE_TAP, true)
        val onlyHomeRow = switchRow(R.string.only_home_title, R.string.only_home_summary, Prefs.KEY_ONLY_HOME, true)
        val statusRow = switchRow(
            R.string.status_bar_title, R.string.status_bar_summary, Prefs.KEY_STATUS_BAR, true
        ) { on -> setRowEnabled(onlyHomeRow.view, on) }
        setRowEnabled(onlyHomeRow.view, statusRow.toggle.isChecked)

        column.addView(sectionHeader(R.string.section_triggers))
        column.addView(group(homeRow.view, statusRow.view, onlyHomeRow.view))

        // ---- Double tap time ----
        val timeRow = sliderRow(
            R.string.tap_time_title, R.string.tap_time_summary, Prefs.KEY_DOUBLE_TAP_MS,
            Prefs.TAP_MS_MIN, Prefs.TAP_MS_MAX, Prefs.TAP_MS_STEP, Prefs.TAP_MS_DEFAULT,
            { getString(R.string.unit_ms, it) }
        )
        column.addView(sectionHeader(R.string.section_timing))
        column.addView(group(timeRow))

        // ---- Vibration ----
        val hasVibrator = Haptics.hasVibrator(this)
        val hasStrength = Haptics.hasAmplitudeControl(this)

        val previewIfOn: () -> Unit = { if (prefs.getBoolean(Prefs.KEY_VIBRATE, Prefs.DEFAULT_VIBRATE)) Haptics.preview(this) }

        val strengthRow = if (hasStrength) {
            sliderRow(
                R.string.vibrate_strength_title, null, Prefs.KEY_VIBRATE_STRENGTH,
                Prefs.STRENGTH_MIN, Prefs.STRENGTH_MAX, Prefs.STRENGTH_STEP, Prefs.STRENGTH_DEFAULT,
                { getString(R.string.unit_percent, it) }, previewIfOn
            )
        } else null
        val lengthRow = sliderRow(
            R.string.vibrate_length_title, null, Prefs.KEY_VIBRATE_MS,
            Prefs.VIBRATE_MS_MIN, Prefs.VIBRATE_MS_MAX, Prefs.VIBRATE_MS_STEP, Prefs.VIBRATE_MS_DEFAULT,
            { getString(R.string.unit_ms, it) }, previewIfOn
        )
        val patternRow = choiceRow(
            R.string.vibrate_pattern_title, Prefs.KEY_VIBRATE_PATTERN,
            intArrayOf(R.string.pattern_single, R.string.pattern_double)
        )
        val testRow = actionRow(R.string.vibrate_test_title, R.string.vibrate_test_summary) {
            Haptics.preview(this)
        }

        val vibrateSubRows = listOfNotNull(strengthRow, lengthRow, patternRow, testRow)
        val vibrateRow = switchRow(
            R.string.vibrate_title,
            if (hasVibrator) R.string.vibrate_summary else R.string.vibrate_unsupported,
            Prefs.KEY_VIBRATE, Prefs.DEFAULT_VIBRATE
        ) { on -> vibrateSubRows.forEach { setRowEnabled(it, on && hasVibrator) } }
        if (!hasVibrator) setRowEnabled(vibrateRow.view, false)
        vibrateSubRows.forEach { setRowEnabled(it, hasVibrator && vibrateRow.toggle.isChecked) }

        column.addView(sectionHeader(R.string.section_vibration))
        column.addView(group(*(listOf(vibrateRow.view) + vibrateSubRows).toTypedArray()))

        column.addView(TextView(this).apply {
            text = getString(R.string.tile_hint)
            textSize = 14f
            setTextColor(palette.subText)
            setPadding(dp(12), dp(24), dp(12), 0)
        })

        column.addView(TextView(this).apply {
            text = getString(R.string.about_text, versionName())
            textSize = 12f
            setTextColor(palette.subText)
            setPadding(dp(12), dp(16), dp(12), 0)
        })

        val scroll = ScrollView(this).apply {
            setBackgroundColor(palette.bg)
            addView(column)
            // Targeting API 35+ draws edge-to-edge, so keep content clear of the system bars.
            setOnApplyWindowInsetsListener { v, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
        }
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        val running = LockService.instance != null
        serviceSwitch.isChecked = running
        serviceSummary.setText(if (running) R.string.status_on else R.string.status_off)
    }

    // ---- Building blocks ----------------------------------------------------------------------

    /** Big rounded card at the top, like the "Use Screen Off" card on the Accessibility page. */
    private fun buildServiceCard(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = dp(88)
            setPadding(dp(24), dp(20), dp(20), dp(20))
            background = GradientDrawable().apply {
                setColor(palette.pill)
                cornerRadius = dp(36).toFloat()
            }
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }

        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(TextView(this).apply {
            text = getString(R.string.service_title)
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(palette.pillText)
        })
        serviceSummary = TextView(this).apply {
            textSize = 14f
            setTextColor(palette.pillText)
        }
        texts.addView(serviceSummary)

        serviceSwitch = Switch(this).apply {
            // Only shows the state; the whole card opens Accessibility settings.
            isClickable = false
            isFocusable = false
        }

        card.addView(texts, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        card.addView(serviceSwitch)
        card.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return card
    }

    private fun sectionHeader(textRes: Int): TextView = TextView(this).apply {
        text = getString(textRes)
        textSize = 14f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(palette.accent)
        setPadding(dp(12), dp(28), dp(12), dp(8))
    }

    /** Stacks rows as separate rounded segments with a small gap, like the Accessibility page. */
    private fun group(vararg rows: View): LinearLayout {
        val outer = dp(24).toFloat()
        val inner = dp(4).toFloat()
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        rows.forEachIndexed { i, row ->
            val top = if (i == 0) outer else inner
            val bottom = if (i == rows.lastIndex) outer else inner
            row.background = GradientDrawable().apply {
                setColor(palette.card)
                cornerRadii = floatArrayOf(top, top, top, top, bottom, bottom, bottom, bottom)
            }
            row.clipToOutline = true
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (i > 0) lp.topMargin = dp(2)
            container.addView(row, lp)
        }
        return container
    }

    private fun rowContainer(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = dp(72)
        setPadding(dp(20), dp(14), dp(20), dp(14))
    }

    private fun textBlock(titleRes: Int, summaryRes: Int?): LinearLayout {
        val block = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        block.addView(TextView(this).apply {
            text = getString(titleRes)
            textSize = 17f
            setTextColor(palette.text)
        })
        if (summaryRes != null) {
            block.addView(TextView(this).apply {
                text = getString(summaryRes)
                textSize = 14f
                setTextColor(palette.subText)
            })
        }
        return block
    }

    private fun switchRow(
        titleRes: Int,
        summaryRes: Int,
        key: String,
        default: Boolean,
        onChange: (Boolean) -> Unit = {}
    ): SwitchRow {
        val toggle = Switch(this).apply { isChecked = prefs.getBoolean(key, default) }
        val row = rowContainer()
        row.addView(
            textBlock(titleRes, summaryRes),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        row.addView(toggle)
        toggle.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(key, checked).apply()
            onChange(checked)
        }
        row.setOnClickListener { toggle.toggle() }
        return SwitchRow(row, toggle)
    }

    /** Title on top, slider with its current value on the right, like the reference screenshot. */
    private fun sliderRow(
        titleRes: Int,
        summaryRes: Int?,
        key: String,
        lo: Int,
        hi: Int,
        step: Int,
        default: Int,
        format: (Int) -> String,
        onStopTracking: () -> Unit = {}
    ): View {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(10))
        }
        column.addView(textBlock(titleRes, summaryRes))

        val current = prefs.getInt(key, default).coerceIn(lo, hi)
        val value = TextView(this).apply {
            text = format(current)
            textSize = 15f
            minWidth = dp(72)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setTextColor(palette.subText)
        }
        val seek = SeekBar(this).apply {
            max = (hi - lo) / step
            progress = (current - lo) / step
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                    val v = lo + progress * step
                    value.text = format(v)
                    if (fromUser) prefs.edit().putInt(key, v).apply()
                }

                override fun onStartTrackingTouch(bar: SeekBar) {}

                override fun onStopTrackingTouch(bar: SeekBar) = onStopTracking()
            })
        }

        val line = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        line.addView(seek, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        line.addView(value)
        column.addView(line)
        return column
    }

    /** A row that shows its current choice and opens a pick-one dialog. */
    private fun choiceRow(titleRes: Int, key: String, optionRes: IntArray): View {
        val labels = optionRes.map { getString(it) }.toTypedArray()
        val row = rowContainer()
        val block = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        block.addView(TextView(this).apply {
            text = getString(titleRes)
            textSize = 17f
            setTextColor(palette.text)
        })
        val summary = TextView(this).apply {
            textSize = 14f
            setTextColor(palette.subText)
            text = labels[prefs.getInt(key, 0).coerceIn(0, labels.lastIndex)]
        }
        block.addView(summary)
        row.addView(block, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        row.setOnClickListener {
            val selected = prefs.getInt(key, 0).coerceIn(0, labels.lastIndex)
            AlertDialog.Builder(this)
                .setTitle(titleRes)
                .setSingleChoiceItems(labels, selected) { dialog, which ->
                    prefs.edit().putInt(key, which).apply()
                    summary.text = labels[which]
                    dialog.dismiss()
                    Haptics.preview(this)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        return row
    }

    private fun actionRow(titleRes: Int, summaryRes: Int, onClick: () -> Unit): View {
        val row = rowContainer()
        row.addView(
            textBlock(titleRes, summaryRes),
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        row.setOnClickListener { onClick() }
        return row
    }

    /** Greys a row out and stops it reacting to touches. */
    private fun setRowEnabled(row: View, enabled: Boolean) {
        row.alpha = if (enabled) 1f else 0.38f
        setEnabledDeep(row, enabled)
    }

    private fun setEnabledDeep(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) setEnabledDeep(view.getChildAt(i), enabled)
        }
    }

    // ---- Colours ------------------------------------------------------------------------------

    private fun buildPalette(): Palette {
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        if (Build.VERSION.SDK_INT >= 31) {
            // Material You colours, so it matches the wallpaper-based theme of the phone.
            fun c(id: Int) = getColor(id)
            return if (dark) {
                Palette(
                    bg = c(android.R.color.system_neutral1_900),
                    card = c(android.R.color.system_neutral1_800),
                    pill = c(android.R.color.system_accent1_700),
                    pillText = c(android.R.color.system_accent1_100),
                    text = c(android.R.color.system_neutral1_100),
                    subText = c(android.R.color.system_neutral2_200),
                    accent = c(android.R.color.system_accent1_200)
                )
            } else {
                Palette(
                    bg = c(android.R.color.system_neutral1_10),
                    card = c(android.R.color.system_neutral1_50),
                    pill = c(android.R.color.system_accent1_100),
                    pillText = c(android.R.color.system_accent1_900),
                    text = c(android.R.color.system_neutral1_900),
                    subText = c(android.R.color.system_neutral2_600),
                    accent = c(android.R.color.system_accent1_600)
                )
            }
        }
        // Android 11: no dynamic colours, use a fixed teal palette.
        return if (dark) {
            Palette(
                bg = 0xFF141D1D.toInt(), card = 0xFF222E2E.toInt(),
                pill = 0xFF1F5557.toInt(), pillText = 0xFFB2EEEF.toInt(),
                text = 0xFFE1E3E3.toInt(), subText = 0xFFBEC8C8.toInt(),
                accent = 0xFF8BCFD1.toInt()
            )
        } else {
            Palette(
                bg = 0xFFF7FAFA.toInt(), card = 0xFFE8EFEF.toInt(),
                pill = 0xFFBFEBEC.toInt(), pillText = 0xFF002021.toInt(),
                text = 0xFF191C1C.toInt(), subText = 0xFF3F4949.toInt(),
                accent = 0xFF006A6B.toInt()
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun versionName(): String = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: ""
    } catch (e: Exception) {
        ""
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
