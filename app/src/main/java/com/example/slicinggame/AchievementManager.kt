package com.example.slicinggame

import android.content.Context

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val check: () -> Boolean
)

object AchievementManager {
    private val repo: ScoreRepository by lazy { ScoreRepository(AppContext.ctx) }
    private val statsPrefs by lazy {
        AppContext.ctx.getSharedPreferences("slice_stats", Context.MODE_PRIVATE)
    }

    val achievements = listOf(
        Achievement("first_slice", "First Cut", "Make your first slice", "🔪",
            { Settings.totalSlices >= 1 }),
        Achievement("centurion", "Centurion", "Make 100 slices", "💯",
            { Settings.totalSlices >= 100 }),
        Achievement("combo_master", "Combo Master", "Land a 4x combo", "🔥",
            { Settings.totalCombos >= 1 }),
        Achievement("perfectionist", "Perfectionist", "Get 3 stars on any level", "⭐",
            { repo.getTotalStars() >= 3 }),
        Achievement("star_collector", "Star Collector", "Earn 10 stars total", "🌟",
            { repo.getTotalStars() >= 10 }),
        Achievement("star_hoarder", "Star Hoarder", "Earn 30 stars total", "✨",
            { repo.getTotalStars() >= 30 }),
        Achievement("level_5", "Halfway There", "Complete 5 levels", "🎯",
            { Settings.levelsCompleted >= 5 }),
        Achievement("completionist", "Completionist", "Complete all levels", "🏆",
            { Settings.levelsCompleted >= 6 }),
        Achievement("undo_user", "Safety Net", "Use undo 10 times", "↶",
            { statsPrefs.getInt("undos_used", 0) >= 10 }),
        Achievement("slow_mo", "Time Lord", "Use slow motion 10 times", "⏱",
            { statsPrefs.getInt("slows_used", 0) >= 10 }),
        Achievement("daily_warrior", "Daily Warrior", "Complete a daily challenge", "📅",
            { Settings.dailyChallengeScore > 0 }),
        Achievement("no_undo", "No Safety", "Complete a level without using undo", "🎖",
            { statsPrefs.getBoolean("clean_win", false) })
    )

    private val unlockPrefs by lazy {
        AppContext.ctx.getSharedPreferences("slice_achievements", Context.MODE_PRIVATE)
    }

    fun isUnlocked(id: String): Boolean = unlockPrefs.getBoolean(id, false)

    fun checkAll(): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        for (a in achievements) {
            if (!isUnlocked(a.id) && a.check()) {
                unlockPrefs.edit().putBoolean(a.id, true).apply()
                newlyUnlocked.add(a)
            }
        }
        return newlyUnlocked
    }

    fun getUnlockedCount(): Int = achievements.count { isUnlocked(it.id) }
}
