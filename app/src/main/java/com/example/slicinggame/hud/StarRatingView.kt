package com.example.slicinggame.hud

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator

class StarRatingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E"); alpha = 200
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#444444") }
    private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        setShadowLayer(8f, 0f, 0f, Color.parseColor("#FFD70088"))
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700"); style = Paint.Style.STROKE; strokeWidth = 2f
    }

    private var filledStars = 0f
    private val starPath = Path()
    private val bgRect = RectF()
    private val bgPath = Path()

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) }

    fun setStars(count: Int) {
        val target = count.toFloat().coerceIn(0f, 3f)
        ValueAnimator.ofFloat(filledStars, target).apply {
            duration = 500
            interpolator = OvershootInterpolator(1.5f)
            addUpdateListener { filledStars = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val scale = resources.displayMetrics.density
        setMeasuredDimension((200 * scale).toInt(), (80 * scale).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val r = h / 2
        bgRect.set(0f, 0f, w, h)
        bgPath.reset()
        bgPath.addRoundRect(bgRect, r, r, Path.Direction.CW)
        canvas.drawPath(bgPath, bgPaint)
        canvas.drawPath(bgPath, borderPaint)

        val starSize = h * 0.55f
        val spacing = w / 4f
        for (i in 0 until 3) {
            val cx = spacing * (i + 1)
            val cy = h / 2f
            val fillAmount = (filledStars - i).coerceIn(0f, 1f)
            drawStar(canvas, cx, cy, starSize, fillAmount)
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, size: Float, fill: Float) {
        starPath.reset()
        val outerR = size / 2f
        val innerR = outerR * 0.45f
        for (i in 0 until 10) {
            val angle = Math.PI * i / 5 - Math.PI / 2
            val r = if (i % 2 == 0) outerR else innerR
            val x = cx + r * kotlin.math.cos(angle).toFloat()
            val y = cy + r * kotlin.math.sin(angle).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()
        canvas.drawPath(starPath, emptyPaint)
        if (fill > 0f) {
            canvas.save()
            canvas.clipRect(cx - outerR, cy - outerR,
                cx - outerR + outerR * 2 * fill, cy + outerR)
            canvas.drawPath(starPath, filledPaint)
            canvas.restore()
        }
    }
}
