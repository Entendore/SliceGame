package com.example.slicinggame

import android.graphics.Path
import android.graphics.PointF

data class PhysicsPiece(
    var polygon: Polygon,
    var velocity: PointF = PointF(0f, 0f),
    var angularVel: Float = 0f,
    var color: Int = 0xFF4CAF50.toInt(),
    var settled: Boolean = false
) {
    @Transient private var cachedPath: Path? = null
    @Transient private var pathVersion: Int = -1

    fun update(dt: Float, gravity: PointF) {
        if (settled) return
        velocity.x += gravity.x * dt
        velocity.y += gravity.y * dt
        velocity.x *= 0.995f
        velocity.y *= 0.995f

        val c = polygon.center()
        val dx = velocity.x * dt
        val dy = velocity.y * dt
        val cos = kotlin.math.cos(angularVel * dt)
        val sin = kotlin.math.sin(angularVel * dt)
        val cx = c.x; val cy = c.y

        val verts = polygon.vertices
        for (i in verts.indices) {
            val v = verts[i]
            val rx = v.x - cx; val ry = v.y - cy
            v.x = rx * cos - ry * sin + cx + dx
            v.y = rx * sin + ry * cos + cy + dy
        }
        polygon.invalidateCache()
        cachedPath = null
        angularVel *= 0.98f
    }

    fun getPath(): Path {
        val currentVersion = polygon.vertices.hashCode()
        if (cachedPath != null && pathVersion == currentVersion) return cachedPath!!
        val path = Path()
        val verts = polygon.vertices
        if (verts.isNotEmpty()) {
            path.moveTo(verts[0].x, verts[0].y)
            for (i in 1 until verts.size) path.lineTo(verts[i].x, verts[i].y)
            path.close()
        }
        cachedPath = path
        pathVersion = currentVersion
        return path
    }

    fun clone(): PhysicsPiece = PhysicsPiece(
        polygon = Polygon(vertices = polygon.vertices.map { PointF(it.x, it.y) }),
        velocity = PointF(velocity.x, velocity.y),
        angularVel = angularVel,
        color = color,
        settled = settled
    )
}
