package com.example.slicinggame

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0F1E"))
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "YOUR STATISTICS"
            setTextColor(Color.parseColor("#FFD700")); textSize = 32f
            gravity = Gravity.CENTER; setPadding(0, 0, 0, 32)
        }
        root.addView(title)

        val scoreRepo = ScoreRepository(this)
        val stats = listOf(
            "🏆 High Score" to scoreRepo.getHighScore().toString(),
            "⭐ Total Stars" to "${scoreRepo.getTotalStars()} / 18",
            "🎯 Levels Completed" to "${Settings.levelsCompleted} / 6",
            "🔪 Total Slices" to Settings.totalSlices.toString(),
            "🔥 Total Combos" to Settings.totalCombos.toString(),
            "📅 Daily Best" to Settings.dailyChallengeScore.toString(),
            "🏅 Achievements" to "${AchievementManager.getUnlockedCount()} / ${AchievementManager.achievements.size}"
        )

        for ((label, value) in stats) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 20, 16, 20)
                setBackgroundColor(Color.parseColor("#2A2A4E"))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = 12
                layoutParams = lp
            }
            row.addView(TextView(this).apply {
                text = label; setTextColor(Color.WHITE); textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            row.addView(TextView(this).apply {
                text = value
                setTextColor(Color.parseColor("#FFD700")); textSize = 22f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            root.addView(row)
        }

        setContentView(root)
    }
}
