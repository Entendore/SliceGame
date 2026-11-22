package com.example.slicinggame

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout

data class TutorialStep(val title: String, val body: String, val icon: String)

class TutorialManager(private val container: FrameLayout) {

    private val steps = listOf(
        TutorialStep("Swipe to Slice", "Drag your finger across the shape to cut it", "✂️"),
        TutorialStep("Fit the Pieces", "Make pieces fit inside the golden hole", "🎯"),
        TutorialStep("Use Power-Ups", "Tap buttons below for undo & slow motion", "⚡"),
        TutorialStep("Tilt to Move", "Tilt your device to change gravity", "📱")
    )
    private var currentStep = 0
    private var overlayView: View? = null
    private var onComplete: (() -> Unit)? = null

    fun start(onComplete: () -> Unit) {
        this.onComplete = onComplete
        currentStep = 0
        showStep()
    }

    fun advance() {
        currentStep++
        if (currentStep >= steps.size) {
            dismiss()
            Settings.tutorialCompleted = true
            onComplete?.invoke()
        } else {
            showStep()
        }
    }

    private fun showStep() {
        overlayView?.let { container.removeView(it) }
        val step = steps[currentStep]

        val overlay = object : View(container.context) {
            private val bgPaint = Paint().apply { color = Color.parseColor("#000000"); alpha = 180 }
            private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2A2A4E") }
            private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700"); style = Paint.Style.STROKE; strokeWidth = 3f
            }
            private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700"); textSize = 42f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE; textSize = 32f; textAlign = Paint.Align.CENTER
            }
            private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE; textSize = 96f; textAlign = Paint.Align.CENTER
            }
            private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#666666"); textSize = 24f; textAlign = Paint.Align.CENTER
            }
            private var scale = 0f

            init {
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 400
                    interpolator = OvershootInterpolator(1.5f)
                    addUpdateListener { scale = it.animatedValue as Float; invalidate() }
                    start()
                }
            }

            override fun onDraw(canvas: Canvas) {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
                val cx = width / 2f
                val cy = height / 2f
                val cardW = width * 0.8f
                val cardH = 380f * resources.displayMetrics.density
                val cardRect = RectF(cx - cardW/2, cy - cardH/2, cx + cardW/2, cy + cardH/2)

                canvas.save()
                canvas.scale(scale, scale, cx, cy)
                canvas.drawRoundRect(cardRect, 24f, 24f, cardPaint)
                canvas.drawRoundRect(cardRect, 24f, 24f, borderPaint)
                canvas.drawText(step.icon, cx, cy - 90f, iconPaint)
                canvas.drawText(step.title, cx, cy, titlePaint)
                canvas.drawText(step.body, cx, cy + 60f, bodyPaint)
                canvas.drawText("${currentStep + 1} / ${steps.size}   •   Tap to continue",
                    cx, cy + 130f, progressPaint)
                canvas.restore()
            }
        }

        overlay.setOnClickListener { advance() }
        overlay.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)
        container.addView(overlay)
        overlayView = overlay
    }

    private fun dismiss() {
        overlayView?.let { container.removeView(it) }
        overlayView = null
    }
}
