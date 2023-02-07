package tech.softwarekitchen.tsr.light

import tech.softwarekitchen.common.vector.Vector3
import java.lang.Double.max

abstract class Light {
    abstract fun getForVector(vector: Vector3): Double
}

class AmbientLight(private val strength: Double): Light(){
    override fun getForVector(vector: Vector3): Double {
        return strength
    }
}

class DirectionalLight(private val strength: Double, private val direction: Vector3): Light(){
    override fun getForVector(vector: Vector3): Double {
        return max(0.0,-vector.norm().scalar(direction.norm()) * strength)
    }
}
