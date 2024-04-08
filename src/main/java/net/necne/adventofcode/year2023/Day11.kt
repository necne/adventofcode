package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset
import kotlin.math.abs

// https://adventofcode.com/2023/day/11

data class Galaxy(var col : Long, var row : Long){
    fun expand(col : Long, row : Long) {
        this.col += col
        this.row += row
    }
}

fun print(layout: List<List<Char>>) =
    println(
        layout.map { row -> "" + row.joinToString("") }
            .joinToString("\n")
    )

fun stepsBetween(galaxyA: Galaxy, galaxyB: Galaxy) = abs(galaxyA.row - galaxyB.row) + abs(galaxyA.col - galaxyB.col)

fun parseGalaxy(resource: String, expansion: Int) {
    val netExpansion = (expansion - 1).toLong()
    val layout = Resources.readLines(Resources.getResource(resource), Charset.defaultCharset())
        .map {it.toCharArray().toMutableList() }
        .toMutableList()
    print(layout)

    val galaxys = mutableListOf<Galaxy>()
    for( row in 0..<layout.size )
        for( col in 0..<layout.first().size)
            if( layout[row][col] == '#' ) galaxys.add(Galaxy(col.toLong(), row.toLong()))
    println("initial galaxys\n$galaxys")

    for ( row in layout.size-1 downTo 0 )
        if (layout[row].none { it == '#' }) {
            galaxys.filter { it.row > row }.forEach { it.expand(0, netExpansion) }
        }

    for ( col in layout.first().size-1 downTo 0)
        if(layout.none { it[col] == '#' }) {
            galaxys.filter { it.col > col }.forEach { it.expand(netExpansion, 0) }
        }

    var sumShortestPaths = 0L
    for( i in 0..<galaxys.size){
        for( j in i..<galaxys.size) {
            sumShortestPaths += stepsBetween(galaxys[i], galaxys[j])
            println("$i $j ${stepsBetween(galaxys[i], galaxys[j])}")
        }
    }
    println("sum of shortest paths: $sumShortestPaths")
}

fun main() {
    //part 1
    parseGalaxy("2023/11/sample", 2) // 374
    parseGalaxy("2023/11/puzzle", 2) // 9274989
    //part 2
    parseGalaxy("2023/11/sample", 10) // 1030
    parseGalaxy("2023/11/sample", 100) // 8410
    parseGalaxy("2023/11/puzzle", 1000000) // 357134560737
}
