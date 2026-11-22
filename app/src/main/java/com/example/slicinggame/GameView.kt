package com.example.slicinggame

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, SensorEventListener {

    private val thread = GameThread()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 3f; color = Color.WHITE
    }
    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222"); style = Paint.Style.FILL
    }
    private val holeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700"); style = Paint.Style.STROKE; strokeWidth = 6f
    }
    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 5f
        setShadowLayer(8f, 0f, 0f, Color.parseColor("#FFFFFF"))
    }
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val bgPaint = Paint().apply { style = Paint.Style.FILL }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 8f
        setShadowLayer(20f, 0f, 0f, Color.parseColor("#FFD700"))
    }
    private val overlayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    val soundManager = SoundManager(context)
    val scoreRepo = ScoreRepository(context)
    private val particles = ParticleSystem()

    private var gravity = PointF(0f, 900f)
    private var targetGravity = PointF(0f, 900f)
    private var displayRotation = 0

    private var levels: List<Level> = emptyList()
    private var currentLevel = 0
    private var pieces = mutableListOf<PhysicsPiece>()
    private var hole: Polygon? = null
    private var slicing = false
    private var sliceStart: PointF? = null
    private var sliceEnd: PointF? = null
    private var lastTime = 0L
    private var levelComplete = false
    private var gameOver = false
    private var paused = false

    private var sliceCount = 0
    private var totalScore = 0
    private var levelScore = 0
    private var stars = 0

    private var lastComboMultiplier = 1
    private var comboDisplayTimer = 0f
    private var comboDisplayValue = 0

    private val undoStack = mutableListOf<List<PhysicsPiece>>()
    private var undoUsesLeft = 3
    private var slowMotionTimer = 0f
    private var slowMotionUsesLeft = 2
    private val SLOW_MO_DURATION = 5f
    private val SLOW_MO_SCALE = 0.3f

    private data class TrailPoint(val x: Float, val y: Float, val time: Float)
    private val touchTrail = ArrayList<TrailPoint>(64)
    private val TRAIL_LIFETIME = 0.35f

    private var shakeTimer = 0f
    private var shakeIntensity = 0f

    private var transitionAlpha = 0f
    private var transitioning = false

    private var needsRedraw = true

    private val SNAP_DISTANCE = 60f
    private val ROTATION_STEP = (Math.PI / 12).toFloat()
    private var secondFingerId = -1
    private var rotationStartAngle = 0f
    private var rotatingPiece: PhysicsPiece? = null

    private var onLevelChange: ((Int, Boolean, Int, Int, Int, Int, Int) -> Unit)? = null
    private var onPowerUpChange: ((Int, Int, Boolean) -> Unit)? = null
    private var onHighScoreChange: ((Int) -> Unit)? = null
    private var onCombo: ((Int) -> Unit)? = null

    init {
        holder.addCallback(this)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setOnLevelChange(cb: (Int, Boolean, Int, Int, Int, Int, Int) -> Unit) { onLevelChange = cb }
    fun setOnPowerUpChange(cb: (Int, Int, Boolean) -> Unit) { onPowerUpChange = cb }
    fun setOnHighScoreChange(cb: (Int) -> Unit) { onHighScoreChange = cb }
    fun setOnCombo(cb: (Int) -> Unit) { onCombo = cb }
    fun setDisplayRotation(rot: Int) { displayRotation = rot }

    fun loadLevels(w: Float, h: Float) {
        levels = Level.generate(w, h)
        currentLevel = 0
        totalScore = 0
        loadLevel(skipTransition = true)
    }

    fun loadDaily(w: Float, h: Float) {
        levels = listOf(Level.makeDaily(w, h))
        currentLevel = 0
        totalScore = 0
        loadLevel(skipTransition = true)
    }

    private fun loadLevel(skipTransition: Boolean = false) {
        if (currentLevel >= levels.size) {
            gameOver = true
            scoreRepo.setHighScore(totalScore)
            onHighScoreChange?.invoke(scoreRepo.getHighScore())
            return
        }
        if (!skipTransition) { transitioning = true; transitionAlpha = 0f }
        val lvl = levels[currentLevel]
        pieces.clear()
        pieces.add(PhysicsPiece(polygon = lvl.objectPolygon, color = Color.parseColor("#4CAF50")))
        hole = lvl.holePolygon
        levelComplete = false
        sliceCount = 0; levelScore = 0; stars = 0
        undoStack.clear(); undoUsesLeft = 3
        slowMotionUsesLeft = 2; slowMotionTimer = 0f
        particles.clear(); touchTrail.clear()
        needsRedraw = true
        notifyChange(); notifyPowerUpChange()
    }

    fun nextLevel() { currentLevel++; loadLevel() }
    fun getTotalScore(): Int = totalScore
    fun isPaused(): Boolean = paused
    fun setPaused(p: Boolean) { paused = p; needsRedraw = true }
    fun getSliceCount(): Int = sliceCount
    fun getLevelName(level: Int): String = levels.getOrNull(level)?.name ?: ""
    fun getLevelOptimal(level: Int): Int = levels.getOrNull(level)?.optimalSlices ?: 1

    fun canUndo(): Boolean = undoUsesLeft > 0 && undoStack.isNotEmpty() && !levelComplete && !paused
    fun canSlowMotion(): Boolean = slowMotionUsesLeft > 0 && slowMotionTimer <= 0 && !levelComplete && !paused

    fun undoSlice() {
        if (!canUndo()) return
        val prevState = undoStack.removeAt(undoStack.lastIndex)
        pieces.clear(); pieces.addAll(prevState.map { it.clone() })
        undoUsesLeft--; sliceCount = (sliceCount - 1).coerceAtLeast(0)
        vibrate(30)
        needsRedraw = true
        notifyPowerUpChange(); notifyChange()
    }

    fun activateSlowMotion() {
        if (!canSlowMotion()) return
        slowMotionTimer = SLOW_MO_DURATION
        slowMotionUsesLeft--
        vibrate(50)
        needsRedraw = true
        notifyPowerUpChange()
    }

    private fun vibrate(durationMs: Long) {
        if (!Settings.hapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMs)
        }
    }

    private fun vibrate(pattern: LongArray) {
        if (!Settings.hapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }

    fun startSensors() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }
    fun stopSensors() { sensorManager.unregisterListener(this) }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val rawX = event.values[0]; val rawY = event.values[1]
        val (sx, sy) = when (displayRotation) {
            0 -> Pair(rawX, rawY); 1 -> Pair(-rawY, rawX)
            2 -> Pair(-rawX, -rawY); 3 -> Pair(rawY, -rawX)
            else -> Pair(rawX, rawY)
        }
        val sens = Settings.sensitivity
        targetGravity.set(sx * 92f * sens, sy * 92f * sens)
    }
    override fun onAccuracyChanged(s: Sensor?, a: Int) {}

    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, h2: Int) { needsRedraw = true }
    override fun surfaceDestroyed(h: SurfaceHolder) { thread.running = false }
    override fun surfaceCreated(h: SurfaceHolder) {
        if (levels.isEmpty()) loadLevels(width.toFloat(), height.toFloat())
        thread.running = true; thread.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (levelComplete || gameOver || paused) return true

        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN && event.pointerCount == 2) {
            secondFingerId = event.getPointerId(event.actionIndex)
            val p0 = PointF(event.getX(0), event.getY(0))
            val p1 = PointF(event.getX(1), event.getY(1))
            rotatingPiece = findPieceAt(p0) ?: findPieceAt(p1)
            if (rotatingPiece != null && !rotatingPiece!!.settled) {
                rotationStartAngle = Math.atan2(
                    (p1.y - p0.y).toDouble(), (p1.x - p0.x).toDouble()).toFloat()
                slicing = false
                return true
            }
        }

        if (event.actionMasked == MotionEvent.ACTION_MOVE && event.pointerCount == 2 && rotatingPiece != null) {
            val p0 = PointF(event.getX(0), event.getY(0))
            val p1 = PointF(event.getX(1), event.getY(1))
            val currentAngle = Math.atan2(
                (p1.y - p0.y).toDouble(), (p1.x - p0.x).toDouble()).toFloat()
            val delta = currentAngle - rotationStartAngle
            if (Math.abs(delta.toDouble()) > ROTATION_STEP) {
                rotatePiece(rotatingPiece!!, if (delta > 0) ROTATION_STEP else -ROTATION_STEP)
                rotationStartAngle = currentAngle
                needsRedraw = true
            }
            return true
        }

        if (event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
            secondFingerId = -1
            rotatingPiece = null
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                slicing = true
                sliceStart = PointF(event.x, event.y)
                sliceEnd = sliceStart
                touchTrail.clear()
                touchTrail.add(TrailPoint(event.x, event.y, 0f))
            }
            MotionEvent.ACTION_MOVE -> {
                sliceEnd = PointF(event.x, event.y)
                touchTrail.add(TrailPoint(event.x, event.y, 0f))
                if (touchTrail.size > 64) touchTrail.removeAt(0)
                needsRedraw = true
            }
            MotionEvent.ACTION_UP -> {
                slicing = false
                performSlice(sliceStart!!, event.x, event.y)
                sliceStart = null; sliceEnd = null
            }
        }
        return true
    }

    private fun findPieceAt(p: PointF): PhysicsPiece? {
        for (piece in pieces) {
            if (piece.polygon.containsPoint(p, 10f)) return piece
        }
        return null
    }

    private fun rotatePiece(piece: PhysicsPiece, angle: Float) {
        val c = piece.polygon.center()
        val cos = kotlin.math.cos(angle)
        val sin = kotlin.math.sin(angle)
        for (v in piece.polygon.vertices) {
            val rx = v.x - c.x; val ry = v.y - c.y
            v.x = rx * cos - ry * sin + c.x
            v.y = rx * sin + ry * cos + c.y
        }
        piece.polygon.invalidateCache()
    }

    private fun performSlice(start: PointF, endX: Float, endY: Float) {
        val end = PointF(endX, endY)
        val dx = end.x - start.x; val dy = end.y - start.y
        if (dx * dx + dy * dy < 100f) return

        if (undoStack.size >= 5) undoStack.removeAt(0)
        undoStack.add(pieces.map { it.clone() })

        val newPieces = mutableListOf<PhysicsPiece>()
        var piecesSliced = 0

        for (piece in pieces) {
            if (piece.settled) { newPieces.add(piece); continue }
            val (left, right) = piece.polygon.split(start, end)
            if (left.vertices.size >= 3 && right.vertices.size >= 3) {
                piecesSliced++
                val perpX = -dy; val perpY = dx
                val len = kotlin.math.sqrt(perpX * perpX + perpY * perpY)
                val impulse = 120f
                newPieces.add(PhysicsPiece(
                    polygon = left,
                    velocity = PointF(perpX / len * impulse, -250f),
                    angularVel = -2f, color = piece.color))
                newPieces.add(PhysicsPiece(
                    polygon = right,
                    velocity = PointF(-perpX / len * impulse, -250f),
                    angularVel = 2f, color = piece.color))
            } else {
                newPieces.add(piece)
            }
        }

        if (piecesSliced > 0) {
            pieces = newPieces
            sliceCount++
            Settings.totalSlices += piecesSliced

            particles.emitSlice(start.x, start.y, end.x, end.y, 25 + piecesSliced * 10)

            lastComboMultiplier = when {
                piecesSliced >= 4 -> 4
                piecesSliced == 3 -> 3
                piecesSliced == 2 -> 2
                else -> 1
            }
            if (lastComboMultiplier > 1) {
                comboDisplayValue = lastComboMultiplier
                comboDisplayTimer = 1.5f
                shakeTimer = 0.4f
                shakeIntensity = 8f * lastComboMultiplier
                Settings.totalCombos++
                onCombo?.invoke(lastComboMultiplier)
            }

            soundManager.playSlice()
            vibrate(40 + piecesSliced * 15L)
            checkWinCondition()
            needsRedraw = true
            notifyChange(); notifyPowerUpChange()
        } else {
            if (undoStack.isNotEmpty()) undoStack.removeAt(undoStack.lastIndex)
        }
    }

    private fun checkWinCondition() {
        val h = hole ?: return
        val lvl = levels.getOrNull(currentLevel) ?: return
        val fitting = pieces.count { it.settled && it.polygon.fitsInside(h) }
        if (fitting >= lvl.requiredPieces) {
            levelComplete = true
            pieces.forEach { it.settled = true }

            val base = 1000
            val penalty = kotlin.math.max(0, sliceCount - lvl.optimalSlices) * 150
            val comboBonus = (lastComboMultiplier - 1) * 250
            levelScore = ((base - penalty + comboBonus).coerceAtLeast(100))
            stars = when {
                sliceCount <= lvl.optimalSlices -> 3
                sliceCount <= lvl.optimalSlices + 1 -> 2
                else -> 1
            }
            totalScore += levelScore
            scoreRepo.setBestStars(currentLevel, stars)
            scoreRepo.setHighScore(totalScore)
            Settings.levelsCompleted = (currentLevel + 1).coerceAtLeast(Settings.levelsCompleted)

            val c = h.center()
            particles.emitWin(c.x, c.y, 100)
            shakeTimer = 0.6f; shakeIntensity = 20f

            soundManager.playWin()
            vibrate(longArrayOf(0, 50, 50, 100))
            needsRedraw = true
            notifyChange()
            onHighScoreChange?.invoke(scoreRepo.getHighScore())

            if (undoUsesLeft == 3) {
                val p = context.getSharedPreferences("slice_stats", Context.MODE_PRIVATE)
                p.edit().putBoolean("clean_win", true).apply()
            }
            AchievementManager.checkAll()
        }
    }

    private fun notifyChange() {
        onLevelChange?.invoke(currentLevel, levelComplete, totalScore, sliceCount, stars, comboDisplayValue, lastComboMultiplier)
    }
    private fun notifyPowerUpChange() {
        onPowerUpChange?.invoke(undoUsesLeft, slowMotionUsesLeft, slowMotionTimer > 0)
    }

    private inner class GameThread : Thread() {
        @Volatile var running = false
        override fun run() {
            lastTime = System.currentTimeMillis()
            while (running) {
                val now = System.currentTimeMillis()
                val dt = ((now - lastTime) / 1000f).coerceAtMost(0.05f)
                lastTime = now
                val changed = update(dt)
                if (changed || needsRedraw) {
                    draw()
                    needsRedraw = false
                } else {
                    try { sleep(32) } catch (_: InterruptedException) {}
                }
            }
        }
    }

    private fun update(dt: Float): Boolean {
        if (paused && !transitioning) return false
        var changed = false

        val lerp = 1f - kotlin.math.exp(-dt * 8f)
        gravity.x += (targetGravity.x - gravity.x) * lerp
        gravity.y += (targetGravity.y - gravity.y) * lerp

        if (transitioning) {
            transitionAlpha += dt * 2.5f
            if (transitionAlpha >= 1f) { transitionAlpha = 1f; transitioning = false }
            changed = true
        }

        val timeScale = if (slowMotionTimer > 0f) SLOW_MO_SCALE else 1f
        if (slowMotionTimer > 0f) {
            slowMotionTimer -= dt
            if (slowMotionTimer <= 0f) { slowMotionTimer = 0f; notifyPowerUpChange() }
            changed = true
        }

        val scaledDt = dt * timeScale

        val h = hole
        if (h != null && !levelComplete) {
            val holeCenter = h.center()
            for (p in pieces) {
                if (p.settled) continue
                val c = p.polygon.center()
                val dx = holeCenter.x - c.x
                val dy = holeCenter.y - c.y
                val dist = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (dist < SNAP_DISTANCE && dist > 1f) {
                    val pull = (1f - dist / SNAP_DISTANCE) * 400f
                    p.velocity.x += dx / dist * pull * dt
                    p.velocity.y += dy / dist * pull * dt
                }
            }
        }

        for (p in pieces) p.update(scaledDt, gravity)

        val w = width.toFloat(); val hh = height.toFloat()
        for (p in pieces) {
            val c = p.polygon.center()
            if (c.x < -300 || c.x > w + 300 || c.y < -500 || c.y > hh + 300) p.settled = true
            val speed = kotlin.math.hypot(p.velocity.x, p.velocity.y)
            if (speed < 5f && abs(p.angularVel) < 0.05f) p.settled = true
        }

        if (comboDisplayTimer > 0f) {
            comboDisplayTimer -= dt
            if (comboDisplayTimer <= 0f) { comboDisplayTimer = 0f; comboDisplayValue = 0 }
            changed = true
        }

        if (shakeTimer > 0f) {
            shakeTimer -= dt
            if (shakeTimer <= 0f) { shakeTimer = 0f; shakeIntensity = 0f }
            changed = true
        }

        if (touchTrail.isNotEmpty()) {
            for (i in touchTrail.indices) {
                val tp = touchTrail[i]
                touchTrail[i] = tp.copy(time = tp.time + dt)
            }
            touchTrail.removeAll { it.time > TRAIL_LIFETIME }
            changed = true
        }

        if (particles.hasActive()) {
            particles.update(scaledDt)
            changed = true
        }

        return changed
    }

    private fun draw() {
        val canvas = holder.lockCanvas() ?: return
        try {
            val shakeX = if (shakeTimer > 0f) (Math.random() - 0.5).toFloat() * shakeIntensity else 0f
            val shakeY = if (shakeTimer > 0f) (Math.random() - 0.5).toFloat() * shakeIntensity else 0f
            canvas.save()
            canvas.translate(shakeX, shakeY)

            bgPaint.color = if (slowMotionTimer > 0f) Color.parseColor("#0A1A3E")
                            else Color.parseColor("#1A1A2E")
            canvas.drawPaint(bgPaint)

            hole?.let { h ->
                val path = Path().apply {
                    h.vertices.forEachIndexed { i, v -> if (i == 0) moveTo(v.x, v.y) else lineTo(v.x, v.y) }
                    close()
                }
                canvas.drawPath(path, holePaint)
                canvas.drawPath(path, holeBorder)
            }

            val h = hole
            if (h != null) {
                val hc = h.center()
                for (p in pieces) {
                    if (p.settled) continue
                    val c = p.polygon.center()
                    val dist = kotlin.math.hypot((c.x - hc.x).toDouble(), (c.y - hc.y).toDouble()).toFloat()
                    if (dist < SNAP_DISTANCE * 2f) {
                        val alpha = ((1f - dist / (SNAP_DISTANCE * 2f)) * 255).toInt()
                        glowPaint.color = Color.parseColor("#FFD700")
                        glowPaint.alpha = alpha
                        canvas.drawPath(p.getPath(), glowPaint)
                    }
                }
            }

            val byColor = pieces.groupBy { it.color }
            for ((color, group) in byColor) {
                fillPaint.color = color
                for (p in group) {
                    canvas.drawPath(p.getPath(), fillPaint)
                    canvas.drawPath(p.getPath(), strokePaint)
                }
            }

            particles.draw(canvas)

            if (touchTrail.size >= 2) {
                for (i in 1 until touchTrail.size) {
                    val a = touchTrail[i - 1]; val b = touchTrail[i]
                    val alpha = ((1f - b.time / TRAIL_LIFETIME) * 255).toInt().coerceIn(0, 255)
                    trailPaint.color = Color.argb(alpha, 255, 255, 255)
                    trailPaint.strokeWidth = 12f * (1f - b.time / TRAIL_LIFETIME) + 2f
                    canvas.drawLine(a.x, a.y, b.x, b.y, trailPaint)
                }
            }

            if (slicing && sliceStart != null && sliceEnd != null) {
                canvas.drawLine(sliceStart!!.x, sliceStart!!.y,
                    sliceEnd!!.x, sliceEnd!!.y, slicePaint)
            }

            if (paused) {
                canvas.restore()
                fillPaint.color = Color.argb(180, 0, 0, 0)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)
                overlayTextPaint.color = Color.WHITE
                overlayTextPaint.textSize = 100f
                canvas.drawText("PAUSED", width / 2f, height / 2f, overlayTextPaint)
                return
            }

            if (gameOver) {
                overlayTextPaint.color = Color.WHITE
                overlayTextPaint.textSize = 80f
                canvas.drawText("YOU WIN!", width / 2f, height / 2f - 40f, overlayTextPaint)
                overlayTextPaint.textSize = 50f
                canvas.drawText("Final Score: $totalScore", width / 2f, height / 2f + 40f, overlayTextPaint)
                canvas.drawText("High Score: ${scoreRepo.getHighScore()}", width / 2f, height / 2f + 110f, overlayTextPaint)
            }

            canvas.restore()

            if (transitioning || transitionAlpha > 0f) {
                if (transitionAlpha < 0.5f) {
                    fillPaint.color = Color.argb(((1f - transitionAlpha * 2f) * 255).toInt(), 0, 0, 0)
                } else {
                    fillPaint.color = Color.argb(((transitionAlpha - 0.5f) * 2 * 255).toInt().let { 255 - it }, 0, 0, 0)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }
}
