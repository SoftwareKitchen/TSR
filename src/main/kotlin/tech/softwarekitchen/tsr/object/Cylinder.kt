package tech.softwarekitchen.tsr.`object`

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.tsr.camera.Pixel
import tech.softwarekitchen.tsr.camera.ProjectionMatrix
import tech.softwarekitchen.tsr.color.Color
import tech.softwarekitchen.tsr.light.Light
import tech.softwarekitchen.tsr.vector.Ray
import tech.softwarekitchen.tsr.vector.Rectangle2D
import kotlin.math.max
import kotlin.math.min

/*
    cylinder -> base + vector

    any point: delta,

    Ray = base + lambda * vector
    delta-> = (ray_base + lambda * vector) - cylinder_base
    long = delta-> o cyl_vec
    -> Ausschluss 0;1
    trans-> = (ray_base + lambda * ray_vec) - cylinder_base - cyl_vec * (((ray_base + lambda * vector) - cylinder_base) o cyl_vec)
    trans-> = ray_base + lambda * ray_vec - cylinder_base - cyl_vec * (ray_base o cyl_vec + lambda * ray_vec o cyl_vec - cylinder_base o cyl_vec)
    trans-> = ray_base + lambda * ray_vec - cylinder_base - cyl_vec * (ray_base o cyl_vec) - cyl_vec * (lambda * ray_vec o cyl_vec) + cyl_vec * (cylinder_base o cyl_vec)

    trans-> = ray_base + lambda * ray_vec - cylinder_base - cyl_vec * (ray_base o cyl_vec) - cyl_vec * (lambda * ray_vec o cyl_vec) + cyl_vec * (cylinder_base o cyl_vec)
    rho := ray_vec o cyl_vec
    trans-> = ray_base + lambda * ray_vec - cylinder_base - rho * cyl_vec - cyl_vec * rho * lambda + cyl_vec * (cylinder_base o cyl_vec)
    trans-> = ray_base - cylinder_base - rho * cyl_vec + cyl_vec * (cylinder_base o cyl_vec) + lambda * (ray_vec - cyl_vec * rho)
    phi := ray_vec - cyl_vec * rho
    sigma := ray_base - cylinder_base - rho*cyl_vec + cyl_vec * (cylinder_base o cyl_vec)
    trans-> = sigma + lambda * phi
    d² = sigma² +2*lambda*phi*sigma + lambda²phi²
    a = phi²
    b = 2*phi*sigma
    c = sigma² - d²
 */


class OptimizedCylinder(
    private val base: Cylinder,
    private val matrix: ProjectionMatrix
): RenderableObject3D(){
    private val pixbound: Rectangle2D
    private val minDepth = min(matrix.mapPoint(base.base).second, matrix.mapPoint(base.base.plus(base.vector)).second) - base.radius

    init{
        val p1 = matrix.mapPoint(base.base)
        val p2 = matrix.mapPoint(base.base.plus(base.vector.scale(base.length)))

        val angle1 = Math.atan2(base.radius, p1.second)
        val angle2 = Math.atan2(base.radius, p2.second)

        val minX = min(p1.first.x - matrix.radToPixels(angle1), p2.first.x - matrix.radToPixels(angle2)).toInt() - 1
        val maxX = max(p1.first.x + matrix.radToPixels(angle1), p2.first.x + matrix.radToPixels(angle2)).toInt() + 1
        val minY = min(p1.first.y - matrix.radToPixels(angle1), p2.first.y - matrix.radToPixels(angle2)).toInt() - 1
        val maxY = max(p1.first.y + matrix.radToPixels(angle1), p2.first.y + matrix.radToPixels(angle2)).toInt() + 1
        pixbound = Rectangle2D(Vector2i(minX,minY), Vector2i(maxX, maxY))
    }

    override fun process(ray: Ray, light: List<Light>, cutoff: Double): Pixel? {
        val gammaVec = ray.base.minus(base.base)
        val rayVecMulCylVec = ray.vec.scalar(base.vector)
        val rayVecMulCylVecSquared = rayVecMulCylVec * rayVecMulCylVec
        val a = (ray.vec.squared() - rayVecMulCylVecSquared)
        val b = 2*(ray.vec.scalar(gammaVec) - rayVecMulCylVec* gammaVec.scalar(base.vector))
        val c = gammaVec.squared() - Math.pow(gammaVec.scalar(base.vector),2.0) - base.radius*base.radius

        val dis = b*b - 4*a*c
        if(dis < 0){
            return null
        }
        val sqdis = Math.sqrt(dis)
        val lambda1 = (-b - sqdis) / (2*a)
        val lambda2 = (-b + sqdis) / (2*a)

        if(lambda2 < 0){
            return null
        }

        val lambda = when(lambda1 > 0){
            true -> lambda1
            false -> lambda2
        }

        val cylBaseToHitPoint = ray.base.minus(base.base).plus(ray.vec.scale(lambda))
        val longit =  base.vector.norm().scalar(cylBaseToHitPoint)

        if(longit < 0 || longit > base.length){
            return null
        }

        val at = ray.at(lambda)
        val fromCenter = at.minus(base.base)

        val lightVec = fromCenter.minus(base.vector.scale(fromCenter.scalar(base.vector)))
        val lightSum = java.lang.Double.min(light.map {
            it.getForVector(lightVec)
        }.sum(), 1.0)

        return Pixel(lambda,base.color,lightSum)
    }

    override fun getMinimalDepth(): Double {
        return minDepth
    }

    override fun getPixbound(): Rectangle2D {
        return pixbound
    }

}

class Cylinder(val base: Vector3, vector: Vector3, val radius: Double, val color: Color): Object3D() {
    val length = vector.length()
    val vector = vector.norm()
    override fun prepare(matrix: ProjectionMatrix): RenderableObject3D {
        return OptimizedCylinder(this, matrix)
    }
}

