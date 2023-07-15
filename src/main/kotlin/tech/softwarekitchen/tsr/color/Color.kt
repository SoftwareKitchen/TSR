package tech.softwarekitchen.tsr.color

data class Color(val r: Int, val g: Int, val b: Int, val a: Int){
    companion object{
        fun fromARGB(argb: UInt): Color{
            val a = argb / 16777216u
            val r = (argb / 65536u) % 256u
            val g = (argb / 256u) % 256u
            val b = argb % 256u
            return Color(r.toInt(),g.toInt(),b.toInt(),a.toInt())
        }

        fun fromRGB(rgb: UInt): Color{
            val r = (rgb / 65536u) % 256u
            val g = (rgb / 256u) % 256u
            val b = rgb % 256u
            return Color(r.toInt(),g.toInt(),b.toInt(),255)
        }
    }
    fun toARGB(): Int{
        return (16777216u * a.toUInt() + 65536u * r.toUInt() + 256u * g.toUInt() + b.toUInt()).toInt()
    }

}
