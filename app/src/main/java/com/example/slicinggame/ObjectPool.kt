package com.example.slicinggame

import android.graphics.PointF

class PointFPool(initialCapacity: Int = 256) {
    private val pool = ArrayDeque<PointF>(initialCapacity)

    init { repeat(initialCapacity) { pool.addLast(PointF(0f, 0f)) } }

    fun obtain(x: Float = 0f, y: Float = 0f): PointF {
        val p = if (pool.isNotEmpty()) pool.removeLast() else PointF()
        p.x = x; p.y = y
        return p
    }

    fun recycle(p: PointF) { if (pool.size < 1024) pool.addLast(p) }
}

object Pools { val points = PointFPool(512) }
