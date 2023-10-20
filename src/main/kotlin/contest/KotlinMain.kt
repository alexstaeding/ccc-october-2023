package contest

import framework.Framework

fun main() {
    // challenge: sum of all numbers
    val level = 0
    for (i in 1..2) {
        val sum = Framework.readInput(level, i)
            .split(" ")
            .sumOf { it.toInt() }

        println("Level $level, Input $i = $sum")
        Framework.writeOutput(level, i, sum.toString())
    }
}
