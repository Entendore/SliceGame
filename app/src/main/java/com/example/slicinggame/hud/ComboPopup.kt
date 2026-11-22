package com.example.slicinggame.hud

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

class ComboPopup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722")
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(10f, 0f, 0f, Color.parseColor("#000000"))
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private var multiplier = 0
    private var scale = 0f
    private var alphaF = 0f
    private var yOffset = 0f

    init { setLayerType(LAYER_TYPE_SOFTWARE, null); visibility = GONE }

    fun show(mult: Int) {
        if (mult < 2) return
        multiplier = mult
        visibility = VISIBLE
        scale = 0f; alphaF = 0f; yOffset = 0f

        val scaleAnim = ValueAnimator.ofFloat(0f, 1.3f, 1f).apply {
            duration = 500; interpolator = OvershootInterpolator(2f)
            addUpdateListener { scale = it.animatedValue as Float; invalidate() }
        }
        val alphaAnim = ValueAnimator.ofFloat(0f, 1f, 1f, 0f).apply {
            duration = 1500
            addUpdateListener { alphaF = it.animatedValue as Float; invalidate() }
        }
        val yAnim = ValueAnimator.ofFloat(0f, -100f).apply {
            duration = 1500; interpolator = DecelerateInterpolator()
            addUpdateListener { yOffset = it.animatedValue as Float; invalidate() }
        }
        AnimatorSet().apply { playTogether(scaleAnim, alphaAnim, yAnim); start() }
        postDelayed({ visibility = GONE }, 1500)
    }

    override fun onDraw(canvas: Canvas) {
        if (multiplier < 2) return
        val cx = width / 2f
        val cy = height / 2f + yOffset
        canvas.save()
        canvas.scale(scale, scale, cx, cy)
        canvas.translate(0f, yOffset)
        textPaint.alpha = (alphaF * 255).toInt()
        strokePaint.alpha = (alphaF * 255).toInt()
        textPaint.textSize = 120f * scale
        strokePaint.textSize = 120f * scale
        val text = "${multiplier}x COMBO!"
        canvas.drawText(text, cx, cy, strokePaint)
        canvas.drawText(text, cx, cy, textPaint)
        canvas.restore()
    }
}
