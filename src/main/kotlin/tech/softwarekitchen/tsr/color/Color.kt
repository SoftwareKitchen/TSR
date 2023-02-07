package tech.softwarekitchen.tsr.color

data class Color(val r: Int, val g: Int, val b: Int, val a: Int){
    fun toARGB(): Int{
        return (16777216u * a.toUInt() + 65536u * r.toUInt() + 256u * g.toUInt() + b.toUInt()).toInt()
    }
}
