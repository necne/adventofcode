package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset

// https://adventofcode.com/2023/day/10

enum class Dir(val modCol: Int, val modRow: Int) {
    N(0, -1) {
        override fun from() = S
    },
    E(1, 0) {
        override fun from() = W
    },
    S(0, 1) {
        override fun from() = N
    },
    W(-1, 0) {
        override fun from() = E
    },
    ;

    fun apply(pos: Pos) = Pos(pos.col + modCol, pos.row + modRow)

    abstract fun from(): Dir
}

enum class Pipe(val symbol: Char, val dirs: List<Dir>) {
    NS('|', listOf(Dir.N, Dir.S)),
    WE('-', listOf(Dir.W, Dir.E)),
    NE('L', listOf(Dir.N, Dir.E)),
    NW('J', listOf(Dir.N, Dir.W)),
    SW('7', listOf(Dir.S, Dir.W)),
    SE('F', listOf(Dir.S, Dir.E)),
    GROUND('.', listOf()),
    START('S', listOf()), ;
}

data class Pos(var col: Int, var row: Int) {}

data class TrackedPos(val pos: Pos, val from: Dir) {
    fun apply(dir: Dir) = TrackedPos(dir.apply(pos), dir.from())
}

data class Layout(val pipes: List<List<Pipe>>, val colCount: Int = pipes.first().size, val rowCount: Int = pipes.size) {
    fun at(pos: Pos) = pipes[pos.row][pos.col]
}

fun parseLayout(resource: String) =
    Layout(Resources.readLines(Resources.getResource(resource), Charset.defaultCharset()).map {
        it.toCharArray().map { chr -> Pipe.entries.find { pipe -> pipe.symbol == chr } }.filterNotNull()
    })


fun findStart(layout: Layout): Pos {
    for (iRow in layout.pipes.indices) {
        val row = layout.pipes[iRow]
        for (iCol in row.indices) {
            if (row[iCol] == Pipe.START) return Pos(iCol, iRow)
        }
    }
    throw Exception("Unable to determine start from layout")
}

fun connectedFrom(layout: Layout, pos: Pos) =
    Dir.entries
        .map { it to it.apply(pos) }
        .filter {
            val p = it.second

            0 <= p.col && p.col < layout.colCount
                    && 0 <= p.row && p.row < layout.rowCount
                    && layout.at(p).dirs.contains(it.first.from())
        }
        .map { TrackedPos(it.second, it.first.from()) }

fun traverse(layout: Layout, tracked: TrackedPos) =
    tracked.apply(
        layout.at(tracked.pos)
            .dirs.first { it != tracked.from }
    )

fun path(resource: String) {
    val layout = parseLayout(resource)
    val posStart = findStart(layout)

    var tracked = connectedFrom(layout, posStart)
    var step = 1

    println(
        """
        layout   : $layout
        start    : $posStart
        step : 
          $step  $tracked
    """.trimIndent()
    )

    while (tracked.first().pos != tracked.last().pos) {
        tracked = tracked.map { traverse(layout, it) }.toList()
        ++step
        println("  $step  $tracked")
    }
    println("steps ahead : " + step)
}

fun main() {
    path("2023/10/sample1") // 4
    path("2023/10/sample2") // 8
    path("2023/10/puzzle") // 7005
}