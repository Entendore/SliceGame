package com.example.slicinggame

import android.content.Context
import android.content.SharedPreferences

object Settings {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("slice_settings", Context.MODE_PRIVATE)
    }

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound", true)
        set(value) = prefs.edit().putBoolean("sound", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(value) = prefs.edit().putBoolean("haptics", value).apply()

    var sensitivity: Float
        get() = prefs.getFloat("sensitivity", 1.0f)
        set(value) = prefs.edit().putFloat("sensitivity", value).apply()

    var tutorialCompleted: Boolean
        get() = prefs.getBoolean("tutorial_done", false)
        set(value) = prefs.edit().putBoolean("tutorial_done", value).apply()

    var lastDailyChallenge: Long
        get() = prefs.getLong("daily_last", 0L)
        set(value) = prefs.edit().putLong("daily_last", value).apply()

    var dailyChallengeScore: Int
        get() = prefs.getInt("daily_score", 0)
        set(value) = prefs.edit().putInt("daily_score", value).apply()

    var totalSlices: Int
        get() = prefs.getInt("total_slices", 0)
        set(value) = prefs.edit().putInt("total_slices", value).apply()

    var totalCombos: Int
        get() = prefs.getInt("total_combos", 0)
        set(value) = prefs.edit().putInt("total_combos", value).apply()

    var levelsCompleted: Int
        get() = prefs.getInt("levels_completed", 0)
        set(value) = prefs.edit().putInt("levels_completed", value).apply()

    fun resetAll() { prefs.edit().clear().apply() }
}
