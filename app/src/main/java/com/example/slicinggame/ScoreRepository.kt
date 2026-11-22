package com.example.slicinggame

import android.content.Context
import android.content.SharedPreferences

class ScoreRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("slice_game_scores", Context.MODE_PRIVATE)

    fun getHighScore(): Int = prefs.getInt("high_score", 0)
    fun setHighScore(score: Int) {
        if (score > getHighScore()) prefs.edit().putInt("high_score", score).apply()
    }

    fun getBestStars(level: Int): Int = prefs.getInt("stars_$level", 0)
    fun setBestStars(level: Int, stars: Int) {
        if (stars > getBestStars(level)) prefs.edit().putInt("stars_$level", stars).apply()
    }

    fun getTotalStars(): Int {
        var total = 0
        for (key in prefs.all.keys) {
            if (key.startsWith("stars_")) total += (prefs.all[key] as? Int) ?: 0
        }
        return total
    }

    fun reset() { prefs.edit().clear().apply() }
}
