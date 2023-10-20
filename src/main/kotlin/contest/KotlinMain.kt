package contest

import framework.Framework
import java.util.Comparator
import java.util.PriorityQueue
import kotlin.math.abs

typealias TheMap = List<List<Char>>

data class Pos(val x: Int, val y: Int) {
    override fun toString(): String {
        return "($x,$y)"
    }
}

operator fun TheMap.get(pos: Pos): Char = this[pos.y][pos.x]

fun Pos.isValid(map: TheMap): Boolean {
    return x in map[0].indices && y in map.indices
}

fun Pos.getNeighbors(map: TheMap): List<Pos> = listOf(
    Pos(x - 1, y),
    Pos(x + 1, y),
    Pos(x, y - 1),
    Pos(x, y + 1),
).filter { it.isValid(map) }

fun Pos.distanceTo(other: Pos): Int {
    return abs(x - other.x) + abs(y - other.y)
}

fun String.toPos() = split(",").map { it.toInt() }.let { Pos(it[0], it[1]) }

fun main() {
    // challenge: sum of all numbers
    val level = 2

    val example = Framework.readInputLines(level, "example")
        .doLevel()

    println("Result:")
    println(example)

    for (i in 1..5) {
        val result = Framework.readInputLines(level, i.toString())
            .doLevel()
        Framework.writeOutput(level, i.toString(), result)
    }
}

fun List<String>.doLevel(): String {
    val text = this
    val dim = text.first().toInt()
    val map = text.slice(1..dim + 1).map { it.toList() }
    val numInts = text[dim + 1].toInt()
    return text.slice(dim + 2 until text.size)
        .map { it.split(" ").map { x -> x.toPos() }.zipWithNext().single() }
        .joinToString("\n") { pair ->
            val (a, b) = pair
            if (areSameIsland(a, b, map)) "SAME" else "DIFFERENT"
        }
}


fun areSameIsland(a: Pos, b: Pos, map: TheMap): Boolean {
    println("a: $a, b: $b")
    // make sure that both a and b are in the same "island" of L chars
    // use a star search with a priority queue
    // comparator compares distance to b
    val visited = PriorityQueue(Comparator<Pos> { x, y -> x.distanceTo(b) - y.distanceTo(b) })
    visited.add(a)
    val visitedSet = mutableSetOf(a)

    while (visited.isNotEmpty()) {
        val current = visited.poll()
        if (current == b) return true
        val neighbors = current.getNeighbors(map)
            .filter { map[it] == 'L' }
            .sortedBy { it.distanceTo(b) }
        for (neighbor in neighbors) {
            if (neighbor !in visitedSet) {
                visited.add(neighbor)
                visitedSet.add(neighbor)
            }
        }
    }
    return false
}
