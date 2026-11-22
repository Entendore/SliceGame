package com.example.slicinggame.hud

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.roundToInt

class ScoreCounterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E"); alpha = 200
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888"); textSize = 28f; typeface = Typeface.DEFAULT_BOLD
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700"); textSize = 56f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(12f, 0f, 0f, Color.parseColor("#FFD70066"))
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700"); style = Paint.Style.STROKE; strokeWidth = 2f
    }

    private var displayValue = 0f
    private var targetValue = 0f
    private var animator: ValueAnimator? = null
    private var pulseScale = 1f
    private val bgRect = RectF()
    private val bgPath = Path()

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) }

    fun setScore(score: Int, animate: Boolean = true) {
        if (score == targetValue.toInt()) return
        targetValue = score.toFloat()
        animator?.cancel()
        if (!animate) { displayValue = targetValue; invalidate(); return }
        animator = ValueAnimator.ofFloat(displayValue, targetValue).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
            addUpdateListener { displayValue = it.animatedValue as Float; invalidate() }
            start()
        }
        pulseScale = 1.15f
        postDelayed({
            ValueAnimator.ofFloat(1.15f, 1f).apply {
                duration = 300
                interpolator = DecelerateInterpolator()
                addUpdateListener { pulseScale = it.animatedValue as Float; invalidate() }
                start()
            }
        }, 50)
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val scale = resources.displayMetrics.density
        setMeasuredDimension((320 * scale).toInt(), (110 * scale).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val r = h / 2
        bgRect.set(0f, 0f, w, h)
        bgPath.reset()
        bgPath.addRoundRect(bgRect, r, r, Path.Direction.CW)
        canvas.drawPath(bgPath, bgPaint)
        canvas.drawPath(bgPath, accentPaint)

        canvas.save()
        val cx = w / 2; val cy = h / 2
        canvas.scale(pulseScale, pulseScale, cx, cy)
        labelPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("SCORE", cx, cy - 8f, labelPaint)
        valuePaint.textAlign = Paint.Align.CENTER
        canvas.drawText(displayValue.roundToInt().toString(), cx, cy + 36f, valuePaint)
        canvas.restore()
    }
}
