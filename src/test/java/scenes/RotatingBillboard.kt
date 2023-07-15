package scenes

import org.junit.jupiter.api.Test
import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.tsr.camera.Camera
import tech.softwarekitchen.tsr.color.Color
import tech.softwarekitchen.tsr.light.AmbientLight
import tech.softwarekitchen.tsr.`object`.Object3D
import tech.softwarekitchen.tsr.`object`.TextureSection
import tech.softwarekitchen.tsr.`object`.TexturedTriangle
import tech.softwarekitchen.tsr.scene.Scene
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class RotatingBillboard {
    @Test
    fun renderRotatingBillboard(){
        val images = 20
        val degreesBetween = 15
        val directory = "./"
        val prefix = "rbb_"

        val cam = Camera(Vector3(10.0,0.0,0.0),Vector3(-1.0, 0.0,0.0), Vector3(0.0,1.0,0.0),45.0, Vector2i(640,480), Color(0,0,0,0), listOf(AmbientLight(1.0)))

        val imgRaw = javaClass.getResource("/image.png")!!
        val img = ImageIO.read(imgRaw)

        val generateTriangles: (Double) -> List<Object3D> = {
            degrees ->
            val rad = PI * degrees / 180.0

            val texTopLeft = Vector2(0.0,0.0)
            val texTopRight = Vector2(1.0, 0.0)
            val texBottomLeft = Vector2(0.0,1.0)
            val texBottomRight = Vector2(1.0,1.0)

            val halfWidth = 1.5
            val halfHeight = 1.0

            val bottomLeft = Vector3(- sin(rad) * halfWidth, - halfHeight, - cos(rad) * halfHeight)
            val bottomRight = Vector3( sin(rad) * halfWidth, - halfHeight, cos(rad) * halfHeight)

            val topLeft = Vector3(- sin(rad) * halfWidth, halfHeight, - cos(rad) * halfHeight)
            val topRight = Vector3( sin(rad) * halfWidth, halfHeight, cos(rad) * halfHeight)

            val tri1 = TexturedTriangle(
                bottomLeft, bottomRight, topLeft, TextureSection(img, texBottomLeft, texBottomRight, texTopLeft)
            )
            val tri2 = TexturedTriangle(
                topRight, bottomRight, topLeft, TextureSection(img, texTopRight, texBottomRight, texTopLeft)
            )

            listOf(tri1, tri2)
        }

        for(i in 0 until images){
            val degrees = (degreesBetween * i).toDouble()

            val triangles = generateTriangles(degrees)
            val scene = Scene(triangles)

            val rendered = cam.render(scene)

            ImageIO.write(rendered, "png", File("$directory$prefix${i.toString().padStart(3,'0')}.png"))
        }
    }
}