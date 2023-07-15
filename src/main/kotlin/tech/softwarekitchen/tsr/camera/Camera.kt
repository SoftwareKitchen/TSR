package tech.softwarekitchen.tsr.camera

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.tsr.color.Color
import tech.softwarekitchen.tsr.light.Light
import tech.softwarekitchen.tsr.scene.Scene
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.lang.Integer.min
import kotlin.math.max

class Pixel(val depth: Double, val color: Color, val light: Double)

class PixelBuffer(
    private val defaultColor: Color
){
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
        var r = defaultColor.r
        var g = defaultColor.g
        var b = defaultColor.b
        var a = defaultColor.a
        data.reversed().forEach{
            val curA = it.color.a
            r = ((curA * it.color.r * it.light).toInt() + (255 - curA) * r) / 255
            g = ((curA * it.color.g * it.light).toInt() + (255 - curA) * g) / 255
            b = ((curA * it.color.b * it.light).toInt() + (255 - curA) * b) / 255
            a = (curA * 255 + (255 - curA) * a) / 255
        }
        return Color(r,g,b,a)
    }
}

data class CachedPixel(val x: Int, val y: Int, val data: Pixel)

class Camera(
    private val base: Vector3,
    front: Vector3,
    up: Vector3,
    private val fov: Double,
    private val imageSize: Vector2i,
    private val defaultColor: Color,
    private val light: List<Light>
) {
    private val matrix = ProjectionMatrix(base,front,up,fov,imageSize)
    private val imageBuffer = Array(imageSize.x){Array(imageSize.y){PixelBuffer(defaultColor)} }

    fun render(s: Scene): BufferedImage{
        imageBuffer.forEach{
            line ->
            line.forEach{
                cell -> cell.reset()
            }
        }

        val preparedObjects = s.objects.map{it.prepareCached(matrix)}.sortedBy{it.getMinimalDepth()}

        preparedObjects.forEach {
            val pixbound = it.getPixbound()
            val cache = ArrayList<CachedPixel>()
            for(x in max(0,pixbound.topLeft.x) until min(imageSize.x, pixbound.bottomRight.x)){
                for( y in max(0,pixbound.topLeft.y) until min(imageSize.y, pixbound.bottomRight.y)){
                    val currentCutoff = imageBuffer[x][y].getCutoff()
                    val res = it.process(matrix.getRayForScreenCoordinates(x,y),light,currentCutoff)
                    res?.let{
                        if(res.depth > 0){
                            cache.add(CachedPixel(x,y,it))
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
