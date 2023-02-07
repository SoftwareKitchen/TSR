package tech.softwarekitchen.tsr.camera

import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.tsr.vector.Ray
import kotlin.math.max

class ProjectionMatrix(
    val base: Vector3,
    front: Vector3,
    up: Vector3,
    private val fov: Double,
    private val imgSize: Vector2i
) {
    private val front = front.norm()
    private val right = front.ortho(up).norm()
    private val up = front.ortho(right).norm()
    private val maxEdge = max(imgSize.x,imgSize.y)
    private val degPerPixel = fov / maxEdge
    private val fovX = fov * imgSize.x / maxEdge
    private val fovY = fov * imgSize.y / maxEdge

    fun radToPixels(rad: Double): Double{
        return rad * (180 / Math.PI) / degPerPixel
    }

    fun getRayForScreenCoordinates(x: Int, y: Int): Ray {
        val upRatio = Math.tan(degPerPixel * (Math.PI / 180) * (imgSize.y / 2 - y))
        val rightRatio = Math.tan(degPerPixel * (Math.PI / 180) * (x - imgSize.x / 2))
        val rayVec = front.plus(up.scale(upRatio)).plus(right.scale(rightRatio)).norm()
        return Ray(base,rayVec,Vector2i(x,y))
    }

    fun mapPoint(point: Vector3): Pair<Vector2,Double>{
        val delta = point.minus(base)

        val f = front.scalar(delta)
        val u = up.scalar(delta)
        val r = right.scalar(delta)

        val angleX = Math.atan2(r,f)
        val angleY = Math.atan2(u,f)

        val ratioX = angleX / (fovX * Math.PI / 360)
        val ratioY = angleY / (fovY * Math.PI / 360)

        val pixX = (1+ratioX) * (imgSize.x / 2)
        val pixY = (1-ratioY) * (imgSize.y / 2)

        return Pair(Vector2(pixX,pixY),delta.length())
    }
}