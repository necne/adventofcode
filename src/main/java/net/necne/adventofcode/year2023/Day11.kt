package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset
import kotlin.math.abs

// https://adventofcode.com/2023/day/11

fun print(layout: List<List<Char>>) =
    println(
        layout.map { row -> "" + row.joinToString("") }
            .joinToString("\n")
    )

fun stepsBetween(a: Pos, b: Pos) = abs(a.row - b.row) + abs(a.col - b.col)

fun parseGalaxy(resource: String) {
    val layout = Resources.readLines(Resources.getResource(resource), Charset.defaultCharset())
        .map {it.toCharArray().toMutableList() }
        .toMutableList()

    for ( row in layout.size-1 downTo 0 )
        if (layout[row].none { it == '#' }) {
            println("no galaxy in row $row")
            layout.add(row, layout[row].toMutableList())
        }

    for ( col in layout.first().size-1 downTo 0)
        if(layout.none { it[col] == '#' }) {
            println("no galaxy in col $col")
            layout.map { it.add(col, '.')  }
        }
    print(layout)

    val poss : MutableList<Pos> = mutableListOf()
    for( row in 0..<layout.size)
        for( col in 0..<layout.first().size)
            if(layout[row][col] == '#') poss.add(Pos(col, row))

    var sumShortestPaths = 0
    for( i in 0..<poss.size){
        for( j in i..<poss.size) {
            sumShortestPaths += stepsBetween(poss[i], poss[j])
            println("$i $j ${stepsBetween(poss[i], poss[j])}")
        }
    }
    println("sum of shortest paths: $sumShortestPaths")
}

fun main() {
    parseGalaxy("2023/11/sample") // 374
    parseGalaxy("2023/11/puzzle") // 9274989
}
