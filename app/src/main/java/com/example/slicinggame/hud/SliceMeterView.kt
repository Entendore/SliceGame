package com.example.slicinggame.hud

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class SliceMeterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E"); alpha = 200
    }
    private val barBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#333333") }
    private val barFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 26f; typeface = Typeface.DEFAULT_BOLD
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 34f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var current = 0f
    private var optimal = 1
    private val bgRect = RectF()
    private val bgPath = Path()
    private val barRect = RectF()

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) }

    fun setSlices(used: Int, optimalSlices: Int) {
        optimal = optimalSlices
        ValueAnimator.ofFloat(current, used.toFloat()).apply {
            duration = 400
            interpolator = DecelerateInterpolator()
            addUpdateListener { current = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val scale = resources.displayMetrics.density
        setMeasuredDimension((280 * scale).toInt(), (70 * scale).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val r = h / 2
        bgRect.set(0f, 0f, w, h)
        bgPath.reset()
        bgPath.addRoundRect(bgRect, r, r, Path.Direction.CW)
        canvas.drawPath(bgPath, bgPaint)

        labelPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("SLICES", 24f, h / 2f + 8f, labelPaint)

        val barLeft = 130f * resources.displayMetrics.density
        val barTop = h * 0.3f
        val barBottom = h * 0.7f
        val barRight = w - 24f * resources.displayMetrics.density
        barRect.set(barLeft, barTop, barRight, barBottom)
        val barR = (barBottom - barTop) / 2f
        canvas.drawRoundRect(barRect, barR, barR, barBgPaint)

        val maxDisplay = (optimal + 2).coerceAtLeast(3).toFloat()
        val fillRatio = (current / maxDisplay).coerceIn(0f, 1f)
        barFillPaint.color = when {
            current <= optimal -> Color.parseColor("#4CAF50")
            current <= optimal + 1 -> Color.parseColor("#FFC107")
            else -> Color.parseColor("#F44336")
        }
        val fillRect = RectF(barLeft, barTop, barLeft + (barRight - barLeft) * fillRatio, barBottom)
        canvas.drawRoundRect(fillRect, barR, barR, barFillPaint)

        valuePaint.textAlign = Paint.Align.CENTER
        valuePaint.color = barFillPaint.color
        canvas.drawText("${current.toInt()}/$optimal",
            (barLeft + barRight) / 2f, h / 2f + 10f, valuePaint)
    }
}
