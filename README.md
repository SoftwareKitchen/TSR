# Tahlaria's Software Renderer
## What is it?
It's a Kotlin-based software renderer.
## How does it work
You define a camera and a scene and call
``` kotlin
class Camera{
    ...
    fun render(s: Scene): BufferedImage
    ...
}
```
