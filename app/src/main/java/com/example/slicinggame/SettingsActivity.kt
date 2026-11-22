package com.example.slicinggame

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0F1E"))
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "SETTINGS"
            setTextColor(Color.parseColor("#FFD700"))
            textSize = 36f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        root.addView(title)

        root.addView(makeSwitchRow("🔊 Sound Effects", Settings.soundEnabled) { Settings.soundEnabled = it })
        root.addView(makeSwitchRow("📳 Haptic Feedback", Settings.hapticsEnabled) { Settings.hapticsEnabled = it })

        val sensLabel = TextView(this).apply {
            text = "🎯 Gravity Sensitivity"
            setTextColor(Color.WHITE); textSize = 20f
            setPadding(0, 32, 0, 8)
        }
        root.addView(sensLabel)

        val sensValue = TextView(this).apply {
            text = "${(Settings.sensitivity * 100).toInt()}%"
            setTextColor(Color.parseColor("#FFD700")); textSize = 18f
            gravity = Gravity.CENTER
        }
        root.addView(sensValue)

        val seek = SeekBar(this).apply {
            max = 200
            progress = (Settings.sensitivity * 100).toInt()
            setPadding(0, 16, 0, 32)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar, p: Int, u: Boolean) {
                    Settings.sensitivity = p / 100f
                    sensValue.text = "$p%"
                }
                override fun onStartTrackingTouch(sb: SeekBar) {}
                override fun onStopTrackingTouch(sb: SeekBar) {}
            })
        }
        root.addView(seek)

        val resetBtn = Button(this).apply {
            text = "🗑️ Reset All Progress"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#F44336"))
            setOnClickListener {
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("Reset Progress?")
                    .setMessage("This will erase all scores, stars, and achievements.")
                    .setPositiveButton("Reset") { _, _ ->
                        Settings.resetAll()
                        ScoreRepository(this@SettingsActivity).reset()
                        finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
        root.addView(resetBtn)

        setContentView(root)
    }

    private fun makeSwitchRow(label: String, initial: Boolean, onChange: (Boolean) -> Unit): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 16, 0, 16)
        }
        val text = TextView(this).apply {
            text = label
            setTextColor(Color.WHITE); textSize = 20f
            layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(text)
        val sw = Switch(this).apply {
            isChecked = initial
            setOnCheckedChangeListener { _, b -> onChange(b) }
        }
        row.addView(sw)
        return row
    }
}
