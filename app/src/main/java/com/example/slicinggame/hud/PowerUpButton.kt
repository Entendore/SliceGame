package com.example.slicinggame.hud

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

class PowerUpButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var icon: String = "↶"
        set(value) { field = value; invalidate() }
    var usesLeft: Int = 3
        set(value) { field = value; invalidate() }
    var cooldownProgress: Float = 0f
        set(value) { field = value; invalidate() }
    var isActive: Boolean = false
        set(value) { field = value; invalidate() }

    var onClick: (() -> Unit)? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 48f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F44336") }
    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 24f
        typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    private val cooldownPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00BCD4"); style = Paint.Style.STROKE
        strokeWidth = 6f; strokeCap = Paint.Cap.ROUND
    }

    private var pressed = false
    private var pressScale = 1f
    private val bgRect = RectF()
    private val bgPath = Path()

    init { setLayerType(LAYER_TYPE_SOFTWARE, null) }

    fun setCooldownAnimated(target: Float, durationMs: Long) {
        ValueAnimator.ofFloat(cooldownProgress, target).apply {
            this.duration = durationMs
            interpolator = DecelerateInterpolator()
            addUpdateListener { cooldownProgress = it.animatedValue as Float }
            start()
        }
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val scale = resources.displayMetrics.density
        setMeasuredDimension((110 * scale).toInt(), (110 * scale).toInt())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { pressed = true; animatePress(0.9f); return true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pressed = false; animatePress(1f)
                if (event.action == MotionEvent.ACTION_UP && isEnabled) onClick?.invoke()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animatePress(target: Float) {
        ValueAnimator.ofFloat(pressScale, target).apply {
            duration = 120
            addUpdateListener { pressScale = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val cx = w / 2; val cy = h / 2
        val r = (kotlin.math.min(w, h) / 2f) - 8f

        canvas.save()
        canvas.scale(pressScale, pressScale, cx, cy)

        bgPaint.color = when {
            !isEnabled -> Color.parseColor("#222222")
            isActive -> Color.parseColor("#00BCD4")
            pressed -> Color.parseColor("#3A3A5E")
            else -> Color.parseColor("#2A2A4E")
        }
        bgPaint.alpha = if (isEnabled) 230 else 120
        bgRect.set(cx - r, cy - r, cx + r, cy + r)
        bgPath.reset()
        bgPath.addOval(bgRect, Path.Direction.CW)
        canvas.drawPath(bgPath, bgPaint)

        borderPaint.color = when {
            !isEnabled -> Color.parseColor("#444444")
            isActive -> Color.parseColor("#00E5FF")
            else -> Color.parseColor("#FFD700")
        }
        canvas.drawPath(bgPath, borderPaint)

        if (cooldownProgress > 0f && cooldownProgress < 1f) {
            val ringRect = RectF(cx - r + 6, cy - r + 6, cx + r - 6, cy + r - 6)
            canvas.drawArc(ringRect, -90f, 360f * cooldownProgress, false, cooldownPaint)
        }

        iconPaint.alpha = if (isEnabled) 255 else 100
        canvas.drawText(icon, cx, cy + 16f, iconPaint)

        if (usesLeft in 1..9) {
            val bx = cx + r * 0.6f
            val by = cy - r * 0.6f
            val br = 20f * resources.displayMetrics.density
            canvas.drawCircle(bx, by, br, badgeBgPaint)
            canvas.drawText(usesLeft.toString(), bx, by + 8f, badgeTextPaint)
        } else if (usesLeft == 0) {
            bgPaint.color = Color.parseColor("#000000"); bgPaint.alpha = 140
            canvas.drawPath(bgPath, bgPaint)
        }

        canvas.restore()
    }
}
