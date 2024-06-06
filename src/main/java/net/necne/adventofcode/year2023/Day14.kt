package net.necne.adventofcode.year2023

import com.google.common.io.Resources
import java.nio.charset.Charset
import kotlin.test.assertEquals

object Day14 {
    private val posOpen = '.'
    private val posRound = 'O'
    private val posSquare = '#'

    enum class Slide {
        North {
            override fun nextEmpty(grid: List<CharArray>, col: Int, row: Int): Int {
                for (r in row + 1..<grid.size) {
                    if (posOpen == grid[r][col]) return r
                }
                return -1
            }

            override fun slide(grid: List<CharArray>) {
                slideVertical(grid, true)
            }
        },
        South {
            override fun nextEmpty(grid: List<CharArray>, col: Int, row: Int): Int {
                for (r in row - 1..<grid.size) {
                    if (posOpen == grid[r][col]) return r
                }
                return -1
            }

            override fun slide(grid: List<CharArray>) {
                slideVertical(grid, false)
            }
        },
        West {
            override fun nextEmpty(grid: List<CharArray>, col: Int, row: Int): Int {
                for (c in col + 1..<grid.first().size) {
                    if (posOpen == grid[row][c]) return c
                }
                return -1
            }

            override fun slide(grid: List<CharArray>) {
                slideHorizontal(grid, true)
            }

        },
        East {
            override fun nextEmpty(grid: List<CharArray>, col: Int, row: Int): Int {
                for (c in col - 1..<grid.first().size) {
                    if (posOpen == grid[row][c]) return c
                }
                return -1
            }

            override fun slide(grid: List<CharArray>) {
                slideHorizontal(grid, false)
            }

        };

        abstract fun nextEmpty(grid: List<CharArray>, col: Int, row: Int): Int

        abstract fun slide(grid: List<CharArray>)

        protected fun slideVertical(grid: List<CharArray>, isNorth: Boolean) {
            for (c in 0..<grid.first().size) {
                var firstEmpty = -1
                val indices = if (isNorth) grid.indices else grid.indices.reversed()
                for (r in indices) {
                    when (val case = grid[r][c]) {
                        posOpen -> if (firstEmpty < 0) firstEmpty = r
                        posSquare -> firstEmpty = -1
                        posRound -> {
                            if (firstEmpty >= 0) {
                                grid[firstEmpty][c] = posRound
                                grid[r][c] = posOpen
                                firstEmpty = nextEmpty(grid, c, firstEmpty)
                            }
                        }

                        else -> throw IllegalArgumentException("Bad map argument $case")
                    }
                }
            }
        }

        protected fun slideHorizontal(grid: List<CharArray>, isWest: Boolean) {
            for (r in grid.indices) {
                val row = grid[r]
                var firstEmpty = -1
                val indices = if (isWest) row.indices else row.indices.reversed()
                for (c in indices) {
                    when (val case = row[c]) {
                        posOpen -> if (firstEmpty < 0) firstEmpty = c
                        posSquare -> firstEmpty = -1
                        posRound -> {
                            if (firstEmpty >= 0) {
                                row[firstEmpty] = posRound
                                row[c] = posOpen
                                firstEmpty = nextEmpty(grid, firstEmpty, r)
                            }
                        }

                        else -> throw IllegalArgumentException("Bad map argument $case")
                    }
                }
            }
        }
    }

    private val CYCLE = listOf(Slide.North, Slide.West, Slide.South, Slide.East)

    fun toString(grid: List<CharArray>): String {
        return grid.joinToString(separator = "\n") { it.joinToString("") }
    }

    fun evaluateSlide(resourceGrid: String, resourceExpected: String? = null): Int {
        val grid = loadGrid(resourceGrid)
        println(toString(grid))
        Slide.North.slide(grid)
        println(toString(grid))

        if (resourceExpected != null) assertEquals(
            toString(loadGrid(resourceExpected)),
            toString(grid),
            "Grid shifted north"
        )

        return load(grid)
    }

    fun evaluateCycleTest(resourceGrid: String): Int {
        val grid = loadGrid(resourceGrid)
        println(toString(grid))
        Slide.North.slide(grid)
        println("\nNorth\n${toString(grid)}")
        Slide.West.slide(grid)
        println("\nWest\n${toString(grid)}")
        Slide.South.slide(grid)
        println("\nSouth\n${toString(grid)}")
        Slide.East.slide(grid)
        println("\nEast\n${toString(grid)}")

        println("\nCycle1\n${toString(grid)}")
        test(grid, "2023/14/sample-cycle1")

        for (i in 2..3) {
            cycle(grid)
            println("\nCycle$i\n${toString(grid)}")
            test(grid, "2023/14/sample-cycle$i")
        }

        return load(grid)
    }

    fun evaluateCycle(resourceGrid: String, reps: Int): Int {
        val grid = loadGrid(resourceGrid)
        val hash_rep = mutableMapOf<Int, Int>()
        var rep = 0
        while (++rep <= reps) {
            cycle(grid)

            val hashCode = hashCode(grid)
            println("   $hashCode")
            if (hash_rep.contains(hashCode)) {
                println("cycle detected at rep $rep with rep ${hash_rep[hashCode]}")

                val loopSize = (rep - hash_rep[hashCode]!!)
                val skipLoops = (reps - rep) / loopSize
                rep += skipLoops * loopSize

                hash_rep.clear()
            }
            hash_rep[hashCode] = rep
        }

        return load(grid)
    }

    private fun hashCode(grid: List<CharArray>): Int {
        return toString(grid).hashCode()
    }

    private fun cycle(grid: List<CharArray>) {
        for (slide in CYCLE) slide.slide(grid)
    }

    private fun test(grid: List<CharArray>, resource: String) {
        assertEquals(
            toString(grid),
            Resources.toString(Resources.getResource(resource), Charset.defaultCharset()),
            "Mismatch on resource $resource"
        )
    }

    private fun loadGrid(resource: String): List<CharArray> {
        return Resources.readLines(Resources.getResource(resource), Charset.defaultCharset())
            .map { it.toCharArray() }
    }

    private fun load(grid: List<CharArray>): Int {
        var sum = 0
        for (r in grid.indices) {
            sum += grid[r].filter { it == posRound }.size * (grid.size - r)
        }

        return sum
    }
}

fun main() {
    Day14.evaluateCycleTest("2023/14/sample").also { assertEquals(69, it) }

    Day14.evaluateSlide("2023/14/sample", "2023/14/sample-shiftN-expected").also { assertEquals(136, it) }
    Day14.evaluateSlide("2023/14/puzzle").also { assertEquals(106186, it) }

    Day14.evaluateCycle("2023/14/sample", 1000000000).also { assertEquals(64, it) }
    Day14.evaluateCycle("2023/14/puzzle", 1000000000).also { assertEquals(106390, it) }

}