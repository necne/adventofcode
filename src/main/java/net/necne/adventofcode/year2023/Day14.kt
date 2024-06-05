package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset
import kotlin.test.assertEquals

object Day14 {
    private val posOpen = '.'
    private val posRound = 'O'
    private val posSquare = '#'

    fun toString(grid: List<CharArray>): String {
        return grid.joinToString(separator = "\n") { it.joinToString("") } + "\n"
    }

    fun evaluate(resourceGrid: String, resourceExpected: String? = null): Int {
        var grid = loadGrid(resourceGrid)
        println(toString(grid))
        shiftN(grid)
        println(toString(grid))

        if (resourceExpected != null) assertEquals(
            toString(loadGrid(resourceExpected)),
            toString(grid),
            "Grid shifted north"
        )

        return load(grid)
    }

    fun loadGrid(resource: String): List<CharArray> {
        return Resources.readLines(Resources.getResource(resource), Charset.defaultCharset())
            .map { it.toCharArray() }
    }

    private fun shiftN(grid: List<CharArray>) {
        for (c in 0..<grid.first().size) {
            var firstEmpty = -1
            for (r in grid.indices) {
                when (val case = grid[r][c]) {
                    posOpen -> if (firstEmpty < 0) firstEmpty = r
                    posSquare -> firstEmpty = -1
                    posRound -> {
                        if (firstEmpty >= 0) {
                            grid[firstEmpty][c] = posRound
                            grid[r][c] = posOpen
                            firstEmpty = nextEmpty(grid, c, firstEmpty + 1)
                        }
                    }

                    else -> throw IllegalArgumentException("Bad map argument $case")
                }

            }
        }
    }

    private fun nextEmpty(grid: List<CharArray>, col: Int, min: Int): Int {
        for (r in min..<grid.size) {
            if (posOpen == grid[r][col]) return r
        }
        return -1
    }

    private fun load(grid: List<CharArray>): Int {
        var sum = 0
        for(r in grid.indices) {
            sum += grid[r].filter { it == posRound }.size * (grid.size - r)
        }

        return sum
    }


}

fun main() {
    Day14.evaluate("2023/14/sample", "2023/14/sample-shiftN-expected").also{ assertEquals(136, it) }
    Day14.evaluate("2023/14/puzzle").also { assertEquals(106186, it) }
}