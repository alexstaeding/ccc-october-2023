package contest

import framework.Framework
import java.awt.geom.Line2D
import java.util.PriorityQueue
import kotlin.math.abs

typealias TheMap = List<List<Char>>

data class Pos(val x: Int, val y: Int) {
    override fun toString(): String {
        return "$x,$y"
//        return "${y + 2}:${x + 1}"
    }
}

class Node(val pos: Pos, val parent: Node? = null) {
    override fun toString(): String {
        return "($pos,p=${parent?.pos})"
    }
}

fun Node.buildPath(): Sequence<Pos> {
    return generateSequence(this) { it.parent }
        .map { it.pos }
        .toList()
        .reversed()
        .asSequence()
}

operator fun Pos.plus(other: Pos): Pos = Pos(x + other.x, y + other.y)
operator fun TheMap.get(pos: Pos): Char = this[pos.y][pos.x]

fun Pos.isValid(map: TheMap): Boolean {
    return y in map.indices && x in map[y].indices
}

fun Pos.getNeighbors(map: TheMap): Sequence<Pos> = sequenceOf(
    Pos(x - 1, y),
    Pos(x + 1, y),
    Pos(x, y - 1),
    Pos(x, y + 1),
    // diagonals
    Pos(x - 1, y - 1),
    Pos(x + 1, y - 1),
    Pos(x - 1, y + 1),
    Pos(x + 1, y + 1),
).filter { it.isValid(map) }

fun Pos.getTouchingNeighbors(map: TheMap): Sequence<Pos> = sequenceOf(
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
    val level = 5

    val example = Framework.readInputLines(level, "example")
        .doEncirclingLevel()

    println("Result:")
    println(example)

//    for (i in 1..5) {
//        println("Running $i")
//        val result = Framework.readInputLines(level, i.toString())
//            .doEncirclingLevel()
//        Framework.writeOutput(level, i.toString(), result)
//    }
}

fun List<String>.doEncirclingLevel(): String {
    val text = this
    val dim = text.first().toInt()
    val map = text.slice(1..dim + 1).map { it.toList() }
    return text.slice(dim + 2 until text.size)
        .map { it.toPos() }
//        .slice(1..1)
        .joinToString("\n") { pos ->
            println("Tracing $pos")
            pos.traceIsland(map).joinToString(" ")
        }
}

fun Pos.traceIsland(map: TheMap): List<Pos> {
    // find nearest W point from this L point
    val waterStart = findNearest(map, this) { map[it] == 'W' } ?: error("No water on island with $this")

//    println("====")

//    println("Waterstart: $waterStart")

    // trace the island, only move to adjacent W points that have an adjacent L point

    val visited = mutableListOf(Node(waterStart))
    val visitedSet = hashSetOf(waterStart)

    var iteration = 0
    while (visited.isNotEmpty()) {
        val current = visited.removeFirst()

        val neighbors = current.pos.getNeighbors(map)
            .filter { map[it] == 'W' }
//            .filter { it !in visitedSet }
            .filter { it.getNeighbors(map).any { x -> areSameIsland(this, x, map) } }
            .toList()

        println("$current -> $neighbors")

        if (iteration > 4 && neighbors.any { it in visitedSet}) {
            println("Cross")
        }

        val notVisitedNeighbors = neighbors.filter { it !in visitedSet }

        for (neighbor in neighbors) {
            // check if two paths cross
            visited.add(Node(neighbor, current))
            visitedSet.add(neighbor)
        }

        iteration++
    }
    error("No path found for $this water start $waterStart")
}

fun List<String>.doPathFindingLevel(): String {
    val text = this
    val dim = text.first().toInt()
    val map = text.slice(1..dim + 1).map { it.toList() }
    return text.slice(dim + 2 until text.size)
        .map { it.split(" ").map { x -> x.toPos() }.zipWithNext().single() }
        .joinToString("\n") { line ->
            line.doTheRouteThing(map)
        }
}

fun Pair<Pos, Pos>.doTheRouteThing(map: TheMap): String {
    val route = findRoute(first, second, map)
    return route.joinToString(" ") { it.toString() }
}


fun List<Pos>.hasSelfIntersection(): Boolean {
    if (toSet().size != size) return true
    // check whether the line made by a series x,y coordinates cross
    // it may happen that the line crosses itself without any of the points being repeated

    // create a line segment for each pair of points and check if any intersect
    return asSequence().hasDiagonalIntersection()
}

fun Sequence<Pos>.hasDiagonalIntersection(): Boolean {
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
//                println("i: $i, j: $j")
                return true
            }
        }
    }

    return false
}

fun findRoute(a: Pos, b: Pos, map: TheMap): List<Pos> {
    // make sure that both a and b are in the same "island" of L chars
    // use a star search with a priority queue
    // comparator compares distance to b

    val visited = PriorityQueue(Comparator<Node> { x, y -> x.pos.distanceTo(b) - y.pos.distanceTo(b) })
    visited.add(Node(a, null))
    val visitedSet = hashSetOf(a)
    while (visited.isNotEmpty()) {
        val current = visited.poll()
        if (current.pos == b) {
            return current.buildPath().toList()
        }

        val comparator = Comparator<Pos> { x, y -> x.distanceTo(b) - y.distanceTo(b) }

        val neighbors = current.pos.getNeighbors(map)
            .filter { map[it] == 'W' }
            .sortedWith(comparator)


        for (neighbor in neighbors) {
            if (neighbor !in visitedSet && !(current.buildPath() + neighbor).hasDiagonalIntersection()) {
                visited.add(Node(neighbor, current))
                visitedSet.add(neighbor)
            }
        }
    }
    return emptyList()
}

fun findNearest(map: TheMap, start: Pos, endcondition: (Pos) -> Boolean): Pos? {
    class Node(val pos: Pos, val parent: Node?)

    val visited = PriorityQueue(Comparator<Node> { x, y -> x.pos.distanceTo(start) - y.pos.distanceTo(start) })
    visited.add(Node(start, null))
    val visitedSet = hashSetOf(start)
    while (visited.isNotEmpty()) {
        val current = visited.poll()
//        println("Visited: ${current.pos}")
        if (endcondition(current.pos)) {
            return current.pos
        }

        val neighbors = current.pos.getNeighbors(map)

        for (neighbor in neighbors) {
            if (neighbor !in visitedSet) {
                visited.add(Node(neighbor, current))
                visitedSet.add(neighbor)
            }
        }
    }
    return null
}

fun areSameIsland(a: Pos, b: Pos, map: TheMap): Boolean {
    // make sure that both a and b are in the same "island" of L chars
    // use a star search with a priority queue
    // comparator compares distance to b
    val visited = PriorityQueue(Comparator<Pos> { x, y -> x.distanceTo(b) - y.distanceTo(b) })
    visited.add(a)
    val visitedSet = mutableSetOf(a)

    while (visited.isNotEmpty()) {
        val current = visited.poll()
        if (current == b) return true
        val neighbors = current.getTouchingNeighbors(map)
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
