package tech.softwarekitchen.tsr.camera

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.tsr.color.Color
import tech.softwarekitchen.tsr.scene.Scene
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.lang.Integer.min
import kotlin.math.max

class Pixel(val depth: Double, val color: Color, val light: Double)

class PixelBuffer{
    private val data = ArrayList<Pixel>()
    fun reset(){
        data.clear()
    }

    fun getCutoff(): Double{
        for(pixel in data){
            if(pixel.color.a == 255){
                return pixel.depth
            }
        }
        return -1.0
    }

    fun put(pixel: Pixel){
        data.add(pixel)
        data.sortBy { it.depth }

        if(pixel.color.a == 255){
            val index = data.indexOf(pixel)
            while(data.size > index + 1){
                data.removeLast()
            }
        }
    }

    fun merge(): Color{
        var r = 0
        var g = 0
        var b = 0
        data.reversed().forEach{
            val a = it.color.a
            r = ((a * it.color.r * it.light).toInt() + (255 - a) * r) / 255
            g = ((a * it.color.g * it.light).toInt() + (255 - a) * g) / 255
            b = ((a * it.color.b * it.light).toInt() + (255 - a) * b) / 255
        }
        return Color(r,g,b,255)
    }
}

class Camera(
    private val base: Vector3,
    front: Vector3,
    up: Vector3,
    private val fov: Double,
    private val imageSize: Vector2i
) {
    private val front = front.norm()
    private val right = front.ortho(up).norm()
    private val up = front.ortho(right).invert().norm()
    private val buffer = Array(imageSize.x){Array(imageSize.y){PixelBuffer()} }
    private val matrix = ProjectionMatrix(base,front,up,fov,imageSize)
    private val imageBuffer = Array(imageSize.x){Array(imageSize.y){PixelBuffer()} }

    fun render(s: Scene): BufferedImage{
        imageBuffer.forEach{
            line ->
            line.forEach{
                cell -> cell.reset()
            }
        }

        val preparedObjects = s.objects.map{it.prepare(matrix)}.sortedWith{i1,i2 -> (i2.getMinimalDepth()-i1.getMinimalDepth()).toInt()}

        preparedObjects.forEach {
            val pixbound = it.getPixbound()
            for(x in max(0,pixbound.topLeft.x) until min(imageSize.x, pixbound.bottomRight.x)){
                for( y in max(0,pixbound.topLeft.y) until min(imageSize.y, pixbound.bottomRight.y)){
                    val currentCutoff = imageBuffer[x][y].getCutoff()
                    val res = it.process(matrix.getRayForScreenCoordinates(x,y),s.lights,currentCutoff)
                    res?.let{
                        if(currentCutoff < 0 || (res.depth > 0 && res.depth < currentCutoff)){
                            imageBuffer[x][y].put(res)
                        }
                    }
                }
            }
        }

        val img = BufferedImage(imageSize.x,imageSize.y,TYPE_INT_ARGB)

        for(x in 0 until imageSize.x){
            for(y in 0 until imageSize.y){
                img.setRGB(x,y,imageBuffer[x][y].merge().toARGB())
            }
        }

        return img
    }
}
