package com.example.slicinggame

import android.graphics.PointF
import kotlin.math.abs

data class Polygon(val vertices: List<PointF>) {

    @Transient private var cachedBounds: RectF? = null
    @Transient private var cachedCenter: PointF? = null

    fun split(lineStart: PointF, lineEnd: PointF): Pair<Polygon, Polygon> {
        val left = ArrayList<PointF>(vertices.size + 2)
        val right = ArrayList<PointF>(vertices.size + 2)
        if (vertices.size < 3) return Pair(this, Polygon(emptyList()))

        for (i in vertices.indices) {
            val current = vertices[i]
            val next = vertices[(i + 1) % vertices.size]
            val currentSide = sideOfLine(current, lineStart, lineEnd)
            val nextSide = sideOfLine(next, lineStart, lineEnd)

            if (currentSide >= 0) left.add(current) else right.add(current)

            if ((currentSide > 0 && nextSide < 0) || (currentSide < 0 && nextSide > 0)) {
                val intersection = intersect(current, next, lineStart, lineEnd)
                left.add(intersection)
                right.add(intersection)
            }
        }
        return Pair(Polygon(left), Polygon(right))
    }

    fun fitsInside(hole: Polygon, tolerance: Float = 5f): Boolean {
        if (vertices.isEmpty() || hole.vertices.isEmpty()) return false
        val myBounds = boundingBox()
        val holeBounds = hole.boundingBox()
        if (myBounds.left < holeBounds.left - tolerance ||
            myBounds.right > holeBounds.right + tolerance ||
            myBounds.top < holeBounds.top - tolerance ||
            myBounds.bottom > holeBounds.bottom + tolerance) {
            return false
        }
        return vertices.all { hole.containsPoint(it, tolerance) }
    }

    fun containsPoint(p: PointF, tolerance: Float = 0f): Boolean {
        var inside = false
        val n = vertices.size
        var j = n - 1
        val px = p.x; val py = p.y
        for (i in 0 until n) {
            val vi = vertices[i]; val vj = vertices[j]
            if (((vi.y > py) != (vj.y > py)) &&
                (px < (vj.x - vi.x) * (py - vi.y) / (vj.y - vi.y + 1e-9f) + vi.x)) {
                inside = !inside
            }
            j = i
        }
        if (!inside && tolerance > 0f) {
            for (i in 0 until n) {
                val a = vertices[i]; val b = vertices[(i + 1) % n]
                if (distanceToSegment(p, a, b) <= tolerance) return true
            }
        }
        return inside
    }

    private fun distanceToSegment(p: PointF, a: PointF, b: PointF): Float {
        val dx = b.x - a.x; val dy = b.y - a.y
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0f) return kotlin.math.hypot(p.x - a.x, p.y - a.y)
        var t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq
        t = t.coerceIn(0f, 1f)
        return kotlin.math.hypot(p.x - (a.x + t * dx), p.y - (a.y + t * dy))
    }

    fun boundingBox(): RectF {
        cachedBounds?.let { return it }
        if (vertices.isEmpty()) return RectF(0f, 0f, 0f, 0f).also { cachedBounds = it }
        var l = vertices[0].x; var r = l
        var t = vertices[0].y; var b = t
        for (v in vertices) {
            val vx = v.x; val vy = v.y
            if (vx < l) l = vx; if (vx > r) r = vx
            if (vy < t) t = vy; if (vy > b) b = vy
        }
        return RectF(l, t, r, b).also { cachedBounds = it }
    }

    fun center(): PointF {
        cachedCenter?.let { return it }
        val b = boundingBox()
        return PointF((b.left + b.right) / 2f, (b.top + b.bottom) / 2f).also { cachedCenter = it }
    }

    fun invalidateCache() { cachedBounds = null; cachedCenter = null }

    fun area(): Float {
        var a = 0f
        val n = vertices.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            a += vertices[i].x * vertices[j].y - vertices[j].x * vertices[i].y
        }
        return abs(a) / 2f
    }

    private fun sideOfLine(p: PointF, a: PointF, b: PointF): Float =
        (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)

    private fun intersect(p1: PointF, p2: PointF, p3: PointF, p4: PointF): PointF {
        val d = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x)
        val t = ((p1.x - p3.x) * (p3.y - p4.y) - (p1.y - p3.y) * (p3.x - p4.x)) / d
        return PointF(p1.x + t * (p2.x - p1.x), p1.y + t * (p2.y - p1.y))
    }

    data class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float)
}
