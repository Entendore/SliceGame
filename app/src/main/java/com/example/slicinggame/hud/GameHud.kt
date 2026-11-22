package com.example.slicinggame.hud

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout

class GameHud(private val container: FrameLayout, context: Context) {

    val scoreCounter = ScoreCounterView(context)
    val starRating = StarRatingView(context)
    val sliceMeter = SliceMeterView(context)
    val undoButton = PowerUpButton(context).apply { icon = "↶" }
    val slowButton = PowerUpButton(context).apply { icon = "⏱" }
    val pauseButton = PowerUpButton(context).apply { icon = "⏸"; usesLeft = -1 }
    val comboPopup = ComboPopup(context)

    init {
        val pad = dp(16)

        container.addView(scoreCounter, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.START
            setMargins(pad, pad, 0, 0)
        })

        container.addView(starRating, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, pad, pad, 0)
        })

        container.addView(sliceMeter, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = pad + dp(120)
        })

        container.addView(pauseButton, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = pad + dp(90)
            rightMargin = pad
        })

        val bottomRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        bottomRow.addView(undoButton)
        bottomRow.addView(android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(40), 1)
        })
        bottomRow.addView(slowButton)
        container.addView(bottomRow, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(32)
        })

        container.addView(comboPopup, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, dp(200)).apply {
            gravity = Gravity.CENTER
        })
    }

    private fun dp(v: Int): Int = (v * container.resources.displayMetrics.density).toInt()
}
