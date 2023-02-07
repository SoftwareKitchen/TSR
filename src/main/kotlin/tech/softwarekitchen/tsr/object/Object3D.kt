package tech.softwarekitchen.tsr.`object`

import tech.softwarekitchen.tsr.camera.Pixel
import tech.softwarekitchen.tsr.camera.ProjectionMatrix
import tech.softwarekitchen.tsr.light.Light
import tech.softwarekitchen.tsr.vector.Ray
import tech.softwarekitchen.tsr.vector.Rectangle2D

abstract class RenderableObject3D{
    abstract fun process(ray: Ray, light: List<Light>, cutoff: Double): Pixel?
    abstract fun getMinimalDepth(): Double
    abstract fun getPixbound(): Rectangle2D
}

abstract class Object3D {
    abstract fun prepare(matrix: ProjectionMatrix): RenderableObject3D
}
