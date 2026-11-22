package com.example.slicinggame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LevelSelectActivity : AppCompatActivity() {

    private val scoreRepo by lazy { ScoreRepository(this) }
    private val totalLevels = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0F1E"))
            setPadding(24, 48, 24, 24)
        }

        val title = TextView(this).apply {
            text = "SELECT LEVEL"
            setTextColor(Color.parseColor("#FFD700"))
            textSize = 36f
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD)
        }
        root.addView(title)

        val totalStars = scoreRepo.getTotalStars()
        val starsLabel = TextView(this).apply {
            text = "⭐ $totalStars / ${totalLevels * 3} stars"
            setTextColor(Color.parseColor("#AAAAAA"))
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 32)
        }
        root.addView(starsLabel)

        val dailyBtn = Button(this).apply {
            text = "📅 Daily Challenge"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#00BCD4"))
            setOnClickListener {
                val intent = Intent(this@LevelSelectActivity, MainActivity::class.java)
                intent.putExtra("mode", "daily")
                startActivity(intent)
            }
        }
        root.addView(dailyBtn)

        val scroll = ScrollView(this)
        val grid = GridLayout(this).apply {
            columnCount = 3
            useDefaultMargins = true
            setPadding(0, 24, 0, 24)
        }

        val levels = Level.generate(resources.displayMetrics.widthPixels.toFloat(),
            resources.displayMetrics.heightPixels.toFloat())

        for (i in 0 until totalLevels) {
            val unlocked = i == 0 || Settings.levelsCompleted >= i
            val stars = scoreRepo.getBestStars(i)
            val lvl = levels.getOrNull(i)

            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
                setBackgroundColor(when {
                    !unlocked -> Color.parseColor("#1A1A2E")
                    else -> Color.parseColor("#2A2A4E")
                })
                val lp = GridLayout.LayoutParams().apply {
                    width = 0; height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                layoutParams = lp
            }

            val numText = TextView(this).apply {
                text = if (unlocked) "${i + 1}" else "🔒"
                setTextColor(if (unlocked) Color.parseColor("#FFD700") else Color.parseColor("#555555"))
                textSize = 32f
                gravity = Gravity.CENTER
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.BOLD)
            }
            cell.addView(numText)

            val nameText = TextView(this).apply {
                text = lvl?.name ?: ""
                setTextColor(Color.parseColor("#AAAAAA"))
                textSize = 14f
                gravity = Gravity.CENTER
            }
            cell.addView(nameText)

            val starText = TextView(this).apply {
                text = if (unlocked) "★".repeat(stars) + "☆".repeat(3 - stars) else ""
                setTextColor(Color.parseColor("#FFD700"))
                textSize = 18f
                gravity = Gravity.CENTER
            }
            cell.addView(starText)

            if (unlocked) {
                cell.setOnClickListener {
                    val intent = Intent(this@LevelSelectActivity, MainActivity::class.java)
                    intent.putExtra("level", i)
                    startActivity(intent)
                }
            }
            grid.addView(cell)
        }

        scroll.addView(grid)
        root.addView(scroll)

        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }
        listOf(
            "🏅 Achievements" to AchievementsActivity::class.java,
            "⚙️ Settings" to SettingsActivity::class.java,
            "📊 Stats" to StatsActivity::class.java
        ).forEach { (label, cls) ->
            val btn = Button(this).apply {
                text = label
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#2A2A4E"))
                layoutParams = LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(8, 0, 8, 0)
                }
                setOnClickListener { startActivity(Intent(this@LevelSelectActivity, cls)) }
            }
            navRow.addView(btn)
        }
        root.addView(navRow)

        setContentView(root)
    }
}
