package contest

import framework.Framework
import java.awt.geom.Line2D
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
    val level = 3

    val example = Framework.readInputLines(level, "example")
        .doIntersection()

    println("Result:")
    println(example)

    for (i in 1..5) {
        val result = Framework.readInputLines(level, i.toString())
            .doIntersection()
        Framework.writeOutput(level, i.toString(), result)
    }
}

fun List<String>.doIntersection(): String {
    val text = this
    val dim = text.first().toInt()
    val map = text.slice(1..dim + 1).map { it.toList() }
    return text.slice(dim + 2 until text.size)
        .map { it.split(" ").map { x -> x.toPos() } }
        .joinToString("\n") { line ->
            if (line.checkSelfIntersection()) "INVALID" else "VALID"
        }
}


fun List<Pos>.checkSelfIntersection(): Boolean {
    if (toSet().size != size) return true
    val visited = mutableSetOf<Pos>()
    // check whether the line made by a series x,y coordinates cross
    // it may happen that the line crosses itself without any of the points being repeated

    // create a line segment for each pair of points and check if any intersect

    val pairs = windowed(2, 1)

    for (i in pairs) {
        for (j in pairs) {
            // check if any of the points are the same
            if (i[0] == j[0] || i[0] == j[1] || i[1] == j[0] || i[1] == j[1]) continue
            if (Line2D.linesIntersect(
                    i[0].x.toDouble(), i[0].y.toDouble(), i[1].x.toDouble(), i[1].y.toDouble(),
                    j[0].x.toDouble(), j[0].y.toDouble(), j[1].x.toDouble(), j[1].y.toDouble(),
                )
            ) {
                println("i: $i, j: $j")
                return true
            }
        }
    }

    return false
}

fun List<String>.doLevel(): String {
    val text = this
    val dim = text.first().toInt()
    val map = text.slice(1..dim + 1).map { it.toList() }
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
