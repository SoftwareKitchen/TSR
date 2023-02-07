package tech.softwarekitchen.tsr.`object`

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.tsr.camera.Pixel
import tech.softwarekitchen.tsr.camera.ProjectionMatrix
import tech.softwarekitchen.tsr.color.Color
import tech.softwarekitchen.tsr.light.Light
import tech.softwarekitchen.tsr.vector.Ray
import tech.softwarekitchen.tsr.vector.Rectangle2D

class OptimizedSphere(
    private val base: Sphere,
    private val _minimalDepth: Double,
    private val c: Double,
    private val cameraToCenter: Vector3,
    private val vecDistThreshold: Double,
    private val pixBound: Rectangle2D
): RenderableObject3D(){
    override fun getMinimalDepth(): Double {
        return _minimalDepth
    }

    override fun getPixbound(): Rectangle2D {
        return pixBound
    }

    override fun process(ray: Ray, light: List<Light>, cutoff: Double): Pixel? {
        val scalar: Double = ray.vec.scalar(cameraToCenter)
        if(scalar < vecDistThreshold){
            return null
        }
        val b = -2 * scalar
        val dis = b*b - 4*c
        if(dis < 0){
            return null
        }
        val sqdis = Math.sqrt(dis)
        val l1 = (-b - sqdis) / 2
        val l2 = (-b + sqdis) / 2
        val l = when(l1 < 0){
            true -> l2
            else -> l1
        }

        val impactPos = ray.base.plus(ray.vec.scale(l))
        val centerToImpact = impactPos.minus(base.base).norm()

        val lightSum = java.lang.Double.min(light.map {
                it.getForVector(centerToImpact)
        }.sum(), 1.0)
        return Pixel(l, base.color,lightSum)
    }
}

class Sphere(
    val base: Vector3,
    private val radius: Double,
    val color: Color
): Object3D() {

    override fun prepare(matrix: ProjectionMatrix): RenderableObject3D {
        val camToCenter = base.minus(matrix.base)
        val dist = camToCenter.length()
        val angle = Math.asin(radius / dist)

        val centerMapped = matrix.mapPoint(base)
        val dpix = matrix.radToPixels(angle)

        return OptimizedSphere(
            this,dist - radius,dist*dist-radius*radius,
            camToCenter,dist*Math.cos(angle),
            Rectangle2D(
                Vector2i((centerMapped.first.x-dpix-1).toInt(),(centerMapped.first.y-dpix-1).toInt()),
                Vector2i((centerMapped.first.x+dpix+1).toInt(),(centerMapped.first.y+dpix+1).toInt())
            )
        )
    }
}

