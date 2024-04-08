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

    fun transitionNorth() = dirs.contains(Dir.N)
    fun transitionSouth() = dirs.contains(Dir.S)
}

data class Pos(var col: Int, var row: Int) {}

data class TrackedPos(val pos: Pos, val from: Dir) {
    fun apply(dir: Dir) = TrackedPos(dir.apply(pos), dir.from())
}

data class Cell(var pipe: Pipe, var start: Boolean = false, var path: Boolean = false) {

    fun plot() = if(path) "*" else pipe.symbol
}

data class Layout(val cells: List<List<Cell>>, val colCount: Int = cells.first().size, val rowCount: Int = cells.size, val start: Pos = findStart(cells)) {
    fun at(pos: Pos) = cells[pos.row][pos.col]
}

fun parseLayout(resource: String): Layout {
    val layout = Layout(Resources.readLines(Resources.getResource(resource), Charset.defaultCharset()).map {
        it.toCharArray()
            .map { chr -> Pipe.entries.find { pipe -> pipe.symbol == chr } }
            .filterNotNull()
            .map { pipe -> Cell(pipe) }
    })

    val startPos = findStart(layout.cells)
    val startCell = layout.at(startPos)
    startCell.start = true
    startCell.path = true

    // determine the shape of the start position
    val startDirs = Dir.entries
        .map { it to it.apply(startPos) }
        .filter {
            val p = it.second

            0 <= p.col && p.col < layout.colCount
                    && 0 <= p.row && p.row < layout.rowCount
                    && layout.at(p).pipe.dirs.contains(it.first.from())
        }
        .map { it.first }
    startCell.pipe = Pipe.entries.filter { it.dirs.containsAll(startDirs) }.first()

    println("directions out of start: $startDirs")


    return layout
}

fun findStart(cells: List<List<Cell>>): Pos {
    for (iRow in cells.indices) {
        val row = cells[iRow]
        for (iCol in row.indices) {
            if (row[iCol].pipe == Pipe.START) return Pos(iCol, iRow)
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
                    && layout.at(p).pipe.dirs.contains(it.first.from())
        }
        .map { TrackedPos(it.second, it.first.from()) }

fun traverse(layout: Layout, tracked: TrackedPos) =
    tracked.apply(
        layout.at(tracked.pos)
            .pipe.dirs.first { it != tracked.from }
    )

fun printLayout(layout: Layout) {
    println(
        layout.cells.map {
            row -> "" + row.map {
                cell -> cell.plot()
            }
            .joinToString("")
        }.joinToString("\n")

    )

}

fun path(resource: String): Int {
    val layout = parseLayout(resource)

    var tracked = connectedFrom(layout, layout.start)
    tracked.forEach {layout.at(it.pos).path = true }
    var step = 1

    println(
        """
        layout   : $layout
        step : 
          $step  $tracked
    """.trimIndent()
    )


    while (tracked.first().pos != tracked.last().pos) {
        tracked = tracked.map { traverse(layout, it) }.toList()
        tracked.forEach {layout.at(it.pos).path = true }
        ++step
        println("  $step  $tracked")
    }
    println("steps ahead : " + step)

    printLayout(layout)

    println("internal cells : " +
    layout.cells
        .map { row ->
            var transitionNorth = false
            var transitionSouth = false
            var inside = false
            var totalInside = 0
            for (cell in row) {
                if(cell.path) {
                    if (cell.pipe.transitionNorth()) {
                        if(transitionSouth) {
                            inside = !inside
                            transitionSouth = false
                        }
                        else transitionNorth = !transitionNorth
                    }
                    if (cell.pipe.transitionSouth()) {
                        if(transitionNorth) {
                            inside = !inside
                            transitionNorth = false
                        }
                        else transitionSouth = !transitionSouth
                    }
                }
                else if(inside) ++totalInside
            }
            totalInside
        }
        .sum()
    )

    return step
}

fun main() {
    path("2023/10/sample1") // 4, 1
    path("2023/10/sample2") // 8, 1
    path("2023/10/sample3") // 23, 4
    path("2023/10/puzzle") // 7005, 417
}