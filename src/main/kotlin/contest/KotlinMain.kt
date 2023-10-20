package contest

import framework.Framework
import java.lang.StringBuilder

fun main() {
    // challenge: sum of all numbers
    val level = 1
    for (i in 1..5) {
        val text = Framework.readInputLines(level, i)
        val dim = text.first().toInt()
        val map = text.slice(1..dim + 1).map { it.toList() }
        val numInts = text[dim + 1].toInt()

        println(map)
        println(dim)
        println(numInts)

        val coords = text.slice(dim + 2 until text.size)

        val sb = StringBuilder()
        for (num in 0 until numInts) {
            val (x, y) = coords[num].split(",").map { it.toInt() }
            sb.appendLine(map[y][x])
        }
        Framework.writeOutput(level, i, sb.toString())
    }
}
