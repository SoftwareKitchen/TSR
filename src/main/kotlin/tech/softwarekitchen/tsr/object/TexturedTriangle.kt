package tech.softwarekitchen.tsr.`object`

import tech.softwarekitchen.common.matrix.Matrix22
import tech.softwarekitchen.common.matrix.Matrix33
import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.tsr.camera.Pixel
import tech.softwarekitchen.tsr.camera.ProjectionMatrix
import tech.softwarekitchen.tsr.color.Color
import tech.softwarekitchen.tsr.light.Light
import tech.softwarekitchen.tsr.vector.Ray
import tech.softwarekitchen.tsr.vector.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.lang.Double.max
import java.lang.Double.min
import java.lang.Math.pow
import kotlin.math.acos
import kotlin.math.sin

class OptimizedTextureTriangle(
    private val base: TexturedTriangle,
    private val vec12: Vector3,
    private val vec13: Vector3,
    private val vec23: Vector3,
    private val ortho: Vector3,
    private val ortho12: Vector3,
    private val ortho13: Vector3,
    private val ortho23: Vector3,
    private val off12: Double,
    private val off13: Double,
    private val off23: Double,
    private val p1ScalarOrtho: Double,
    private val off12Low: Double,
    private val off13Low: Double,
    private val off23Low: Double,
    private val off12High: Double,
    private val off13High: Double,
    private val off23High: Double,
    private val _minimalDepth: Double,
    private val _pixbound: Rectangle2D
): RenderableObject3D(){
    override fun getPixbound(): Rectangle2D {
        return _pixbound
    }

    override fun getMinimalDepth(): Double {
        return _minimalDepth
    }

    private fun mapOnTexture(vec: Vector3): Color{
        val triMatrix = Matrix33.fromColumnVectors(vec12, vec13, vec12.ortho(vec13))
        val invTriMatrix = triMatrix.invert()

        val (phi, psi, _) = invTriMatrix.mul(vec.minus(base.p1)).expand()

        val texBase = base.texture.tp1
        val tex12 = base.texture.tp2.minus(base.texture.tp1)
        val tex13 = base.texture.tp3.minus(base.texture.tp1)

        val texLoc = texBase.plus(tex12.scale(phi)).plus(tex13.scale(psi))
        val texPix = Vector2i((texLoc.x * base.texture.img.width).toInt(), (texLoc.y * base.texture.img.height).toInt())

        if(texPix.x < 0 || texPix.y < 0 || texPix.x >= base.texture.img.width || texPix.y >= base.texture.img.height){
            return Color(0,0,0,0)
        }

        val rgb = base.texture.img.getRGB(texPix.x, texPix.y).toUInt()

        return Color.fromRGB(rgb)
    }

    override fun process(ray: Ray, light: List<Light>, cutoff: Double): Pixel? {
        val delta = ray.vec.scalar(ortho)
        if(delta == 0.0){
            return null
        }
        val baseOffset = ray.base.scalar(ortho) - p1ScalarOrtho
        val rayLambda = -baseOffset / delta

        if(cutoff > 0 && rayLambda > cutoff){
            return null
        }

        val pos = ray.base.plus(ray.vec.scale(rayLambda))

        val po12 = pos.scalar(ortho12)
        if(po12 < off12Low || po12 > off12High){
            return null
        }

        val po13 = pos.scalar(ortho13)
        if(po13 < off13Low || po13 > off13High){
            return null
        }

        val po23 = pos.scalar(ortho23)
        if(po23 < off23Low || po23 > off23High){
            return null
        }

        val lightSum = min(light.map{max(it.getForVector(ortho), it.getForVector(ortho.invert()))}.sum(),1.0)
        return Pixel(rayLambda, mapOnTexture(pos),lightSum)
    }
}

data class TextureSection(val img: BufferedImage, val tp1: Vector2, val tp2: Vector2, val tp3: Vector2)
class TexturedTriangle(
    val p1: Vector3,
    val p2: Vector3,
    val p3: Vector3,
    val texture: TextureSection
): Object3D(){
    override fun prepare(matrix: ProjectionMatrix): RenderableObject3D {
        val vec12 = p2.minus(p1)
        val vec13 = p3.minus(p1)
        val vec23 = p3.minus(p2)
        val ortho = vec12.ortho(vec13).norm()
        val ortho12 = vec12.ortho(ortho).norm()
        val ortho13 = vec13.ortho(ortho).norm()
        val ortho23 = vec23.ortho(ortho).norm()

        var off12Low = 0.0
        var off12High = 0.0
        var off13Low = 0.0
        var off13High = 0.0
        var off23Low = 0.0
        var off23High = 0.0

        val off12 = vec13.scalar(ortho12)
        val off13 = vec12.scalar(ortho13)
        val off23 = vec12.invert().scalar(ortho23)

        if(off12 < 0){
            off12Low = p3.scalar(ortho12)
            off12High = p1.scalar(ortho12)
        }else{
            off12Low = p1.scalar(ortho12)
            off12High = p3.scalar(ortho12)
        }
        if(off13 < 0){
            off13Low = p2.scalar(ortho13)
            off13High = p1.scalar(ortho13)
        }else{
            off13Low = p1.scalar(ortho13)
            off13High = p2.scalar(ortho13)
        }
        if(off23 < 0){
            off23Low = p1.scalar(ortho23)
            off23High = p2.scalar(ortho23)
        }else{
            off23Low = p2.scalar(ortho23)
            off23High = p1.scalar(ortho23)
        }

        val camd1 = matrix.base.dist(p1)
        val camd2 = matrix.base.dist(p2)
        val camd3 = matrix.base.dist(p3)

        val translatedP1 = matrix.mapPoint(p1)
        val translatedP2 = matrix.mapPoint(p2)
        val translatedP3 = matrix.mapPoint(p3)

        val pixXMin = kotlin.math.min(kotlin.math.min(translatedP1.first.x,translatedP2.first.x),translatedP3.first.x)
        val pixXMax = kotlin.math.max(kotlin.math.max(translatedP1.first.x,translatedP2.first.x),translatedP3.first.x)
        val pixYMin = kotlin.math.min(kotlin.math.min(translatedP1.first.y,translatedP2.first.y),translatedP3.first.y)
        val pixYMax = kotlin.math.max(kotlin.math.max(translatedP1.first.y,translatedP2.first.y),translatedP3.first.y)

        return OptimizedTextureTriangle(
            this,
            vec12,vec13,vec23,
            ortho,
            ortho12,ortho13,ortho23,
            off12,off13,off23,
            p1.scalar(ortho),
            off12Low, off13Low, off23Low,
            off12High, off13High, off23High,min(min(camd1,camd2),camd3),
            Rectangle2D(Vector2i(pixXMin.toInt(),pixYMin.toInt()),Vector2i(pixXMax.toInt(),pixYMax.toInt()))
        )
    }

}
