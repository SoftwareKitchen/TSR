package tech.softwarekitchen.tsr.vector

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3

class Ray(val base: Vector3, val vec: Vector3, val scrCoo: Vector2i){
    fun at(lambda: Double): Vector3{
        return base.plus(vec.scale(lambda))
    }
}
