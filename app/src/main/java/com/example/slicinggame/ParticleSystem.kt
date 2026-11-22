package com.example.slicinggame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float, var maxLife: Float,
    var size: Float, var color: Int
)

class ParticleSystem(private val maxParticles: Int = 300) {
    private val particles = ArrayList<Particle>(maxParticles)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun emitSlice(x1: Float, y1: Float, x2: Float, y2: Float, count: Int = 20) {
        val dx = x2 - x1; val dy = y2 - y1
        val len = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
        if (len < 1f) return
        val nx = -dy / len; val ny = dx / len

        repeat(count) {
            val t = Random.nextFloat()
            val px = x1 + dx * t
            val py = y1 + dy * t
            val speed = 200f + Random.nextFloat() * 400f
            val dir = if (Random.nextBoolean()) 1f else -1f
            val spread = (Random.nextFloat() - 0.5f) * 150f

            if (particles.size >= maxParticles) particles.removeAt(0)
            particles.add(Particle(
                x = px, y = py,
                vx = nx * speed * dir + dx / len * spread,
                vy = ny * speed * dir + dy / len * spread - 100f,
                life = 0.6f + Random.nextFloat() * 0.4f,
                maxLife = 1f,
                size = 3f + Random.nextFloat() * 5f,
                color = if (Random.nextFloat() < 0.3f) Color.parseColor("#FFD700")
                        else Color.parseColor("#FF5722")
            ))
        }
    }

    fun emitWin(cx: Float, cy: Float, count: Int = 80) {
        repeat(count) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = 300f + Random.nextFloat() * 500f
            if (particles.size >= maxParticles) particles.removeAt(0)
            particles.add(Particle(
                x = cx, y = cy,
                vx = kotlin.math.cos(angle).toFloat() * speed,
                vy = kotlin.math.sin(angle).toFloat() * speed,
                life = 1.2f, maxLife = 1.2f,
                size = 4f + Random.nextFloat() * 6f,
                color = when (Random.nextInt(4)) {
                    0 -> Color.parseColor("#FFD700")
                    1 -> Color.parseColor("#4CAF50")
                    2 -> Color.parseColor("#00BCD4")
                    else -> Color.parseColor("#FF5722")
                }
            ))
        }
    }

    fun update(dt: Float, gravity: Float = 800f) {
        var i = 0
        while (i < particles.size) {
            val p = particles[i]
            p.life -= dt
            if (p.life <= 0f) { particles.removeAt(i); continue }
            p.vy += gravity * dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vx *= 0.98f
            p.vy *= 0.98f
            i++
        }
    }

    fun draw(canvas: Canvas) {
        for (p in particles) {
            val alpha = (p.life / p.maxLife * 255).toInt().coerceIn(0, 255)
            paint.color = p.color
            paint.alpha = alpha
            paint.style = Paint.Style.FILL
            canvas.drawCircle(p.x, p.y, p.size, paint)
        }
    }

    fun hasActive(): Boolean = particles.isNotEmpty()
    fun clear() { particles.clear() }
}
