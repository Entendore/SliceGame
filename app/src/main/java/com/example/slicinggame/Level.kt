package com.example.slicinggame

import android.graphics.PointF
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class Level(
    val objectPolygon: Polygon,
    val holePolygon: Polygon,
    val requiredPieces: Int = 2,
    val optimalSlices: Int = 1,
    val name: String = ""
) {
    companion object {
        fun makeCircle(cx: Float, cy: Float, r: Float, segments: Int = 32): Polygon {
            val verts = (0 until segments).map { i ->
                val angle = 2 * PI * i / segments
                PointF(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat())
            }
            return Polygon(verts)
        }

        fun makeStar(cx: Float, cy: Float, outerR: Float, innerR: Float, points: Int = 5): Polygon {
            val verts = (0 until points * 2).map { i ->
                val angle = PI * i / points - PI / 2
                val r = if (i % 2 == 0) outerR else innerR
                PointF(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat())
            }
            return Polygon(verts)
        }

        fun makeL(cx: Float, cy: Float, w: Float, h: Float): Polygon {
            val hw = w / 2; val hh = h / 2
            val t = w * 0.35f
            return Polygon(listOf(
                PointF(cx - hw, cy - hh),
                PointF(cx - hw + t, cy - hh),
                PointF(cx - hw + t, cy + hh - t),
                PointF(cx + hw, cy + hh - t),
                PointF(cx + hw, cy + hh),
                PointF(cx - hw, cy + hh)
            ))
        }

        fun makeArrow(cx: Float, cy: Float, w: Float, h: Float): Polygon {
            val hw = w / 2; val hh = h / 2
            return Polygon(listOf(
                PointF(cx - hw, cy - hh * 0.4f),
                PointF(cx, cy - hh * 0.4f),
                PointF(cx, cy - hh),
                PointF(cx + hw, cy),
                PointF(cx, cy + hh),
                PointF(cx, cy + hh * 0.4f),
                PointF(cx - hw, cy + hh * 0.4f)
            ))
        }

        fun makeCross(cx: Float, cy: Float, size: Float): Polygon {
            val s = size / 2
            val t = size * 0.2f
            return Polygon(listOf(
                PointF(cx - t, cy - s), PointF(cx + t, cy - s),
                PointF(cx + t, cy - t), PointF(cx + s, cy - t),
                PointF(cx + s, cy + t), PointF(cx + t, cy + t),
                PointF(cx + t, cy + s), PointF(cx - t, cy + s),
                PointF(cx - t, cy + t), PointF(cx - s, cy + t),
                PointF(cx - s, cy - t), PointF(cx - t, cy - t)
            ))
        }

        fun makeDaily(width: Float, height: Float): Level {
            val cx = width / 2f
            val objY = height * 0.35f
            val holeY = height * 0.78f
            val size = width * 0.35f
            val seed = (System.currentTimeMillis() / (24 * 60 * 60 * 1000)).toInt()
            val rng = java.util.Random(seed.toLong())
            val sides = 3 + rng.nextInt(6)
            val verts = (0 until sides).map { i ->
                val angle = 2 * PI * i / sides + rng.nextDouble() * 0.3
                val r = size * 0.5f * (0.7f + rng.nextFloat() * 0.3f)
                PointF(cx + r * cos(angle).toFloat(), objY + r * sin(angle).toFloat())
            }
            return Level(
                objectPolygon = Polygon(verts),
                holePolygon = Polygon(listOf(
                    PointF(cx - size/2, holeY - size/4), PointF(cx + size/2, holeY - size/4),
                    PointF(cx + size/2, holeY + size/4), PointF(cx - size/2, holeY + size/4))),
                optimalSlices = 1 + rng.nextInt(2),
                name = "Daily"
            )
        }

        fun generate(width: Float, height: Float): List<Level> {
            val cx = width / 2f
            val objY = height * 0.35f
            val holeY = height * 0.78f
            val size = width * 0.35f

            return listOf(
                Level(
                    objectPolygon = Polygon(listOf(
                        PointF(cx - size/2, objY - size/2), PointF(cx + size/2, objY - size/2),
                        PointF(cx + size/2, objY + size/2), PointF(cx - size/2, objY + size/2))),
                    holePolygon = Polygon(listOf(
                        PointF(cx - size/2, holeY - size/4), PointF(cx + size/2, holeY - size/4),
                        PointF(cx + size/2, holeY + size/4), PointF(cx - size/2, holeY + size/4))),
                    optimalSlices = 1, name = "Square"
                ),
                Level(
                    objectPolygon = Polygon(listOf(
                        PointF(cx, objY - size/2),
                        PointF(cx + size/2, objY + size/2),
                        PointF(cx - size/2, objY + size/2))),
                    holePolygon = Polygon(listOf(
                        PointF(cx, holeY - size/3),
                        PointF(cx + size/3, holeY + size/3),
                        PointF(cx - size/3, holeY + size/3))),
                    optimalSlices = 1, name = "Triangle"
                ),
                Level(
                    objectPolygon = makeCircle(cx, objY, size * 0.55f, 32),
                    holePolygon = Polygon(listOf(
                        PointF(cx - size/2, holeY - size/4), PointF(cx + size/2, holeY - size/4),
                        PointF(cx + size/2, holeY + size/4), PointF(cx - size/2, holeY + size/4))),
                    optimalSlices = 1, name = "Circle"
                ),
                Level(
                    objectPolygon = makeStar(cx, objY, size * 0.6f, size * 0.25f, 5),
                    holePolygon = Polygon(listOf(
                        PointF(cx - size/2, holeY - size/4), PointF(cx + size/2, holeY - size/4),
                        PointF(cx + size/2, holeY + size/4), PointF(cx - size/2, holeY + size/4))),
                    optimalSlices = 2, name = "Star"
                ),
                Level(
                    objectPolygon = makeL(cx, objY, size * 1.2f, size * 1.2f),
                    holePolygon = Polygon(listOf(
                        PointF(cx - size/2, holeY - size/4), PointF(cx + size/2, holeY - size/4),
                        PointF(cx + size/2, holeY + size/4), PointF(cx - size/2, holeY + size/4))),
                    optimalSlices = 2, name = "L-Shape"
                ),
                Level(
                    objectPolygon = makeCross(cx, objY, size * 1.3f),
                    holePolygon = Polygon(listOf(
                        PointF(cx - size/3, holeY - size/3), PointF(cx + size/3, holeY - size/3),
                        PointF(cx + size/3, holeY + size/3), PointF(cx - size/3, holeY + size/3))),
                    requiredPieces = 4,
                    optimalSlices = 2, name = "Cross"
                )
            )
        }
    }
}
