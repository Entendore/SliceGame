package com.example.slicinggame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.slicinggame.hud.GameHud

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var hud: GameHud
    private lateinit var levelLabel: TextView
    private lateinit var root: FrameLayout
    private lateinit var tutorial: TutorialManager
    private var startLevel = 0
    private var isDailyChallenge = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings.init(this)
        AppContext.instance

        startLevel = intent.getIntExtra("level", 0)
        isDailyChallenge = intent.getStringExtra("mode") == "daily"

        root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#0F0F1E"))
            fitsSystemWindows = true
        }

        gameView = GameView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT)
        }
        root.addView(gameView)

        hud = GameHud(root, this)

        levelLabel = TextView(this).apply {
            text = if (isDailyChallenge) "DAILY CHALLENGE" else "LEVEL 1"
            setTextColor(Color.parseColor("#AAAAAA")); textSize = 14f
            setPadding(dp(16), 0, 0, 0)
        }
        root.addView(levelLabel, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.START
            topMargin = dp(120)
        })

        tutorial = TutorialManager(root)
        wireCallbacks()
        setContentView(root)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !Settings.tutorialCompleted && !isDailyChallenge) {
            tutorial.start { }
        }
    }

    private fun wireCallbacks() {
        hud.pauseButton.onClick = {
            val newPaused = !gameView.isPaused()
            gameView.setPaused(newPaused)
            hud.pauseButton.icon = if (newPaused) "▶" else "⏸"
        }
        hud.undoButton.onClick = {
            gameView.undoSlice()
            val p = getSharedPreferences("slice_stats", MODE_PRIVATE)
            p.edit().putInt("undos_used", p.getInt("undos_used", 0) + 1).apply()
        }
        hud.slowButton.onClick = {
            gameView.activateSlowMotion()
            hud.slowButton.setCooldownAnimated(1f, 5000)
            val p = getSharedPreferences("slice_stats", MODE_PRIVATE)
            p.edit().putInt("slows_used", p.getInt("slows_used", 0) + 1).apply()
        }

        gameView.setOnLevelChange { level, complete, score, slices, stars, combo, mult ->
            runOnUiThread {
                val lvlName = gameView.getLevelName(level).uppercase()
                levelLabel.text = if (isDailyChallenge) "DAILY CHALLENGE"
                                  else "LEVEL ${level + 1} · $lvlName"
                hud.scoreCounter.setScore(score)
                hud.sliceMeter.setSlices(slices, gameView.getLevelOptimal(level))
                if (complete) {
                    hud.starRating.setStars(stars)
                    root.postDelayed({
                        if (isDailyChallenge) {
                            Settings.dailyChallengeScore = score.coerceAtLeast(Settings.dailyChallengeScore)
                        }
                        startActivity(Intent(this, LevelSelectActivity::class.java))
                        finish()
                    }, 2500)
                }
            }
        }

        gameView.setOnPowerUpChange { undosLeft, slowsLeft, slowActive ->
            runOnUiThread {
                hud.undoButton.usesLeft = undosLeft
                hud.undoButton.isEnabled = undosLeft > 0
                hud.slowButton.usesLeft = slowsLeft
                hud.slowButton.isActive = slowActive
                hud.slowButton.isEnabled = slowsLeft > 0 && !slowActive
            }
        }

        gameView.setOnCombo { mult ->
            runOnUiThread { hud.comboPopup.show(mult) }
        }

        gameView.setOnHighScoreChange { }
    }

    override fun onResume() {
        super.onResume()
        gameView.startSensors()
        gameView.setDisplayRotation(windowManager.defaultDisplay.rotation)
        gameView.setPaused(false)
        hud.pauseButton.icon = "⏸"

        if (isDailyChallenge) {
            root.post {
                gameView.loadDaily(
                    gameView.width.toFloat().coerceAtLeast(1f),
                    gameView.height.toFloat().coerceAtLeast(1f)
                )
            }
        } else if (startLevel > 0) {
            root.post {
                repeat(startLevel) { gameView.nextLevel() }
                startLevel = 0
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameView.stopSensors()
        gameView.setPaused(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.soundManager.release()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
