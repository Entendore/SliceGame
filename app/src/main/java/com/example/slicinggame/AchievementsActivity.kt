package com.example.slicinggame

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AchievementsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0F1E"))
            setPadding(24, 48, 24, 24)
        }

        val unlocked = AchievementManager.getUnlockedCount()
        val title = TextView(this).apply {
            text = "ACHIEVEMENTS  $unlocked/${AchievementManager.achievements.size}"
            setTextColor(Color.parseColor("#FFD700")); textSize = 28f
            gravity = Gravity.CENTER; setPadding(0, 0, 0, 24)
        }
        root.addView(title)

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        for (a in AchievementManager.achievements) {
            val isUnlocked = AchievementManager.isUnlocked(a.id)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(if (isUnlocked) Color.parseColor("#2A2A4E")
                                   else Color.parseColor("#1A1A2E"))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = 12
                layoutParams = lp
            }
            val icon = TextView(this).apply {
                text = a.icon; textSize = 40f
                setPadding(0, 0, 16, 0)
                alpha = if (isUnlocked) 1f else 0.3f
            }
            row.addView(icon)

            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            info.addView(TextView(this).apply {
                text = a.title
                setTextColor(if (isUnlocked) Color.parseColor("#FFD700")
                             else Color.parseColor("#888888"))
                textSize = 20f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            info.addView(TextView(this).apply {
                text = a.description
                setTextColor(Color.parseColor("#AAAAAA")); textSize = 14f
            })
            row.addView(info)

            val status = TextView(this).apply {
                text = if (isUnlocked) "✓" else "🔒"
                textSize = 28f
            }
            row.addView(status)

            list.addView(row)
        }

        scroll.addView(list)
        root.addView(scroll)
        setContentView(root)
    }
}
