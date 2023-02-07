package tech.softwarekitchen.tsr.scene

import tech.softwarekitchen.tsr.light.Light
import tech.softwarekitchen.tsr.`object`.Object3D

data class Scene(val objects : List<Object3D>, val lights: List<Light>)

