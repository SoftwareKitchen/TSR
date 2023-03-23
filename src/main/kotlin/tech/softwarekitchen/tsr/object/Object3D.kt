package tech.softwarekitchen.tsr.`object`

import tech.softwarekitchen.tsr.camera.Pixel
import tech.softwarekitchen.tsr.camera.ProjectionMatrix
import tech.softwarekitchen.tsr.light.Light
import tech.softwarekitchen.tsr.vector.Ray
import tech.softwarekitchen.tsr.vector.Rectangle2D
import java.util.*

abstract class RenderableObject3D{
    abstract fun process(ray: Ray, light: List<Light>, cutoff: Double): Pixel?
    abstract fun getMinimalDepth(): Double
    abstract fun getPixbound(): Rectangle2D
}

abstract class Object3D {
    private val cache = HashMap<String, RenderableObject3D>()
    abstract fun prepare(matrix: ProjectionMatrix): RenderableObject3D

    fun prepareCached(matrix: ProjectionMatrix): RenderableObject3D{
        val v = cache[matrix.uuid]
        if(v != null){
            return v
        }
        val n = prepare(matrix)
        cache[matrix.uuid] = n
        return n
    }

}
